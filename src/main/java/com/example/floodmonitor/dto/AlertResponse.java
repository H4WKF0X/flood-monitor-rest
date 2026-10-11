package com.example.floodmonitor.dto;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.WarningLevel;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "alert")
public record AlertResponse(String stationId, String name, String river, Location location, Instant timestamp, double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel, double waterLevel, WarningLevel warningLevel) {
}
