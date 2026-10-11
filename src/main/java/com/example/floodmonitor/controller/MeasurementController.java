package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class MeasurementController {
    private final MeasurementService measurementService;

    private final int maxLimit;

    public MeasurementController(MeasurementService measurementService, @Value("${api.measurements.maxLimit}") int maxLimit) {
        this.measurementService = measurementService;
        this.maxLimit = maxLimit;
    }

    @GetMapping("/stations/{id}/measurements/latest")
    public ResponseEntity<MeasurementResponse> getLatestMeasurement(@PathVariable String id) {
        return measurementService.getLatestMeasurement(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/{id}/measurements")
    public ResponseEntity<MeasurementListResponse> getMeasurements(@PathVariable String id,
                                                                   @RequestParam(name = "from", required = false) Instant fromFilter,
                                                                   @RequestParam(name = "to", required = false) Instant toFilter,
                                                                   @RequestParam(name = "limit", required = false, defaultValue = "${api.measurements.defaultLimit}") Integer limit,
                                                                   @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return measurementService.getMeasurements(id, fromFilter, toFilter, Math.min(limit, maxLimit), warningLevelFilter)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/{id}/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(@PathVariable String id,
                                                            @RequestParam(name = "from", required = false) Instant fromFilter,
                                                            @RequestParam(name = "to", required = false) Instant toFilter) {
        return measurementService.getStatistics(id, fromFilter, toFilter)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
