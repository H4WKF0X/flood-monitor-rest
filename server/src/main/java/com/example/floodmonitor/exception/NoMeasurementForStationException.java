package com.example.floodmonitor.exception;

public class NoMeasurementForStationException extends RuntimeException {
    public NoMeasurementForStationException(String stationId) {
        super("No measurement exist for station " + stationId);
    }
}
