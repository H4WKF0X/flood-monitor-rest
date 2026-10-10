package com.example.floodmonitor.config;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StationSeeder {
    private static final String[] STATION_NAMES = {
            "Station Vienna", "Station Linz", "Station Salzburg",
            "Station Graz", "Station Innsbruck"
    };

    private static final Map<String, String> STATION_RIVERS = Map.of(
            "Station Vienna",    "Donau",
            "Station Linz",      "Donau",
            "Station Salzburg",  "Salzach",
            "Station Graz",      "Mur",
            "Station Innsbruck", "Inn"
    );

    private static final Map<String, Location> STATION_LOCATIONS = Map.of(
            "Station Vienna",    new Location(48.21, 16.37),
            "Station Linz",      new Location(48.31, 14.29),
            "Station Salzburg",  new Location(47.80, 13.04),
            "Station Graz",      new Location(47.07, 15.44),
            "Station Innsbruck", new Location(47.27, 11.39)
    );

    private static final Map<String, Double> RIVER_WARNING_LEVELS = Map.of(
            "Donau", 1.75, "Salzach", 1.95, "Mur", 1.85, "Inn", 1.90);

    private static final Map<String, Double> RIVER_CRITICAL_LEVELS = Map.of(
            "Donau", 2.00, "Salzach", 2.70, "Mur", 2.55, "Inn", 2.65);

    private final StationRepository stationRepository;

    public StationSeeder(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        generateStations();
    }

    private void generateStations() {
        for (int i = 0; i < STATION_NAMES.length; i++) {
            String stationName = STATION_NAMES[i];
            String id = String.format("ST-%03d", i + 1);
            String river = STATION_RIVERS.get(stationName);
            Location location = STATION_LOCATIONS.get(stationName);

            double normalWaterLevel = 150.0;
            double warningWaterLevel = RIVER_WARNING_LEVELS.get(STATION_RIVERS.get(stationName));
            double criticalWaterLevel = RIVER_CRITICAL_LEVELS.get(STATION_RIVERS.get(stationName));

            boolean isActive = true;
            stationRepository.create(new Station(id, stationName, river, location, normalWaterLevel, warningWaterLevel, criticalWaterLevel, isActive));
        }
    }
}
