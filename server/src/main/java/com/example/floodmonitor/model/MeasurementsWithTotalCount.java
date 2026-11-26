package com.example.floodmonitor.model;

import java.util.List;

public record MeasurementsWithTotalCount(List<Measurement> measurement, int totalCount) {
}
