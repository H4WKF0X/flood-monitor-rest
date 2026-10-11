package com.example.floodmonitor.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "stations")
public record StationListResponse(@JacksonXmlProperty(localName = "station") @JacksonXmlElementWrapper(useWrapping = false) List<StationResponse> stations) {
}
