package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

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
    public MeasurementResponse getLatestMeasurement(@PathVariable String id) {
        return measurementService.getLatestMeasurement(id);
    }

    @GetMapping("/stations/{id}/measurements")
    public MeasurementListResponse getMeasurements(@PathVariable String id,
                                                                   @RequestParam(name = "from", required = false) Instant fromFilter,
                                                                   @RequestParam(name = "to", required = false) Instant toFilter,
                                                                   @RequestParam(name = "limit", required = false, defaultValue = "${api.measurements.defaultLimit}") Integer limit,
                                                                   @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return measurementService.getMeasurements(id, fromFilter, toFilter, limit, warningLevelFilter);
    }

    @GetMapping("/stations/{id}/statistics")
    public StatisticsResponse getStatistics(@PathVariable String id,
                                                            @RequestParam(name = "from", required = false) Instant fromFilter,
                                                            @RequestParam(name = "to", required = false) Instant toFilter) {
        return measurementService.getStatistics(id, fromFilter, toFilter);
    }
}
