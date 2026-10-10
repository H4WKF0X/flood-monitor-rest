package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true")
public class SimulationScheduler {
    private volatile boolean prefillDone = false;

    private final Clock clock;
    private final SimulationService simulationService;
    private final StationRepository stationRepository;

    public SimulationScheduler(Clock clock, SimulationService simulationService,  StationRepository stationRepository) {
        this.clock = clock;
        this.simulationService = simulationService;
        this.stationRepository = stationRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void prefillOnStartup() {
        // simulationService.prefill(from, to);
        prefillDone = true;
    }

    @Scheduled(fixedRateString = "${simulation.interval}")
    public void tick() {
        if (!prefillDone) {
            return;
        }
        List<Station> stations = stationRepository.findAll();
        simulationService.step(Instant.now(clock), stations);
    }
}
