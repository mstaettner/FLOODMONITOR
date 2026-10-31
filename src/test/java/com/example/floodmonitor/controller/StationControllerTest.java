package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.model.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StationController.class)
public class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimulationService simulationService;

    private Station testStation;

    @BeforeEach
    void setUp() {
        // Testdaten vorbereiten (deterministisch ohne echten Zufall)
        testStation = new Station("1", "Test Station", "Donau", new Location(48.0, 16.0), 100.0, 200.0, 300.0, true);
        Measurement m1 = new Measurement(LocalDateTime.now().minusHours(1), 120.0, 8.0, 0.0, 14.0, 95.0, StationStatus.ONLINE, WarningLevel.NORMAL);
        Measurement m2 = new Measurement(LocalDateTime.now(), 150.0, 10.0, 0.0, 15.0, 90.0, StationStatus.ONLINE, WarningLevel.NORMAL);
        testStation.getMeasurements().add(m1);
        testStation.getMeasurements().add(m2); // m2 ist der aktuellste Wert
    }

    // 1. Abruf aller Stationen als JSON
    @Test
    void testGetStationsJson() throws Exception {
        Mockito.when(simulationService.getStations()).thenReturn(List.of(testStation));

        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Station")));
    }

    // 2. Abruf aller Stationen als XML
    @Test
    void testGetStationsXml() throws Exception {
        Mockito.when(simulationService.getStations()).thenReturn(List.of(testStation));

        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    // 3. Abruf einer existierenden Station
    @Test
    void testGetStationByIdExisting() throws Exception {
        Mockito.when(simulationService.getStationById("1")).thenReturn(Optional.of(testStation));

        mockMvc.perform(get("/api/v1/stations/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")));
    }

    // 4. Abruf einer unbekannten Station (Erwartet 404 via GlobalExceptionHandler)
    @Test
    void testGetStationByIdNotFound() throws Exception {
        Mockito.when(simulationService.getStationById("999"))
                .thenThrow(new StationNotFoundException("Station 999 wurde nicht gefunden"));

        mockMvc.perform(get("/api/v1/stations/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    // 5. Abruf des aktuellsten Messwerts
    @Test
    void testGetLatestMeasurement() throws Exception {
        Mockito.when(simulationService.getStationById("1")).thenReturn(Optional.of(testStation));

        // Annahme: Der Endpunkt liefert die Station, deren letztes Measurement geprüft wird
        mockMvc.perform(get("/api/v1/stations/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements", hasSize(2)))
                .andExpect(jsonPath("$.measurements[1].waterLevel", is(150.0)));
    }

    // 6. Filterung nach gültigem Zeitraum (from / to)
    @Test
    void testFilterByTimeRangeValid() throws Exception {
        Mockito.when(simulationService.getStationById("1")).thenReturn(Optional.of(testStation));

        mockMvc.perform(get("/api/v1/stations/1")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // 7. Ungültiger Zeitraum (z. B. 'from' liegt nach 'to') -> Erwartet 400 Bad Request
    @Test
    void testFilterByTimeRangeInvalid() throws Exception {
        Mockito.when(simulationService.getStationById("1")).thenReturn(Optional.of(testStation));

        // Wenn dein Controller/Service prüft, ob 'from' nach 'to' liegt und eine IllegalArgumentException wirft:
        // (Falls du diese Logik im Controller/Service hast, greift hier der Bad-Request-Handler)
        mockMvc.perform(get("/api/v1/stations/1")
                        .param("from", "2026-12-31T23:59:59")
                        .param("to", "2026-01-01T00:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // 8, 9, 10. Tests für Warnstufen (NORMAL, WARNING, CRITICAL Logik)
    @Test
    void testWarningLevelCalculationLogic() {
        SimulationService realService = new SimulationService();
        Station s = new Station("1", "S1", "Fluss", new Location(0, 0), 100.0, 200.0, 300.0, true);

        // 8. Warnstufe NORMAL (< 200)
        WarningLevel wNormal = realService.calculateWarningLevel(s, 150.0, null, 0.0, StationStatus.ONLINE);
        org.junit.jupiter.api.Assertions.assertEquals(WarningLevel.NORMAL, wNormal);

        // 9. Warnstufe WARNING (>= 200 und < 300)
        WarningLevel wWarning = realService.calculateWarningLevel(s, 250.0, null, 0.0, StationStatus.ONLINE);
        org.junit.jupiter.api.Assertions.assertEquals(WarningLevel.WARNING, wWarning);

        // 10. Warnstufe CRITICAL (>= 300)
        WarningLevel wCritical = realService.calculateWarningLevel(s, 350.0, null, 0.0, StationStatus.ONLINE);
        org.junit.jupiter.api.Assertions.assertEquals(WarningLevel.CRITICAL, wCritical);
    }

    // 11. Nicht unterstütztes Format (z. B. Client schickt Accept: application/pdf)
    @Test
    void testNotAcceptableFormat() throws Exception {
        Mockito.when(simulationService.getStations()).thenReturn(List.of(testStation));

        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isNotAcceptable()); // HTTP 406
    }

    // 12. Maximales Limit (z. B. ?limit=1)
    @Test
    void testMaxLimitParameter() throws Exception {
        Mockito.when(simulationService.getStationById("1")).thenReturn(Optional.of(testStation));

        mockMvc.perform(get("/api/v1/stations/1")
                        .param("limit", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}