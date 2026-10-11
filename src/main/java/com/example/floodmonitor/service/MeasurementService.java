package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class MeasurementService {
    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;

    public MeasurementService(MeasurementRepository measurementRepository,  StationRepository stationRepository) {
        this.measurementRepository = measurementRepository;
        this.stationRepository = stationRepository;
    }

    public Optional<List<MeasurementResponse>> getMeasurements(String id) {
        if (stationRepository.findById(id).isEmpty()) return Optional.empty();
        return Optional.of(measurementRepository.getMeasurementHistory(id).stream().map(this::toMeasurementResponse).toList());
    }

    public Optional<MeasurementResponse> getLatestMeasurement(String id){
        return measurementRepository.getLastMeasurement(id).map(this::toMeasurementResponse);
    }

    private MeasurementResponse toMeasurementResponse(Measurement m) {
        return new MeasurementResponse(m.stationId(), m.timestamp(), round2(m.waterLevel()), round2(m.flowRate()), round2(m.rainfall()), round2(m.temperature()), round2(m.batteryLevel()), m.status(), m.warningLevel());
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
