package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.AlertListResponse;
import com.example.floodmonitor.dto.ApiError;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
@Tag(name = "Alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    @Operation(summary = "List active alerts", description = "Active, non-offline stations at WARNING, CRITICAL or UNKNOWN, most severe first.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Invalid parameter value", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public AlertListResponse getAlerts(@Parameter(description = "Case-insensitive")
                                       @RequestParam(name = "river", required = false) String riverFilter,
                                       @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return alertService.getAlerts(riverFilter, warningLevelFilter);
    }
}
