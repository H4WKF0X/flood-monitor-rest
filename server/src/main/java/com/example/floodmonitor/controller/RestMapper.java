package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.*;
import com.example.floodmonitor.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class RestMapper {
    public static StationResponse toStationResponse(StationWithWarningLevel s) {
        return new StationResponse(s.station().id(), s.station().name(), s.station().river(), s.station().location(), s.station().normalWaterLevel(), s.station().warningWaterLevel(), s.station().criticalWaterLevel(), s.station().isActive(), s.warningLevel());
    }

    public static StationListResponse toStationListResponse(List<StationWithWarningLevel> stations) {
        int totalCount = stations.size();
        List<StationResponse> stationResponses = stations.stream().map(RestMapper::toStationResponse).toList();

        return new StationListResponse(stationResponses, totalCount);
    }

    public static MeasurementResponse toMeasurementResponse(Measurement m) {
        return new MeasurementResponse(m.stationId(), m.timestamp(), round2(m.waterLevel()), round2(m.flowRate()), round2(m.rainfall()), round2(m.temperature()), round2(m.batteryLevel()), m.status(), m.warningLevel());
    }

    public static MeasurementListResponse toMeasurementListResponse(MeasurementsWithTotalCount measurements) {
        int totalCount = measurements.totalCount();
        List<MeasurementResponse> measurementResponses = measurements.measurement().stream().map(RestMapper::toMeasurementResponse).toList();

        return new MeasurementListResponse(measurementResponses, totalCount);
    }

    private static AlertResponse toAlertResponse(Alert a) {
        return new AlertResponse(a.stationId(), a.name(), a.river(), a.location(), a.timestamp(), a.normalWaterLevel(), a.warningWaterLevel(), a.criticalWaterLevel(), round2(a.waterLevel()), a.warningLevel());
    }

    public static AlertListResponse toAlertListResponse(List<Alert> alerts) {
        int totalCount = alerts.size();
        List<AlertResponse> alertResponses = alerts.stream().map(RestMapper::toAlertResponse).toList();

        return new AlertListResponse(alertResponses, totalCount);
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
