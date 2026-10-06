package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimulationService {

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

    private final List<Station> stations = new ArrayList<>();

    private final Random random = new Random();

    public SimulationService() {
        generateStations();
    }

    private void generateStations() {
        for (int i = 0; i < STATION_NAMES.length; i++) {
            String stationName = STATION_NAMES[i];
            String id = "ST-00" + (i+1);
            String river = STATION_RIVERS.get(stationName);
            Location location = STATION_LOCATIONS.get(stationName);

            // normal: 150–399 cm, warning: normal + 100-199cm, critical: warning + 80-149cm
            int normalWaterLevel = random.nextInt(150, 400);
            int warningWaterLevel = normalWaterLevel + random.nextInt(100, 200);
            int criticalWaterLevel = warningWaterLevel + random.nextInt(80, 150);

            boolean isActive = true;
            stations.add(new Station(id, stationName, river, location, normalWaterLevel, warningWaterLevel, criticalWaterLevel, isActive));
        }
    }

    public void addStation(Station station) {
        this.stations.add(station);
    }

    public List<Station> getStations() {
        return Collections.unmodifiableList(stations);
    }

    public Optional<Station> getStationById(String id) {
        return getStations().stream()
            .filter(s -> id.equals(s.id()))
            .findFirst();
    }
}
