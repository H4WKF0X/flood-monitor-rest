package com.example.floodmonitor.grpc.mapper;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationStatus;

public class MeasurementMapper {
    private MeasurementMapper() {}

    public static com.example.floodmonitor.grpc.v1.Measurement toProto(Measurement m) {
        return com.example.floodmonitor.grpc.v1.Measurement.newBuilder()
                .setStationId(m.stationId()).setTimestamp(CommonMapper.toProto(m.timestamp()))
                .setWaterLevel(m.waterLevel()).setFlowRate(m.flowRate())
                .setRainfall(m.rainfall()).setTemperature(m.temperature())
                .setBatteryLevel(m.batteryLevel()).setStationStatus(toProto(m.status()))
                .setWarningLevel(CommonMapper.toProto(m.warningLevel())).build();
    }

    public static com.example.floodmonitor.grpc.v1.StationStatus toProto(StationStatus s) {
        return switch (s) {
            case ONLINE -> com.example.floodmonitor.grpc.v1.StationStatus.STATION_STATUS_ONLINE;
            case MAINTENANCE -> com.example.floodmonitor.grpc.v1.StationStatus.STATION_STATUS_MAINTENANCE;
            case OFFLINE ->  com.example.floodmonitor.grpc.v1.StationStatus.STATION_STATUS_OFFLINE;
        };
    }
}
