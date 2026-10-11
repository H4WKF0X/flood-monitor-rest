package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.service.MeasurementService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class MeasurementController {
    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping("/stations/{id}/measurements/latest")
    public ResponseEntity<MeasurementResponse> getLatestMeasurement(@PathVariable String id) {
        return measurementService.getLatestMeasurement(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/{id}/measurements")
    public ResponseEntity<List<MeasurementResponse>> getMeasurements(@PathVariable String id) {
        return measurementService.getMeasurements(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/{id}/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(@PathVariable String id) {
        return measurementService.getStatistics(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
