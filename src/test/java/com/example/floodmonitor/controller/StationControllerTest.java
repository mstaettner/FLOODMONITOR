package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.model.*;
import com.example.floodmonitor.repository.StationRepository;
import com.example.floodmonitor.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StationController.class)
class StationControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private SimulationService simulationService;
    private Station station;
    private Measurement latest;

    @BeforeEach
    void setUp() {
        station = new Station("1", "Test Station", "Donau", new Location(48, 16), 100, 200, 300, true);
        latest = new Measurement(LocalDateTime.parse("2026-10-31T12:00:00"), 150, 10, 0, 15, 90,
                StationStatus.ONLINE, WarningLevel.NORMAL);
        station.getMeasurements().add(latest);
    }

    @Test void getStationsAsJson() throws Exception {
        Mockito.when(simulationService.getStations(null, null, null)).thenReturn(List.of(station));
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Station")));
    }

    @Test void getStationsAsXml() throws Exception {
        Mockito.when(simulationService.getStations(null, null, null)).thenReturn(List.of(station));
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test void getExistingStation() throws Exception {
        Mockito.when(simulationService.getStationById("1")).thenReturn(Optional.of(station));
        mockMvc.perform(get("/api/v1/stations/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is("1")));
    }

    @Test void unknownStationReturns404() throws Exception {
        Mockito.when(simulationService.getStationById("999")).thenThrow(new StationNotFoundException("Station 999 wurde nicht gefunden"));
        mockMvc.perform(get("/api/v1/stations/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status", is(404)));
    }

    @Test void getLatestMeasurement() throws Exception {
        Mockito.when(simulationService.getLatestMeasurement("1")).thenReturn(Optional.of(latest));
        mockMvc.perform(get("/api/v1/stations/1/measurements/latest").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.waterLevel", is(150.0)));
    }

    @Test void filterMeasurementsByTimeRange() throws Exception {
        Mockito.when(simulationService.getMeasurements(eq("1"), any(), any(), eq(100), eq(WarningLevel.WARNING)))
                .thenReturn(List.of(latest));
        mockMvc.perform(get("/api/v1/stations/1/measurements")
                        .param("from", "2026-10-31T08:00:00Z").param("to", "2026-10-31T12:00:00Z")
                        .param("limit", "100").param("warningLevel", "WARNING"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test void invalidTimeRangeReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/1/measurements")
                        .param("from", "2026-12-31T23:59:59").param("to", "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test void invalidDateReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/1/measurements").param("from", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test void invalidWarningLevelReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/1/measurements").param("warningLevel", "DANGER"))
                .andExpect(status().isBadRequest());
    }

    @Test void unsupportedFormatReturns406() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_PDF)).andExpect(status().isNotAcceptable());
    }

    @Test void limitAboveMaximumReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/stations/1/measurements").param("limit", "501"))
                .andExpect(status().isBadRequest());
    }

    @Test void limitAtMaximumIsAccepted() throws Exception {
        Mockito.when(simulationService.getMeasurements("1", null, null, 500, null)).thenReturn(List.of(latest));
        mockMvc.perform(get("/api/v1/stations/1/measurements").param("limit", "500"))
                .andExpect(status().isOk());
    }

    @Test void calculatesWarningLevels() {
        SimulationService service = new SimulationService(new StationRepository());
        Station thresholds = new Station("x", "x", "Donau", new Location(0, 0), 100, 200, 300, true);
        org.junit.jupiter.api.Assertions.assertEquals(WarningLevel.NORMAL,
                service.calculateWarningLevel(thresholds, 150.0, null, 0, StationStatus.ONLINE));
        org.junit.jupiter.api.Assertions.assertEquals(WarningLevel.WARNING,
                service.calculateWarningLevel(thresholds, 250.0, null, 0, StationStatus.ONLINE));
        org.junit.jupiter.api.Assertions.assertEquals(WarningLevel.CRITICAL,
                service.calculateWarningLevel(thresholds, 350.0, null, 0, StationStatus.ONLINE));
    }
}
