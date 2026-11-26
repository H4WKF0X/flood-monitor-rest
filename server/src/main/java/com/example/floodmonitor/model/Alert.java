package com.example.floodmonitor.model;

import java.time.Instant;

public record Alert(String stationId, String name, String river, Location location, Instant timestamp, double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel, double waterLevel, WarningLevel warningLevel) {
}