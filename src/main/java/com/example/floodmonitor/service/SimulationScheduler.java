package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true")
public class SimulationScheduler {
    private volatile boolean prefillDone = false;

    private final Clock clock;
    private final SimulationService simulationService;
    private final StationRepository stationRepository;

    private final Duration warmUp;
    private final Duration history;

    public SimulationScheduler(Clock clock, SimulationService simulationService,  StationRepository stationRepository,
                               @Value("${simulation.prefillWarmUp}") Duration warmUp, @Value("${simulation.prefillHistory}") Duration history) {
        this.clock = clock;
        this.simulationService = simulationService;
        this.stationRepository = stationRepository;

        this.warmUp = warmUp;
        this.history = history;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void prefillOnStartup() {
        List<Station> stations = stationRepository.findAll();
        simulationService.prefill(warmUp, history, Instant.now(clock).truncatedTo(ChronoUnit.SECONDS), stations);
        prefillDone = true;

    }

    @Scheduled(fixedRateString = "${simulation.interval}")
    public void tick() {
        if (!prefillDone) {
            return;
        }
        List<Station> stations = stationRepository.findAll();
        simulationService.step(Instant.now(clock).truncatedTo(ChronoUnit.SECONDS), stations);
    }
}
