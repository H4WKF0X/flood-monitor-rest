package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.AlertListResponse;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.AlertService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    public AlertListResponse getAlerts(@RequestParam(name = "river", required = false) String riverFilter,
                                       @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return alertService.getAlerts(riverFilter, warningLevelFilter);
    }
}
