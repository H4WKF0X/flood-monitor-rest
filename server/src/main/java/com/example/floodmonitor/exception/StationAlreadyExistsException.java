package com.example.floodmonitor.exception;

public class StationAlreadyExistsException extends RuntimeException {
    public StationAlreadyExistsException(String stationId) {
        super("Station with id: " + stationId +  " already exists");
    }
}
