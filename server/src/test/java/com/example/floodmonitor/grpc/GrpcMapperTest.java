package com.example.floodmonitor.grpc;

import com.example.floodmonitor.exception.InvalidRequestException;
import com.example.floodmonitor.grpc.mapper.MeasurementMapper;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GrpcMapperTest {
    Instant now = Instant.ofEpochSecond(1_700_000_000, 123_456_789);
    @Test
    void measurementRoundtripKeepsTimestamp() {
        Measurement m1 = new Measurement("1", now, 1.0, 1.0, 1.0, 1.0, 1.0, StationStatus.ONLINE, WarningLevel.NORMAL);
        Measurement m2 = MeasurementMapper.toDomain(MeasurementMapper.toProto(m1));

        assertThat(m1.timestamp().getEpochSecond()).isEqualTo(m2.timestamp().getEpochSecond());
        assertThat(m1.timestamp().getNano()).isEqualTo(m2.timestamp().getNano());
    }

    @Test
    void domainMeasurementToProtoKeepsTimestamp() {
        Measurement m1 = new Measurement("1", now, 1.0, 1.0, 1.0, 1.0, 1.0, StationStatus.ONLINE, WarningLevel.NORMAL);
        com.example.floodmonitor.grpc.v1.Measurement m2 = MeasurementMapper.toProto(m1);

        assertThat(m1.timestamp().getEpochSecond()).isEqualTo(m2.getTimestamp().getSeconds());
        assertThat(m1.timestamp().getNano()).isEqualTo(m2.getTimestamp().getNanos());
    }

    @Test
    void measurementRoundtripKeepsEnums() {
        Measurement m1 = new Measurement("1", now, 1.0, 1.0, 1.0, 1.0, 1.0, StationStatus.ONLINE, WarningLevel.NORMAL);
        Measurement m2 = MeasurementMapper.toDomain(MeasurementMapper.toProto(m1));

        assertThat(m2.status()).isEqualTo(StationStatus.ONLINE);
        assertThat(m2.warningLevel()).isEqualTo(WarningLevel.NORMAL);
    }

    @Test
    void domainMeasurementToProtoKeepsEnums() {
        Measurement m1 = new Measurement("1", now, 1.0, 1.0, 1.0, 1.0, 1.0, StationStatus.ONLINE, WarningLevel.NORMAL);
        com.example.floodmonitor.grpc.v1.Measurement m2 = MeasurementMapper.toProto(m1);

        assertThat(m2.getStationStatus()).isEqualTo(com.example.floodmonitor.grpc.v1.StationStatus.STATION_STATUS_ONLINE);
        assertThat(m2.getWarningLevel()).isEqualTo(com.example.floodmonitor.grpc.v1.WarningLevel.WARNING_LEVEL_NORMAL);
    }

    @Test
    void unspecifiedProtoWarningLevelToDomainThrows() {
        com.example.floodmonitor.grpc.v1.Measurement m2 = com.example.floodmonitor.grpc.v1.Measurement.newBuilder()
                .setStationStatus(com.example.floodmonitor.grpc.v1.StationStatus.STATION_STATUS_ONLINE).build();
        assertThatThrownBy(() -> MeasurementMapper.toDomain(m2)).isInstanceOf(InvalidRequestException.class);
    }
}
