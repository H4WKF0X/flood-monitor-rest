package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.ApiError;
import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.MeasurementResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
@Tag(name = "Measurements")
public class MeasurementController {
    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping("/stations/{id}/measurements/latest")
    @Operation(summary = "Get the latest measurement")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Station not found or no measurements yet", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeasurementResponse getLatestMeasurement(@PathVariable String id) {
        return measurementService.getLatestMeasurement(id);
    }

    @GetMapping("/stations/{id}/measurements")
    @Operation(summary = "List measurements", description = "Newest first. `totalCount` is the number of matches before `limit` is applied.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Invalid parameter value, `from` after `to` or `limit` out of range", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Station not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeasurementListResponse getMeasurements(@PathVariable String id,
                                                                   @Parameter(description = "ISO-8601, inclusive", example = "2026-05-01T00:00:00Z")
                                                                   @RequestParam(name = "from", required = false) Instant fromFilter,
                                                                   @Parameter(description = "ISO-8601, inclusive", example = "2026-05-02T00:00:00Z")
                                                                   @RequestParam(name = "to", required = false) Instant toFilter,
                                                                   @Parameter(description = "1 to ${api.measurements.maxLimit}")
                                                                   @RequestParam(name = "limit", required = false, defaultValue = "${api.measurements.defaultLimit}") Integer limit,
                                                                   @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return measurementService.getMeasurements(id, fromFilter, toFilter, limit, warningLevelFilter);
    }

    @GetMapping("/stations/{id}/statistics")
    @Operation(summary = "Get statistics", description = "Without `from`/`to` the whole stored history is used.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Invalid parameter value or `from` after `to`", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Station not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public StatisticsResponse getStatistics(@PathVariable String id,
                                                            @Parameter(description = "ISO-8601, inclusive", example = "2026-05-01T00:00:00Z")
                                                            @RequestParam(name = "from", required = false) Instant fromFilter,
                                                            @Parameter(description = "ISO-8601, inclusive", example = "2026-05-02T00:00:00Z")
                                                            @RequestParam(name = "to", required = false) Instant toFilter) {
        return measurementService.getStatistics(id, fromFilter, toFilter);
    }
}
