package com.example.floodmonitor.hydrodynamics;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RainField {
    private final Random random;

    private final double intervalMs;
    private final double meanDryMs;
    private final double meanLifetimeMs;
    private final double meanIntensity;

    private final double radiusKm;
    private final double speedKmH;

    private final double minLat;
    private final double maxLat;
    private final double minLon;
    private final double maxLon;

    private RainCell cell;
    private double msUntilNextCell;

    private Map<String, Double> rainAccumulation = Map.of();

    /**
     * Creates the field in the dry state and draws the time until the first cell.
     *
     * @param random source of all random numbers
     * @param intervalMs length of one simulation tick in milliseconds
     * @param meanDryMs average time between two cells in milliseconds
     * @param meanLifetimeMs average lifetime of a cell in milliseconds
     * @param meanIntensity average rain intensity of a cell in mm/h
     * @param radiusKm radius of every cell in km
     * @param speedKmH speed of every cell in km/h
     * @param minLat southern border of the map
     * @param maxLat northern border of the map
     * @param minLon western border of the map
     * @param maxLon eastern border of the map
     */
    public RainField(Random random,
                     @Value("${simulation.interval}") double intervalMs, @Value("${simulation.meanDryMs}") double meanDryMs,
                     @Value("${simulation.meanLifetimeMs}") double meanLifetimeMs, @Value("${simulation.meanIntensity}") double meanIntensity,
                     @Value("${simulation.radiusKm}") double radiusKm, @Value("${simulation.speedKmH}") double speedKmH,
                     @Value("${simulation.minLat}") double minLat, @Value("${simulation.maxLat}") double maxLat,
                     @Value("${simulation.minLon}") double minLon, @Value("${simulation.maxLon}") double maxLon) {
        this.random = random;

        this.intervalMs = intervalMs;
        this.meanDryMs = meanDryMs;
        this.meanLifetimeMs = meanLifetimeMs;
        this.meanIntensity = meanIntensity;

        this.radiusKm = radiusKm;
        this.speedKmH = speedKmH;

        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;

        drawDryGap();
    }

    public void advance(List<Station> currentStations) {
        Map<String, Double> tempAccumulation = new ConcurrentHashMap<>();
        for (Station station : currentStations) {
            tempAccumulation.put(station.id(), 0.0);
        }

        if (cell == null) {
            msUntilNextCell -= intervalMs;
            if (msUntilNextCell <= 0) {
                drawCell();
            }
        } else {
            cell.advance(intervalMs);
            if(cell.isDead() || cellIsOutOfBounds()){
                cell = null;
                drawDryGap();
            }
        }

        if (cell != null) {
            double mm = cell.getIntensityMmPerHour() * intervalMs / 3_600_000.0;
            for (Station station : currentStations) {
                if (cell.covers(station.location())) {
                    tempAccumulation.put(station.id(), tempAccumulation.get(station.id()) + mm);
                }
            }
        }

        rainAccumulation = tempAccumulation;
    }

    /**
     * Returns the rain that fell at a station during the most recent tick.
     *
     * @param station the station to look up
     * @return rain in mm, or 0 if the station was not part of the last tick
     */
    public double getRain(Station station) {
        return rainAccumulation.getOrDefault(station.id(), 0.0);
    }

    /**
     * Creates a new cell at a random point on one of the four map borders, heading into the map.
     * The direction is turned by up to 22.5 degrees to either side.
     * Intensity and lifetime are drawn randomly.
     */
    private void drawCell() {
        double lat;
        double lon;
        double angle;

        switch (random.nextInt(4)) {
            case 0 -> {
                // north border, heading south
                lat = maxLat;
                lon = minLon + random.nextDouble() * (maxLon - minLon);
                angle = 180;
            }
            case 1 -> {
                // south border, heading north
                lat = minLat;
                lon = minLon + random.nextDouble() * (maxLon - minLon);
                angle = 0;
            }
            case 2 -> {
                // east border, heading west
                lat = minLat + random.nextDouble() * (maxLat - minLat);
                lon = maxLon;
                angle = 270;
            }
            default -> {
                // west border, heading east
                lat = minLat + random.nextDouble() * (maxLat - minLat);
                lon = minLon;
                angle = 90;
            }
        }

        // turn up to 22.5 degrees left or right, still heading into the map
        angle += (random.nextDouble() - 0.5) * 45;

        double speedNorthKmH = speedKmH * Math.cos(Math.toRadians(angle));
        double speedEastKmH = speedKmH * Math.sin(Math.toRadians(angle));

        Location location = new Location(lat, lon);

        cell = new RainCell(location, radiusKm, speedNorthKmH, speedEastKmH, drawIntensityMmPerHour(), drawCellLifetime());
    }

    /**
     * Checks whether the center of the current cell has left the map.
     * May only be called while a cell exists.
     *
     * @return true if the center is outside the map borders
     */
    private boolean cellIsOutOfBounds() {
        Location location = cell.getLocation();
        return location.latitude() < minLat || location.latitude() > maxLat
                || location.longitude() < minLon || location.longitude() > maxLon;
    }

    /**
     * Draws a random time until the next cell appears and stores it.
     * The time is exponentially distributed around the average dry time.
     */
    private void drawDryGap() {
        msUntilNextCell = -1 * meanDryMs * Math.log(1 - random.nextDouble());
    }

    /**
     * Draws a random lifetime for a new cell.
     * The time is exponentially distributed around the average lifetime.
     *
     * @return lifetime in milliseconds
     */
    private double drawCellLifetime() {
        return -1 * meanLifetimeMs * Math.log(1 - random.nextDouble());
    }

    /**
     * Draws a random intensity for a new cell.
     * The intensity is exponentially distributed around the average intensity.
     *
     * @return intensity in mm/h
     */
    private double drawIntensityMmPerHour() {
        return -1 * meanIntensity * Math.log(1 - random.nextDouble());
    }
}
