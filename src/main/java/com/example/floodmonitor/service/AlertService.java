package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.AlertListResponse;
import com.example.floodmonitor.dto.AlertResponse;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class AlertService {
    private static final List<WarningLevel> ALERT_ORDER = List.of(WarningLevel.CRITICAL, WarningLevel.WARNING, WarningLevel.UNKNOWN);
    private static final Comparator<AlertResponse> ALERT_COMPARATOR = Comparator.comparing((AlertResponse a) -> ALERT_ORDER.indexOf(a.warningLevel())).thenComparing(AlertResponse::stationId);

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;

    public AlertService(StationRepository stationRepository, MeasurementRepository measurementRepository, WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    public AlertListResponse getAlerts() {
        List<AlertResponse> alertResponses = new ArrayList<>();

        List<Station> stations = stationRepository.findAll();
        for (Station station : stations) {
            Measurement lastMeasurement = measurementRepository.getLastMeasurement(station.id()).orElse(null);
            WarningLevel warningLevel = warningLevelService.getCurrentWarningLevel(station);
            if (!station.isActive() || lastMeasurement == null || lastMeasurement.status() == StationStatus.OFFLINE
                    || warningLevel == WarningLevel.NORMAL) continue;
            alertResponses.add(toAlertResponse(station, lastMeasurement,  warningLevel));
        }
        alertResponses.sort(ALERT_COMPARATOR);
        return new AlertListResponse(alertResponses);
    }

    private AlertResponse toAlertResponse(Station s, Measurement m, WarningLevel w) {
        return new AlertResponse(s.id(), s.name(), s.river(), s.location(), m.timestamp(), s.normalWaterLevel(), s.warningWaterLevel(), s.criticalWaterLevel(), round2(m.waterLevel()), w);
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
