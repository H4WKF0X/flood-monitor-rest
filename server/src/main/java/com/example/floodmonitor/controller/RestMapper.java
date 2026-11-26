package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StationResponse;
import com.example.floodmonitor.model.StationWithWarningLevel;

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
}
