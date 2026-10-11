package com.example.floodmonitor.dto;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.WarningLevel;

import java.time.Instant;

public record AlertResponse(String stationId, String name, String river, Location location, Instant timestamp, double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel, double waterLevel, WarningLevel warningLevel) {
}
