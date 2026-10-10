package com.example.floodmonitor.service;

import com.example.floodmonitor.hydrodynamics.Catchment;
import com.example.floodmonitor.hydrodynamics.RainField;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
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

    private final double BATTERY_DRAIN_PER_HOUR;
    private final double BATTERY_CHARGE_PER_HOUR;
    private static final double BATTERY_LOW = 5.0;

    private static final double TEMPERATURE_MEAN = 15.0; // μ, in °C
    private static final double TEMPERATURE_PULL = 0.02; // θ, per hour
    private static final double TEMPERATURE_NOISE = 0.5; // σ

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
    private final WarningLevelService warningLevelService;
    private final RainField rainField;

    private final Map<String, Catchment> riverCatchments = new HashMap<>();
    private final Map<String, Measurement> lastMeasurements = new HashMap<>();

    public SimulationService(Clock clock, Random random,
                             StationRepository stationRepository, MeasurementRepository measurementRepository,
                             WarningLevelService warningLevelService, RainField rainField,
                             @Value("${simulation.hoursToDrain}") double hoursToDrain, @Value("${simulation.hoursToCharge}") double hoursToCharge,
                             @Value("${simulation.interval}") double intervalMs) {
        this.clock = clock;
        this.random = random;

        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;

        this.warningLevelService = warningLevelService;
        this.rainField = rainField;

        this.BATTERY_DRAIN_PER_HOUR = 100.0 / hoursToDrain;
        this.BATTERY_CHARGE_PER_HOUR = 100.0 / hoursToCharge;
        this.intervalMs = intervalMs;
    }

    @Scheduled(fixedRateString = "${simulation.interval}")
    public void tick() {
        List<Station> stations = stationRepository.findAll();
        step(Instant.now(clock), stations);
    }

    private void step(Instant timestamp, List<Station> stations) {
        double hours = intervalMs / 3_600_000.0;

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
            double waterLevel = H0 + Math.pow(discharge / a, 1.0 / B);

            for (Station s : riverStations) {
                if(!s.isActive()) continue;

                Measurement previous = lastMeasurements.get(s.id());
                if (previous == null) {
                    previous = measurementRepository.getLastMeasurement(s.id()).orElse(null);
                }

                double temperature = TEMPERATURE_MEAN;
                double battery = 100.0;
                StationStatus status = StationStatus.ONLINE;

                if (previous != null) {
                    if (previous.status() == StationStatus.OFFLINE) continue;
                    temperature = previous.temperature();
                    battery = previous.batteryLevel();
                    status = previous.status();
                }

                temperature += TEMPERATURE_PULL * (TEMPERATURE_MEAN - temperature) * hours
                        + TEMPERATURE_NOISE * Math.sqrt(hours) * random.nextGaussian();

                double jitter = 0.9 + 0.2 * random.nextDouble();

                if (status == StationStatus.MAINTENANCE) {
                    battery = Math.min(100.0, battery + BATTERY_CHARGE_PER_HOUR * hours * jitter);
                    if (battery >= 100.0) {
                        status = StationStatus.ONLINE;
                    }
                } else {
                    battery = Math.max(0.0, battery - BATTERY_DRAIN_PER_HOUR * hours * jitter);
                    if (battery <= BATTERY_LOW) {
                        status = StationStatus.MAINTENANCE;
                    }
                }

                double rainMmPerHour = rainField.getRain(s) / hours;

                Measurement measurement = new Measurement(s.id(), timestamp, waterLevel, discharge, rainMmPerHour,
                        temperature, battery, status, warningLevelService.getWarningLevel(s, waterLevel));

                lastMeasurements.put(s.id(), measurement);
                measurementRepository.saveMeasurement(measurement);
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
