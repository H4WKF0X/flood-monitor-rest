package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SimulationService {

    private static final String[] STATION_NAMES = {
        "Station Vienna", "Station Linz", "Station Salzburg",
        "Station Graz", "Station Innsbruck"
    };

    private final List<Station> stations = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger();

    private final Random random = new Random();

    public SimulationService() {
        generateStations();
    }

    private void generateStations() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (String stationName : STATION_NAMES) {
            Station station = new Station();
            station.setId(String.valueOf(counter.incrementAndGet()));
            station.setStationName(stationName);
            station.setTimestamp(timestamp);
            station.setWaterLevel(Math.round((0.5 + random.nextDouble() * 4.5) * 100.0) / 100.0);
            // waterFlow (discharge) grows with waterLevel
            station.setWaterFlow(Math.round((station.getWaterLevel() * 300 + random.nextDouble() * 100) * 10.0) / 10.0);
            station.setWaterTemperature(Math.round((4.0 + random.nextDouble() * 16.0) * 10.0) / 10.0);
            station.setUnit("m");
            stations.add(station);
        }
    }

    public void addStation(Station station) {
        station.setId(String.valueOf(counter.incrementAndGet()));
        this.stations.add(station);
    }

    public List<Station> getStations() {
        return Collections.unmodifiableList(stations);
    }

    public Optional<Station> getStationById(String id) {
        return getStations().stream()
            .filter(s -> s.getId().equals(id))
            .findFirst();
    }
}
