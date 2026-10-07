package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Repository
public class MeasurementRepository {
    private final Map<String, ConcurrentSkipListSet<Measurement>> stationMeasurements = new ConcurrentHashMap<>();
    private static final Comparator<Measurement> comp = Comparator.comparing(Measurement::timestamp).reversed();

    // 4 hours (at intervals of 10 seconds)
    private static final int MAX_HISTORY = 1440;

    public void saveMeasurement(Measurement measurement) {
        stationMeasurements.putIfAbsent(measurement.stationId(), new ConcurrentSkipListSet<>(comp));
        ConcurrentSkipListSet<Measurement> measurementSet = stationMeasurements.get(measurement.stationId());

        if (measurementSet.add(measurement)) {
            int excess = measurementSet.size() - MAX_HISTORY;
            for (int i = 0; i < excess; i++) {
                measurementSet.pollLast();
            }
        }
    }

    public Optional<Measurement> getLastMeasurement(String stationId) {
        ConcurrentSkipListSet<Measurement> temp = stationMeasurements.get(stationId);
        return (temp == null || temp.isEmpty()) ? Optional.empty() : Optional.of(temp.first());
    }

    public List<Measurement> getMeasurementHistory(String stationId) {
        ConcurrentSkipListSet<Measurement> temp = stationMeasurements.get(stationId);
        return (temp == null) ? List.of() : List.copyOf(temp);
    }
}
