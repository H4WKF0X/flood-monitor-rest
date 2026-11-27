package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class MeasurementPublisher {
    private final Map<String, Set<Consumer<Measurement>>> listeners = new ConcurrentHashMap<>();

    public void subscribe(String stationId, Consumer<Measurement> listener) {
        listeners.putIfAbsent(stationId, ConcurrentHashMap.newKeySet());
        listeners.get(stationId).add(listener);
    }

    public void unsubscribe(String stationId, Consumer<Measurement> listener) {
        Set<Consumer<Measurement>> consumers = listeners.get(stationId);
        if (consumers != null) {
            consumers.remove(listener);
        }
    }

    public void announce(Measurement measurement) {
        for (Consumer<Measurement> listener : listeners.getOrDefault(measurement.stationId(), Set.of())) {
            try {
                listener.accept(measurement);
            } catch (RuntimeException e) {
                unsubscribe(measurement.stationId(), listener);
            }
        }
    }
}
