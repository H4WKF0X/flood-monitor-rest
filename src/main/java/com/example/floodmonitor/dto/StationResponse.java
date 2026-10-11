package com.example.floodmonitor.dto;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.WarningLevel;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "station")
public record StationResponse(String id, String name, String river, Location location, double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel, boolean isActive, WarningLevel currentWarningLevel) {
}
