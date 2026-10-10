package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class WarningLevelService {
    private final Clock clock;
    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final Duration maxAge;

    public WarningLevelService(Clock clock, StationRepository stationRepository, MeasurementRepository measurementRepository,
                               @Value("${simulation.interval}") double intervalMs) {
        this.clock = clock;
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.maxAge = Duration.ofMillis((long) (intervalMs * 3));
    }

    public WarningLevel getWarningLevel(Station station, double waterLevel) {
        if (waterLevel < 0 || !Double.isFinite(waterLevel)) { return WarningLevel.UNKNOWN; }
        else if (waterLevel < station.warningWaterLevel()) { return WarningLevel.NORMAL; }
        else if (waterLevel < station.criticalWaterLevel()) { return WarningLevel.WARNING; }
        return WarningLevel.CRITICAL;
    }

    public WarningLevel getWarningLevel(String stationId, double waterLevel) {
        return getWarningLevel(stationRepository.findById(stationId).orElseThrow(), waterLevel);
    }

    public WarningLevel getCurrentWarningLevel(Station station) {
        Optional<Measurement> measurementOptional = measurementRepository.getLastMeasurement(station.id());
        if (measurementOptional.isEmpty() || !station.isActive()) return WarningLevel.UNKNOWN;
        Measurement measurement = measurementOptional.get();

        Instant now = Instant.now(clock);
        boolean tooOld = measurement.timestamp().isBefore(now.minus(maxAge));

        if (tooOld) return WarningLevel.UNKNOWN;
        return getWarningLevel(station, measurement.waterLevel());
    }

    public WarningLevel getCurrentWarningLevel(String stationId) {
        return getCurrentWarningLevel(stationRepository.findById(stationId).orElseThrow());
    }

}
