package com.example.floodmonitor.controller;

import com.example.floodmonitor.FixedClockConfig;
import com.example.floodmonitor.model.*;
import com.example.floodmonitor.repository.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static com.example.floodmonitor.FixedClockConfig.NOW;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "simulation.enabled=false")
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
class MeasurementControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Test
    void getLatestMeasurementReturnsNewest() throws Exception {
        measurementRepository.saveMeasurement(measurement("ST-001", NOW.minusSeconds(120), 160.0));
        measurementRepository.saveMeasurement(measurement("ST-001", NOW.minusSeconds(180), 140.0));

        mockMvc.perform(get("/api/v1/stations/ST-001/measurements/latest").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId").value("ST-001"))
                .andExpect(jsonPath("$.timestamp").value(NOW.minusSeconds(120).toString()))
                .andExpect(jsonPath("$.waterLevel").value(160.0));
    }

    @Test
    void filterByTimeRangeReturnsOnlyMeasurementsInRange() throws Exception {
        measurementRepository.saveMeasurement(measurement("ST-002", NOW.minusSeconds(18), 160.0));
        measurementRepository.saveMeasurement(measurement("ST-002", NOW.minusSeconds(6), 150.0));
        measurementRepository.saveMeasurement(measurement("ST-002", NOW.minusSeconds(12), 160.0));

        String from = NOW.minusSeconds(20).toString();
        String to = NOW.minusSeconds(10).toString();

        mockMvc.perform(get("/api/v1/stations/ST-002/measurements")
                        .param("from", from)
                        .param("to", to)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.measurements[*].waterLevel", everyItem(is(160.0))));
    }

    @Test
    void fromAfterToReturns400() throws Exception {
        String from = NOW.minusSeconds(10).toString();
        String to = NOW.minusSeconds(20).toString();

        mockMvc.perform(get("/api/v1/stations/ST-003/measurements")
                        .param("from", from)
                        .param("to", to)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parameter 'from' must not be after 'to'"))
                .andExpect(jsonPath("$.path").value("/api/v1/stations/ST-003/measurements"));
    }
    @Test
    void limitAboveMaximumReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-003/measurements")
                        .param("limit", "30000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parameter 'limit' must be between 1 and 20160"))
                .andExpect(jsonPath("$.path").value("/api/v1/stations/ST-003/measurements"));
    }

    private static Measurement measurement(String stationId, Instant timestamp, double waterLevel) {
        return new Measurement(stationId, timestamp, waterLevel, 100.0, 0.0, 15.0, 80.0,
                StationStatus.ONLINE, WarningLevel.NORMAL);
    }
}
