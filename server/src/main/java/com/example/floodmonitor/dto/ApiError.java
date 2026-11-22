package com.example.floodmonitor.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "error")
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
