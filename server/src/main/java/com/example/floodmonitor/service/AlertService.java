package com.example.floodmonitor.service;

import com.example.floodmonitor.model.*;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class AlertService {
    private static final List<WarningLevel> ALERT_ORDER = List.of(WarningLevel.CRITICAL, WarningLevel.WARNING, WarningLevel.UNKNOWN);
    private static final Comparator<Alert> ALERT_COMPARATOR = Comparator.comparing((Alert a) -> ALERT_ORDER.indexOf(a.warningLevel())).thenComparing(Alert::stationId);

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;

    public AlertService(StationRepository stationRepository, MeasurementRepository measurementRepository, WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    public List<Alert> getAlerts(String riverFilter, WarningLevel warningLevelFilter) {
        List<Alert> alertResponses = new ArrayList<>();

        List<Station> stations = stationRepository.findAll();
        for (Station station : stations) {
            Measurement lastMeasurement = measurementRepository.getLastMeasurement(station.id()).orElse(null);
            WarningLevel warningLevel = warningLevelService.getCurrentWarningLevel(station);
            if (!station.isActive() || lastMeasurement == null || lastMeasurement.status() == StationStatus.OFFLINE
                    || warningLevel == WarningLevel.NORMAL || warningLevel == WarningLevel.UNKNOWN ||(warningLevelFilter != null && warningLevelFilter != warningLevel)) continue;
            alertResponses.add(new Alert(station.id(), station.name(), station.river(), station.location(), lastMeasurement.timestamp(), station.normalWaterLevel(), station.warningWaterLevel(), station.criticalWaterLevel(), round2(lastMeasurement.waterLevel()), warningLevel));
        }

        alertResponses.sort(ALERT_COMPARATOR);
        return alertResponses.stream().filter(a -> riverFilter == null || a.river().equalsIgnoreCase(riverFilter)).toList();
    }



    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
