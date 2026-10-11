package com.example.floodmonitor.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "statistics")
public record StatisticsResponse(Double minWaterLevel, Double maxWaterLevel, Double meanWaterLevel, Double meanFlowRate, double totalRainfall, int measurementCount, int warningPeriodCount, int criticalPeriodCount) {
}
