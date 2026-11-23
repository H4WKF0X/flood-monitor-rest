package com.example.floodmonitor.grpc.mapper;

import com.example.floodmonitor.model.WarningLevel;

import java.time.Instant;

public class CommonMapper {
    private CommonMapper() {}

    public static com.example.floodmonitor.grpc.v1.WarningLevel toProto(WarningLevel w) {
        return switch (w) {
            case NORMAL -> com.example.floodmonitor.grpc.v1.WarningLevel.WARNING_LEVEL_NORMAL;
            case WARNING -> com.example.floodmonitor.grpc.v1.WarningLevel.WARNING_LEVEL_WARNING;
            case CRITICAL -> com.example.floodmonitor.grpc.v1.WarningLevel.WARNING_LEVEL_CRITICAL;
            case UNKNOWN ->  com.example.floodmonitor.grpc.v1.WarningLevel.WARNING_LEVEL_UNKNOWN;
        };
    }

    public static com.google.protobuf.Timestamp toProto(Instant t) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(t.getEpochSecond())
                .setNanos(t.getNano()).build();
    }
}
