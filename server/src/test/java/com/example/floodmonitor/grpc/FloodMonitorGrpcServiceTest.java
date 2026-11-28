package com.example.floodmonitor.grpc;

import com.example.floodmonitor.FixedClockConfig;
import com.example.floodmonitor.InProcessGrpcConfig;
import com.example.floodmonitor.grpc.v1.*;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.service.MeasurementPublisher;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationStatus;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.example.floodmonitor.FixedClockConfig.NOW;
import static com.example.floodmonitor.InProcessGrpcConfig.NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "simulation.enabled=false")
@Import({FixedClockConfig.class, InProcessGrpcConfig.class})
public class FloodMonitorGrpcServiceTest {
    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private MeasurementPublisher measurementPublisher;

    private ManagedChannel channel;
    private FloodMonitorServiceGrpc.FloodMonitorServiceBlockingStub stub;

    @BeforeEach
    void createStub() {
        channel = InProcessChannelBuilder.forName(NAME).directExecutor().build();
        stub = FloodMonitorServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void shutDownChannel() {
        channel.shutdownNow();
    }

    @Test
    void listAllStations() {
        ListStationsRequest request = ListStationsRequest.newBuilder().build();
        ListStationsResponse stations = stub.listStations(request);

        assertThat(stations.getTotalCount()).isEqualTo(5);
    }

    @Test
    void getExistingStation() {
        GetStationRequest request = GetStationRequest.newBuilder().setStationId("ST-001").build();
        Station station = stub.getStation(request);

        assertThat(station.getId()).isEqualTo("ST-001");
    }

    @Test
    void getUnknownStationThrowsNotFound() {
        GetStationRequest request = GetStationRequest.newBuilder().setStationId("ST-999").build();

        assertThatThrownBy(() -> stub.getStation(request))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> assertThat(((StatusRuntimeException) e).getStatus().getCode())
                        .isEqualTo(Status.Code.NOT_FOUND));
    }

    @ParameterizedTest
    @CsvSource({
            "174.9, WARNING_LEVEL_NORMAL, 30",
            "175.0, WARNING_LEVEL_WARNING, 20",
            "199.9, WARNING_LEVEL_WARNING, 10",
            "200.0, WARNING_LEVEL_CRITICAL, 0"
    })
    void waterLevelsReturnCorrectWarningLevel(double waterLevel, WarningLevel warningLevel, int secondsBeforeNow) {
        Measurement measurement = measurement("ST-001", NOW.minusSeconds(secondsBeforeNow), waterLevel);
        measurementRepository.saveMeasurement(measurement);

        GetStationRequest request = GetStationRequest.newBuilder().setStationId("ST-001").build();
        Station station = stub.getStation(request);

        assertThat(station.getCurrentWarningLevel()).isEqualTo(warningLevel);
    }

    @Test
    void subscriptionReceivesCorrectMeasurements() {
        measurementRepository.saveMeasurement(measurement("ST-002", NOW.minusSeconds(10), 140));

        SubscribeMeasurementsRequest request = SubscribeMeasurementsRequest.newBuilder().setStationId("ST-002").build();
        Iterator<com.example.floodmonitor.grpc.v1.Measurement> stream = stub.withDeadlineAfter(5, TimeUnit.SECONDS).subscribeMeasurements(request);

        for (int i = 0; i < 3; i++) {
            Measurement measurement = measurement("ST-002", NOW, 150 + i);
            measurementPublisher.announce(measurement);
        }

        for (int i = 0; i < 3; i++) {
            assertThat(stream.hasNext()).isTrue();
            assertThat(stream.next().getWaterLevel()).isEqualTo(150 + i);
        }
    }

    private static Measurement measurement(String stationId, Instant timestamp, double waterLevel) {
        return new Measurement(stationId, timestamp, waterLevel, 100.0, 0.0, 15.0, 80.0,
                StationStatus.ONLINE, com.example.floodmonitor.model.WarningLevel.NORMAL);
    }
}
