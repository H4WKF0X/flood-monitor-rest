package com.example.floodmonitor.dto;

public record StatisticsResponse(Double minWaterLevel, Double maxWaterLevel, Double meanWaterLevel, Double meanFlowRate, double totalRainfall, int measurementCount, int warningPeriodCount, int criticalPeriodCount) {
}
