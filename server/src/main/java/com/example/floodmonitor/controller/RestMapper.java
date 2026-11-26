package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StationResponse;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationWithWarningLevel;

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

    public static MeasurementListResponse toMeasurementListResponse(List<Measurement> measurements) {
        int totalCount = measurements.size();
        List<MeasurementResponse> measurementResponses = measurements.stream().map(RestMapper::toMeasurementResponse).toList();

        return new MeasurementListResponse(measurementResponses, totalCount);
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
