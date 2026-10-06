package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.service.SimulationService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class StationController {

    private final SimulationService simulationService;

    public StationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/stations")
    public List<Station> getStations() {
        return simulationService.getStations();
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return simulationService.getStationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/count")
    public int getCount() {
        return simulationService.getStations().size();
    }

    @PostMapping(value="/stations",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public ResponseEntity<Station> postStation(@RequestBody Station station) {
        simulationService.addStation(station);
        URI location = URI.create("/api/v1/stations/" + station.getId());
        return ResponseEntity.created(location).body(station);
    }
}
