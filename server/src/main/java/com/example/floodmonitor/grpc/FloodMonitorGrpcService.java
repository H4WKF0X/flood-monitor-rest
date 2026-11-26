package com.example.floodmonitor.grpc;

import com.example.floodmonitor.exception.InvalidRequestException;
import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.grpc.mapper.CommonMapper;
import com.example.floodmonitor.grpc.mapper.MeasurementMapper;
import com.example.floodmonitor.grpc.mapper.StationMapper;
import com.example.floodmonitor.grpc.v1.*;
import com.example.floodmonitor.model.StationWithWarningLevel;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.service.StationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FloodMonitorGrpcService extends FloodMonitorServiceGrpc.FloodMonitorServiceImplBase {
    private final StationService stationService;
    private final MeasurementService measurementService;

    public FloodMonitorGrpcService(StationService stationService, MeasurementService measurementService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
    }

    @Override
    public void listMeasurements(ListMeasurementsRequest request, StreamObserver<ListMeasurementsResponse> responseObserver) {
        String stationId = request.getStationId();

        if (stationId.isBlank()) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(new InvalidRequestException("stationId is required")));
            return;
        }

        Instant start_time = request.hasStartTime() ? CommonMapper.toDomain(request.getStartTime()) : null;
        Instant end_time = request.hasEndTime() ? CommonMapper.toDomain(request.getEndTime()) : null;

        int limit = request.hasLimit() ? request.getLimit() : 20160;

        WarningLevel warningLevelFilter;

        try {
            warningLevelFilter = request.hasWarningLevel() ? CommonMapper.toDomain(request.getWarningLevel()) : null;
        } catch (InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        List<Measurement> measurements;

        try {
            measurements = measurementService.getMeasurements(stationId, start_time, end_time, limit, warningLevelFilter);
        } catch (StationNotFoundException | InvalidRequestException e) {
            responseObserver.onError(getCorrespondingStatusRuntimeException(e));
            return;
        }

        List<com.example.floodmonitor.grpc.v1.Measurement> grpcMeasurements = measurements.stream().map(MeasurementMapper::toProto).toList();

        responseObserver.onNext(ListMeasurementsResponse.newBuilder().addAllMeasurements(grpcMeasurements).setTotalCount(grpcMeasurements.size()).build());
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
            responseObserver.onError(getCorrespondingStatusRuntimeException(new InvalidRequestException("stationId is required")));
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

    private StatusRuntimeException getCorrespondingStatusRuntimeException(RuntimeException e) {
        if (e instanceof InvalidRequestException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
        }  else if (e instanceof StationNotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
        }
        return Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException();
    }
}
