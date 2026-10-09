package com.example.floodmonitor.rain;

import com.example.floodmonitor.model.Location;

public class RainCell {
    private static final double KM_PER_DEGREE = 111.2;

    private Location location;
    private final double radiusKm;
    private final double speedNorthKmH;
    private final double speedEastKmH;
    private final double intensityMmPerHour;
    private double msLeft;

    /**
     * Creates a rain cell
     *
     * @param location center of the cell
     * @param radiusKm radius of the rain area in km
     * @param speedNorthKmH speed towards north (negative meaning south) in km/h
     * @param speedEastKmH speed towards east (negative meaning west) in km/h
     * @param intensityMmPerHour rain intensity inside the cell in mm/h
     * @param msLeft lifetime of the cell in milliseconds
     */
    public RainCell(Location location, double radiusKm, double speedNorthKmH, double speedEastKmH, double intensityMmPerHour, double msLeft) {
        this.location = location;
        this.radiusKm = radiusKm;
        this.speedNorthKmH = speedNorthKmH;
        this.speedEastKmH = speedEastKmH;
        this.intensityMmPerHour = intensityMmPerHour;
        this.msLeft = msLeft;
    }

    /**
     * Moves the cell for the given time and reduces its remaining lifetime.
     *
     * @param ms time that has passed in milliseconds
     */
    public void advance(double ms) {
        double hours = ms / 3_600_000.0;
        double latChange = speedNorthKmH * hours / KM_PER_DEGREE;
        double longChange = speedEastKmH * hours / kmPerDegreeLon();
        location = new Location(location.latitude() + latChange, location.longitude() + longChange);
        this.msLeft -= ms;
    }

    /**
     * Checks whether a location is inside the cell, meaning it is raining there.
     *
     * @param stationLocation the location to check
     * @return true if the location is closer to the center than the radius
     */
    public boolean covers(Location stationLocation) {
        double northKm = (stationLocation.latitude() - location.latitude()) * KM_PER_DEGREE;
        double eastKm = (stationLocation.longitude() - location.longitude()) * kmPerDegreeLon();
        return northKm * northKm + eastKm * eastKm < radiusKm * radiusKm;
    }

    /**
     * @return true if the lifetime of the cell has run out
     */
    public boolean isDead() {
        return msLeft <= 0;
    }

    /**
     * @return the rain intensity inside the cell in mm/h
     */
    public double getIntensityMmPerHour() {
        return intensityMmPerHour;
    }

    /**
     * @return the current center of the cell
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the length of one degree of longitude at the cell's current latitude.
     *
     * @return km per degree of longitude
     */
    private double kmPerDegreeLon() {
        return KM_PER_DEGREE * Math.cos(Math.toRadians(location.latitude()));
    }
}
