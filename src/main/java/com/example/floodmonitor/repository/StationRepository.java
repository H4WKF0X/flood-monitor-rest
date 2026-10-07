package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StationRepository {
    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    public List<Station> findAll() {
        return List.copyOf(stations.values());
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public boolean update(Station station) {
        return this.stations.replace(station.id(), station) != null;
    }

    public boolean create(Station station) {
        return this.stations.putIfAbsent(station.id(), station) == null;
    }

    public boolean delete(String id) {
        return this.stations.remove(id) != null;
    }
}
