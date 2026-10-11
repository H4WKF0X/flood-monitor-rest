package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StationResponse;
import com.example.floodmonitor.model.Station;

import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.StationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/stations")
    public StationListResponse getStations(@RequestParam(name = "river", required = false) String riverFilter,
                                           @RequestParam(name = "isActive", required = false) Boolean isActiveFilter,
                                           @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return stationService.getStations(riverFilter, isActiveFilter, warningLevelFilter);
    }

    @GetMapping("/stations/{id}")
    public StationResponse getStationById(@PathVariable String id) {
        return stationService.getStationById(id);
    }

    @PostMapping(value="/stations",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public ResponseEntity<Station> postStation(@RequestBody Station station) {
        stationService.addStation(station);
        URI location = URI.create("/api/v1/stations/" + station.id());
        return ResponseEntity.created(location).body(station);
    }
}
