package com.example.floodmonitor.grpc.mapper;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationWithWarningLevel;
import com.example.floodmonitor.model.WarningLevel;

public class StationMapper {
    private StationMapper(){}

    public static com.example.floodmonitor.grpc.v1.Station toProto(StationWithWarningLevel sw) {
        Station s = sw.station();
        WarningLevel w = sw.warningLevel();
        return com.example.floodmonitor.grpc.v1.Station.newBuilder()
                .setId(s.id()).setName(s.name()).setRiver(s.river())
                .setLocation(CommonMapper.toProto(s.location()))
                .setNormalWaterLevel(s.normalWaterLevel())
                .setWarningWaterLevel(s.warningWaterLevel())
                .setCriticalWaterLevel(s.criticalWaterLevel())
                .setIsActive(s.isActive())
                .setCurrentWarningLevel(CommonMapper.toProto(w)).build();
    }
}
