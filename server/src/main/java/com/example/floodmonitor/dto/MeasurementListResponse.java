package com.example.floodmonitor.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "measurements")
public record MeasurementListResponse(@JacksonXmlProperty(localName = "measurement") @JacksonXmlElementWrapper(useWrapping = false) List<MeasurementResponse> measurements,
                                      @JacksonXmlProperty(isAttribute = true) int totalCount) {}
