package com.example.floodmonitor.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "alerts")
public record AlertListResponse(@JacksonXmlProperty(localName = "alert") @JacksonXmlElementWrapper(useWrapping = false) List<AlertResponse> alerts) {
}
