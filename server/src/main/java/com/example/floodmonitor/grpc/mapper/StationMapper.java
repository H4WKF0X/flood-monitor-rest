package com.example.floodmonitor.grpc.mapper;


import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;

public class StationMapper {
    private StationMapper(){}

    public static com.example.floodmonitor.grpc.v1.Station toProto(Station s, WarningLevel w) {
        return com.example.floodmonitor.grpc.v1.Station.newBuilder()
                .setId(s.id()).setName(s.name()).setRiver(s.river())
                .setLocation(toProto(s.location()))
                .setNormalWaterLevel(s.normalWaterLevel())
                .setWarningWaterLevel(s.warningWaterLevel())
                .setCriticalWaterLevel(s.criticalWaterLevel())
                .setIsActive(s.isActive())
                .setCurrentWarningLevel(CommonMapper.toProto(w)).build();
    }

    public static com.example.floodmonitor.grpc.v1.Location toProto(Location l) {
        return com.example.floodmonitor.grpc.v1.Location.newBuilder()
                .setLatitude(l.latitude())
                .setLongitude(l.longitude())
                .build();
    }
}
