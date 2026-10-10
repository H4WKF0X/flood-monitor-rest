package com.example.floodmonitor.service;

import com.example.floodmonitor.hydrodynamics.Catchment;
import com.example.floodmonitor.hydrodynamics.RainField;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
@EnableScheduling
public class SimulationService {
    private static final Map<String, Double> RIVER_AREAS_KM2 = Map.of(
            "Donau",   100_000.0,
            "Salzach",   4_400.0,
            "Mur",       7_000.0,
            "Inn",       5_800.0
    );

    private final Clock clock;
    private final Random random;

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final RainField rainField;

    private final Map<String, Catchment> riverCatchments = new HashMap<>();

    public SimulationService(Clock clock, Random random, StationRepository  stationRepository, MeasurementRepository measurementRepository, RainField rainField) {
        this.clock = clock;
        this.random = random;
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.rainField = rainField;
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

        Map<String, Catchment> riverCatchments = new HashMap<>();

        for (String river : byRiver.keySet()) {
            // riverCatchments.put(river, new Catchment());
        }

        for (Map.Entry<String, List<Station>> entry : byRiver.entrySet()) {

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
