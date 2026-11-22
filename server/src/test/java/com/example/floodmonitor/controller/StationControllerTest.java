package com.example.floodmonitor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "simulation.enabled=false")
@AutoConfigureMockMvc
class StationControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void getAllStationsAsJson() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(5));
    }

    @Test
    void getAllStationsAsXML() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(xpath("count(/stations/station)").number(5.0))
                .andExpect(xpath("/stations/@totalCount").string("5"));
    }

    @Test
    void getExistingStationReturnsStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ST-001"));
    }

    @Test
    void getUnknownStationReturns404()  throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/stations/ST-999"));
    }

    @Test
    void unsupportedAcceptHeaderReturns406() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept("text/csv"))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.status").value(406))
                .andExpect(jsonPath("$.error").value("Not Acceptable"))
                .andExpect(jsonPath("$.path").value("/api/v1/stations"));

    }
}
