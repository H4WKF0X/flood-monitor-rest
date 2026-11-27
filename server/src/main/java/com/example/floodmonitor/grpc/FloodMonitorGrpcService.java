package com.example.floodmonitor.grpc;

import com.example.floodmonitor.exception.InvalidRequestException;
import com.example.floodmonitor.exception.NoMeasurementForStationException;
import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.grpc.mapper.AlertMapper;
import com.example.floodmonitor.grpc.mapper.CommonMapper;
import com.example.floodmonitor.grpc.mapper.MeasurementMapper;
import com.example.floodmonitor.grpc.mapper.StationMapper;
import com.example.floodmonitor.grpc.v1.*;
import com.example.floodmonitor.model.*;
import com.example.floodmonitor.model.Alert;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.AlertService;
import com.example.floodmonitor.service.MeasurementPublisher;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.service.StationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

@Service
public class FloodMonitorGrpcService extends FloodMonitorServiceGrpc.FloodMonitorServiceImplBase {
    private final StationService stationService;
    private final MeasurementService measurementService;
    private final AlertService alertService;
    private final MeasurementPublisher measurementPublisher;

    private final int defaultLimit;
    public FloodMonitorGrpcService(StationService stationService, MeasurementService measurementService,
                                   AlertService alertService, MeasurementPublisher measurementPublisher,
                                   @Value("${api.measurements.defaultLimit}") int defaultLimit) {
        this.stationService = stationService;
        this.measurementService = measurementService;
        this.alertService = alertService;
        this.defaultLimit = defaultLimit;
        this.measurementPublisher = measurementPublisher;
    }

    @Override
    public void listMeasurements(ListMeasurementsRequest request, StreamObserver<ListMeasurementsResponse> responseObserver) {
        String stationId = request.getStationId();

        if (stationId.isBlank()) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(new InvalidRequestException("station_id is required")));
            return;
        }

        Instant startTime = request.hasStartTime() ? CommonMapper.toDomain(request.getStartTime()) : null;
        Instant endTime = request.hasEndTime() ? CommonMapper.toDomain(request.getEndTime()) : null;

        int limit = request.hasLimit() ? request.getLimit() : defaultLimit;

        WarningLevel warningLevelFilter;

        try {
            warningLevelFilter = request.hasWarningLevel() ? CommonMapper.toDomain(request.getWarningLevel()) : null;
        } catch (InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        MeasurementsWithTotalCount measurements;

        try {
            measurements = measurementService.getMeasurements(stationId, startTime, endTime, limit, warningLevelFilter);
        } catch (StationNotFoundException | InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        List<com.example.floodmonitor.grpc.v1.Measurement> grpcMeasurements = measurements.measurement().stream().map(MeasurementMapper::toProto).toList();

        responseObserver.onNext(ListMeasurementsResponse.newBuilder().addAllMeasurements(grpcMeasurements).setTotalCount(measurements.totalCount()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getLatestMeasurement(GetLatestMeasurementRequest request, StreamObserver<com.example.floodmonitor.grpc.v1.Measurement> responseObserver) {
        String stationId = request.getStationId();

        if (stationId.isBlank()) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(new InvalidRequestException("station_id is required")));
            return;
        }

        Measurement measurement;

        try {
            measurement = measurementService.getLatestMeasurement(stationId);
        } catch (StationNotFoundException | NoMeasurementForStationException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        com.example.floodmonitor.grpc.v1.Measurement grpcMeasurement = MeasurementMapper.toProto(measurement);

        responseObserver.onNext(grpcMeasurement);
        responseObserver.onCompleted();
    }

    @Override
    public void listStations(ListStationsRequest request, StreamObserver<ListStationsResponse> responseObserver) {
        String riverFilter = request.getRiver();
        riverFilter = riverFilter.isBlank() ? null : request.getRiver();

        Boolean isActiveFilter = request.hasIsActive() ? request.getIsActive() : null;

        WarningLevel warningLevelFilter;

        try {
            warningLevelFilter = request.hasWarningLevel() ? CommonMapper.toDomain(request.getWarningLevel()) : null;
        } catch (InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        List<StationWithWarningLevel> stations = stationService.getStations(riverFilter, isActiveFilter, warningLevelFilter);
        List<com.example.floodmonitor.grpc.v1.Station> grpcStations = stations.stream().map(StationMapper::toProto).toList();

        responseObserver.onNext(ListStationsResponse.newBuilder().addAllStations(grpcStations).setTotalCount(grpcStations.size()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getStation(GetStationRequest request, StreamObserver<com.example.floodmonitor.grpc.v1.Station> responseObserver) {
        String stationId = request.getStationId();

        if (stationId.isBlank()) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(new InvalidRequestException("station_id is required")));
            return;
        }

        StationWithWarningLevel station;

        try {
            station = stationService.getStationById(stationId);
        } catch (StationNotFoundException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        com.example.floodmonitor.grpc.v1.Station grpcStation = StationMapper.toProto(station);

        responseObserver.onNext(grpcStation);
        responseObserver.onCompleted();
    }

    @Override
    public void listAlerts(ListAlertsRequest request, StreamObserver<ListAlertsResponse> responseObserver) {
        String riverFilter = request.getRiver();
        riverFilter = riverFilter.isBlank() ? null : request.getRiver();

        WarningLevel warningLevelFilter;

        try {
            warningLevelFilter = request.hasWarningLevel() ? CommonMapper.toDomain(request.getWarningLevel()) : null;
        } catch (InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        List<Alert> alerts = alertService.getAlerts(riverFilter, warningLevelFilter);
        List<com.example.floodmonitor.grpc.v1.Alert> grpcAlerts = alerts.stream().map(AlertMapper::toProto).toList();

        responseObserver.onNext(ListAlertsResponse.newBuilder().addAllAlerts(grpcAlerts).setTotalCount(grpcAlerts.size()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void subscribeMeasurements(SubscribeMeasurementsRequest request, StreamObserver<com.example.floodmonitor.grpc.v1.Measurement> responseObserver) {
        String stationId = request.getStationId();

        if (stationId.isBlank()) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(new InvalidRequestException("station_id is required")));
            return;
        }

        try {
            stationService.getStationById(stationId);
        } catch (StationNotFoundException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        WarningLevel warningLevelFilter;

        try {
            warningLevelFilter = request.hasWarningLevel() ? CommonMapper.toDomain(request.getWarningLevel()) : null;
        } catch (InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        Consumer<Measurement> listener = measurement -> {
            if (measurement.warningLevel() == warningLevelFilter || warningLevelFilter == null) {
                responseObserver.onNext(MeasurementMapper.toProto(measurement));
            }
        };

        measurementPublisher.subscribe(stationId, listener);

        ServerCallStreamObserver<com.example.floodmonitor.grpc.v1.Measurement> serverCallResponseObserver =
                (ServerCallStreamObserver<com.example.floodmonitor.grpc.v1.Measurement>) responseObserver;

        serverCallResponseObserver.setOnCancelHandler(() -> measurementPublisher.unsubscribe(stationId, listener));
    }

    private StatusRuntimeException getCorrespondingStatusRuntimeException(RuntimeException e) {
        if (e instanceof InvalidRequestException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
        } else if (e instanceof StationNotFoundException
                || e instanceof NoMeasurementForStationException) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        }
        return Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException();
    }
}
