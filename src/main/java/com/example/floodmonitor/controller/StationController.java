package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.StationNotFoundException;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.model.MeasurementStatistics;
import com.example.floodmonitor.service.SimulationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Station Management", description = "Endpunkte zur Abfrage, Überwachung und manuellen Erstellung von Messstationen")
public class StationController {

    private final SimulationService simulationService;

    public StationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Operation(
            summary = "Alle Stationen abrufen",
            description = "Liefert eine Liste aller registrierten Messstationen. Filter: river, isActive und warningLevel. Unterstützt JSON und XML."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Station.class)),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = Station.class))
                    }),
            @ApiResponse(responseCode = "406", description = "Nicht unterstütztes Format (Ungültiger Accept-Header)", content = @Content)
    })
    @GetMapping(value = "/stations", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Station> getStations(@RequestParam(required = false) String river,
                                     @RequestParam(required = false) Boolean isActive,
                                     @RequestParam(required = false) WarningLevel warningLevel) {
        return simulationService.getStations(river, isActive, warningLevel);
    }

    @Operation(
            summary = "Anzahl der Stationen abrufen",
            description = "Liefert die Gesamtzahl der aktuell registrierten Messstationen."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anzahl erfolgreich ermittelt",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = Integer.class))
                    }),
            @ApiResponse(responseCode = "406", description = "Nicht unterstütztes Format", content = @Content)
    })
    @GetMapping(value = "/stations/count", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public int countStations() {
        return simulationService.getStations().size();
    }

    @Operation(
            summary = "Station nach ID abrufen",
            description = "Liefert die Stammdaten einer Station anhand ihrer ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Station erfolgreich gefunden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Station.class)),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = Station.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Station nicht gefunden (GlobalExceptionHandler greift)", content = @Content),
            @ApiResponse(responseCode = "406", description = "Nicht unterstütztes Format", content = @Content)
    })
    @GetMapping(value = "/stations/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Station> getStationById(
            @Parameter(description = "Eindeutige ID der Station", example = "1") @PathVariable String id) {
        Station station = simulationService.getStationById(id)
                .orElseThrow(() -> new StationNotFoundException("Station " + id + " wurde nicht gefunden"));

        return ResponseEntity.ok(station);
    }

    @Operation(summary = "Aktuellsten Messwert abrufen")
    @GetMapping(value = "/stations/{id}/measurements/latest", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Measurement getLatestMeasurement(@PathVariable String id) {
        return simulationService.getLatestMeasurement(id)
                .orElseThrow(() -> new IllegalArgumentException("Für Station " + id + " existiert noch keine Messung"));
    }

    @Operation(summary = "Messwerthistorie abrufen", description = "Zeitstempel sind ISO-8601; limit liegt zwischen 1 und 500.")
    @GetMapping(value = "/stations/{id}/measurements", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Measurement> getMeasurements(@PathVariable String id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) WarningLevel warningLevel) {
        LocalDateTime fromTime = parseTimestamp(from);
        LocalDateTime toTime = parseTimestamp(to);
        if (fromTime != null && toTime != null && fromTime.isAfter(toTime)) {
            throw new IllegalArgumentException("'from' darf nicht nach 'to' liegen.");
        }
        if (limit != null && (limit < 1 || limit > 500)) {
            throw new IllegalArgumentException("'limit' muss zwischen 1 und 500 liegen.");
        }
        return simulationService.getMeasurements(id, fromTime, toTime, limit, warningLevel);
    }

    @Operation(summary = "Aktuelle Warnungen abrufen")
    @GetMapping(value = "/alerts", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Station> getAlerts(@RequestParam(required = false) WarningLevel warningLevel,
                                   @RequestParam(required = false) String river) {
        return simulationService.getAlerts(river, warningLevel);
    }

    @Operation(summary = "Statistik einer Station abrufen")
    @GetMapping(value = "/stations/{id}/measurements/statistics", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public MeasurementStatistics getStatistics(@PathVariable String id) {
        return simulationService.getStatistics(id);
    }

    private LocalDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (java.time.format.DateTimeParseException localDateTimeError) {
            try {
                return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            } catch (java.time.format.DateTimeParseException offsetError) {
                throw new IllegalArgumentException("Ungültiges ISO-8601-Datum: " + value);
            }
        }
    }

    @Operation(
            summary = "Neue Station manuell erstellen",
            description = "Fügt eine neue Messstation hinzu. Der Request-Body kann im JSON- oder XML-Format übergeben werden."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Station erfolgreich erstellt",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Station.class)),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = Station.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten im Request-Body", content = @Content)
    })
    @PostMapping(value = "/stations",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Station> createStation(
            @Parameter(description = "Stationsdaten als Request-Body") @RequestBody Station station) {
        Station createdStation = simulationService.addStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }
}
