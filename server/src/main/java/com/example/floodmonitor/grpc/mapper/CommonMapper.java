package com.example.floodmonitor.grpc.mapper;

import com.example.floodmonitor.exception.InvalidRequestException;
import com.example.floodmonitor.model.Location;
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

    public static WarningLevel toDomain(com.example.floodmonitor.grpc.v1.WarningLevel w) {
        return switch (w) {
            case WARNING_LEVEL_UNSPECIFIED, UNRECOGNIZED -> throw new InvalidRequestException("Invalid warning level: " + w);
            case WARNING_LEVEL_NORMAL -> WarningLevel.NORMAL;
            case WARNING_LEVEL_WARNING -> WarningLevel.WARNING;
            case WARNING_LEVEL_CRITICAL -> WarningLevel.CRITICAL;
            case WARNING_LEVEL_UNKNOWN -> WarningLevel.UNKNOWN;
        };
    }

    public static com.example.floodmonitor.grpc.v1.Location toProto(Location l) {
        return com.example.floodmonitor.grpc.v1.Location.newBuilder()
                .setLatitude(l.latitude())
                .setLongitude(l.longitude())
                .build();
    }

    public static com.google.protobuf.Timestamp toProto(Instant t) {
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(t.getEpochSecond())
                .setNanos(t.getNano()).build();
    }

    public static Instant toDomain(com.google.protobuf.Timestamp t) {
        return Instant.ofEpochSecond(t.getSeconds(), t.getNanos());
    }
}
