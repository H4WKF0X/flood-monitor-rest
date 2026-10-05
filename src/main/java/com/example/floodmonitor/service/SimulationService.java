package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SimulationService {

    private static final String[] STATION_NAMES = {
        "Station Vienna", "Station Linz", "Station Salzburg",
        "Station Graz", "Station Innsbruck"
    };

    private final Random random = new Random();

    public List<Station> generateStations() {
        List<Station> stations = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (int i = 0; i < STATION_NAMES.length; i++) {
            Station station = new Station();
            station.setId(String.valueOf(i + 1));
            station.setStationName(STATION_NAMES[i]);
            station.setTimestamp(timestamp);
            station.setWaterLevel(Math.round((0.5 + random.nextDouble() * 4.5) * 100.0) / 100.0);
            station.setWaterTemperature(Math.round((4.0 + random.nextDouble() * 16.0) * 10.0) / 10.0);
            station.setUnit("m");
            stations.add(station);
        }

        return stations;
    }

    public Optional<Station> getStationById(String id) {
        return generateStations().stream()
            .filter(s -> s.getId().equals(id))
            .findFirst();
    }
}
