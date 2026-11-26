package com.example.floodmonitor.grpc.mapper;


import com.example.floodmonitor.model.Alert;

public class AlertMapper {
    public com.example.floodmonitor.grpc.v1.Alert toProto(Alert a) {
        return com.example.floodmonitor.grpc.v1.Alert.newBuilder()
                .setStationId(a.stationId()).setName(a.name()).setRiver(a.river())
                .setLocation(CommonMapper.toProto(a.location()))
                .setTimestamp(CommonMapper.toProto(a.timestamp()))
                .setNormalWaterLevel(a.normalWaterLevel())
                .setWarningWaterLevel(a.warningWaterLevel())
                .setCriticalWaterLevel(a.criticalWaterLevel())
                .setWaterLevel(a.waterLevel())
                .setWarningLevel(CommonMapper.toProto(a.warningLevel())).build();
    }
}
