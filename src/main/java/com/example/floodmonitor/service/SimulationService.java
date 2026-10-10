package com.example.floodmonitor.service;

import com.example.floodmonitor.hydrodynamics.Catchment;
import com.example.floodmonitor.hydrodynamics.RainField;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
@EnableScheduling
public class SimulationService {
    private static final double DEFAULT_AREA_KM2 = 1_000.0;
    private static final double H0 = 0.5;
    private static final double B = 2.0;

    private static final Map<String, Double> RIVER_AREAS_KM2 = Map.of(
            "Donau",   100_000.0,
            "Salzach",   4_400.0,
            "Mur",       7_000.0,
            "Inn",       5_800.0
    );

    private final double intervalMs;

    private final Clock clock;
    private final Random random;

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final RainField rainField;

    private final Map<String, Catchment> riverCatchments = new HashMap<>();

    public SimulationService(Clock clock, Random random, StationRepository  stationRepository, MeasurementRepository measurementRepository, RainField rainField,
                             @Value("${simulation.interval}") double intervalMs) {
        this.clock = clock;
        this.random = random;
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.rainField = rainField;

        this.intervalMs = intervalMs;
    }

    @Scheduled(fixedRateString = "${simulation.interval}")
    public void tick() {
        List<Station> stations = stationRepository.findAll();
        step(Instant.now(clock), stations);
    }

    private void step(Instant timestamp, List<Station> stations) {
        rainField.advance(stations);

        Map<String, List<Station>> byRiver = new HashMap<>();
        for (Station s : stations) {
            if (!byRiver.containsKey(s.river())) {
                byRiver.put(s.river(), new ArrayList<>());
            }
            byRiver.get(s.river()).add(s);
        }

        for (Map.Entry<String, List<Station>> entry : byRiver.entrySet()) {
            String river = entry.getKey();
            List<Station> riverStations = entry.getValue();

            double area = RIVER_AREAS_KM2.getOrDefault(river, DEFAULT_AREA_KM2);
            double baseFlow = area * 0.02;

            if (!riverCatchments.containsKey(river)) {
                double k = 0.75 * Math.sqrt(area);
                riverCatchments.put(river, new Catchment(k, area, baseFlow));
            }
            Catchment catchment = riverCatchments.get(river);

            double rainSum = 0;
            for (Station s : riverStations) {
                rainSum += rainField.getRain(s);
            }

            double averageRain = rainSum / riverStations.size();

            catchment.advance(averageRain, intervalMs);
            double discharge = catchment.getDischargeM3s();

            double a = baseFlow / 0.67;
            double waterLevel = H0 + Math.pow(discharge / a, 1 / B);

            for (Station s : riverStations) {
            }
        }
    }

    public boolean addStation(Station station) {
        return stationRepository.create(station);
    }

    public List<Station> getStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(String id) {
        return stationRepository.findById(id);
    }
}
