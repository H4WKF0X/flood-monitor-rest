package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.exception.InvalidRequestException;
import com.example.floodmonitor.exception.NoMeasurementForStationException;
import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class MeasurementService {
    private final MeasurementRepository measurementRepository;
    private final StationRepository stationRepository;

    private final double intervalMs;
    private final int maxLimit;

    public MeasurementService(MeasurementRepository measurementRepository,  StationRepository stationRepository,
                              @Value("${simulation.interval}") double intervalMs,  @Value("${api.measurements.maxLimit}") int maxLimit) {
        this.measurementRepository = measurementRepository;
        this.stationRepository = stationRepository;
        this.intervalMs = intervalMs;
        this.maxLimit = maxLimit;
    }

    public MeasurementListResponse getMeasurements(String id, Instant fromFilter, Instant toFilter, int limit, WarningLevel warningLevelFilter) {
        stationRepository.findById(id).orElseThrow(() -> new StationNotFoundException(id));
        if (limit <= 0 || limit > maxLimit) throw new InvalidRequestException("Parameter 'limit' must be between 1 and " +  maxLimit);

        // add fromFiler before toFilter check in #11
        List<Measurement> filtered = getTimeFilteredMeasurements(id, fromFilter, toFilter).stream()
                .filter(m -> warningLevelFilter == null || m.warningLevel() == warningLevelFilter)
                .toList();

        List<MeasurementResponse> responses = filtered.stream()
                .limit(limit)
                .map(this::toMeasurementResponse)
                .toList();

        return new MeasurementListResponse(responses, filtered.size());
    }

    public MeasurementResponse getLatestMeasurement(String id){
        return measurementRepository.getLastMeasurement(id).map(this::toMeasurementResponse).orElseThrow(() -> new NoMeasurementForStationException(id));
    }

    public StatisticsResponse getStatistics(String id, Instant fromFilter, Instant toFilter) {
        stationRepository.findById(id).orElseThrow(() -> new StationNotFoundException(id));
        List<Measurement> measurementHistory = getTimeFilteredMeasurements(id, fromFilter, toFilter);
        return computeStatisticsResponse(measurementHistory);
    }

    private List<Measurement> getTimeFilteredMeasurements(String id, Instant fromFilter, Instant toFilter) {
        if (fromFilter != null && toFilter != null && fromFilter.isAfter(toFilter)) throw new  InvalidRequestException("Parameter 'from' must not be after 'to'");
        List<Measurement> measurementHistory = measurementRepository.getMeasurementHistory(id);
        if (measurementHistory.isEmpty()) throw new NoMeasurementForStationException(id);
        return measurementHistory.stream()
                .filter(m -> fromFilter == null || !m.timestamp().isBefore(fromFilter))
                .filter(m -> toFilter == null || !m.timestamp().isAfter(toFilter)).toList();
    }

    private MeasurementResponse toMeasurementResponse(Measurement m) {
        return new MeasurementResponse(m.stationId(), m.timestamp(), round2(m.waterLevel()), round2(m.flowRate()), round2(m.rainfall()), round2(m.temperature()), round2(m.batteryLevel()), m.status(), m.warningLevel());
    }

    private StatisticsResponse computeStatisticsResponse(List<Measurement> measurements) {
        if(measurements.isEmpty()) return new StatisticsResponse(null, null,null, null, 0.0, 0, 0, 0);

        DoubleSummaryStatistics waterLevelStats = measurements.stream().mapToDouble(Measurement::waterLevel).summaryStatistics();
        DoubleSummaryStatistics flowRateStats = measurements.stream().mapToDouble(Measurement::flowRate).summaryStatistics();
        DoubleSummaryStatistics rainfallStats =  measurements.stream().mapToDouble(Measurement::rainfall).summaryStatistics();

        double minWaterLevel = waterLevelStats.getMin();
        double maxWaterLevel = waterLevelStats.getMax();
        double meanWaterLevel = waterLevelStats.getAverage();

        double meanFlowRate = flowRateStats.getAverage();

        double totalRainfall = rainfallStats.getSum() * intervalMs / 3_600_000.0;

        int measurementCount = measurements.size();

        int warningPeriodCount = 0;
        int criticalPeriodCount = 0;
        boolean wasWarning = false;
        boolean wasCritical = false;

        for (Measurement m : measurements) {
            boolean isCritical = m.warningLevel() == WarningLevel.CRITICAL;
            boolean isWarning = isCritical || m.warningLevel() == WarningLevel.WARNING;

            if (isWarning && !wasWarning) {
                warningPeriodCount++;
            }
            if (isCritical && !wasCritical) {
                criticalPeriodCount++;
            }

            wasWarning = isWarning;
            wasCritical = isCritical;
        }

        return new StatisticsResponse(round2(minWaterLevel), round2(maxWaterLevel), round2(meanWaterLevel), round2(meanFlowRate), round2(totalRainfall), measurementCount, warningPeriodCount, criticalPeriodCount);
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
