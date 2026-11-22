package com.example.floodmonitor.hydrodynamics;

public class Catchment {
    private static final double MM_KM2_PER_HOUR_TO_M3_PER_SECOND = 1000.0 / 3600.0;

    private final double kHours;
    private final double areaKm2;
    private final double baseFlowM3s;

    private double storageMm = 0;

    /**
     * @param kHours recession time in hours. Larger means floods fade more slowly
     * @param areaKm2 size of the catchment in km²
     * @param baseFlowM3s discharge of the river when the reservoir is empty, in m³/s
     */
    public Catchment(double kHours, double areaKm2, double baseFlowM3s) {
        this.kHours = kHours;
        this.areaKm2 = areaKm2;
        this.baseFlowM3s = baseFlowM3s;
    }

    /**
     * Adds the rain of one tick to the reservoir and lets it drain for the length of that tick.
     * The tick has to be much shorter than the recession time.
     *
     * @param rainMm rain that fell on the catchment during the tick, in mm
     * @param ms length of the tick in milliseconds
     */
    public void advance(double rainMm, double ms) {
        double hours = ms / 3_600_000.0;
        storageMm += rainMm;
        storageMm -= storageMm * hours / kHours;
    }

    /**
     * @return current discharge of the river in m³/s
     */
    public double getDischargeM3s() {
        return baseFlowM3s + storageMm / kHours * areaKm2 * MM_KM2_PER_HOUR_TO_M3_PER_SECOND;
    }
}
