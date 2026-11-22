package com.example.floodmonitor.model;

public record Station(String id, String name, String river, Location location, double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel, boolean isActive) {
}
