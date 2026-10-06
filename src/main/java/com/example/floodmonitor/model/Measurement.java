package com.example.floodmonitor.model;

import java.time.Instant;

public record Measurement(String stationId, Instant timestamp, double waterLevel, double flowRate, double rainfall, double temperature, int batteryLevel, StationStatus status, WarningLevel warningLevel) {
}
