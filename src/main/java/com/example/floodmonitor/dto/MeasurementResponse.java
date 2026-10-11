package com.example.floodmonitor.dto;

import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "measurement")
public record MeasurementResponse(String stationId, Instant timestamp, double waterLevel, double flowRate, double rainfall, double temperature, double batteryLevel, StationStatus status, WarningLevel warningLevel) {
}
