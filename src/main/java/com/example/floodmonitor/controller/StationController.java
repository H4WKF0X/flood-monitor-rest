package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.ApiError;
import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StationResponse;
import com.example.floodmonitor.model.Station;

import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
@Tag(name = "Stations")
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/stations")
    @Operation(summary = "List stations")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Invalid parameter value", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public StationListResponse getStations(@Parameter(description = "Case-insensitive")
                                           @RequestParam(name = "river", required = false) String riverFilter,
                                           @RequestParam(name = "isActive", required = false) Boolean isActiveFilter,
                                           @RequestParam(name = "warningLevel", required = false) WarningLevel warningLevelFilter) {
        return stationService.getStations(riverFilter, isActiveFilter, warningLevelFilter);
    }

    @GetMapping("/stations/{id}")
    @Operation(summary = "Get a station")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Station not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public StationResponse getStationById(@PathVariable String id) {
        return stationService.getStationById(id);
    }

    @PostMapping(value="/stations",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(summary = "Create a station")
    @ApiResponse(responseCode = "201", description = "Station created")
    @ApiResponse(responseCode = "400", description = "Malformed request body", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Station already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "415", description = "Unsupported body format", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Station> postStation(@RequestBody Station station) {
        stationService.addStation(station);
        URI location = URI.create("/api/v1/stations/" + station.id());
        return ResponseEntity.created(location).body(station);
    }
}
