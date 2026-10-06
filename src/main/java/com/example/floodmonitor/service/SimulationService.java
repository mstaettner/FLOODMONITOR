package com.example.floodmonitor.service;

import com.example.floodmonitor.StationStatus;
import com.example.floodmonitor.WarningLevel;
import com.example.floodmonitor.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SimulationService {

    private final List<Station> stations = new ArrayList<>();
    private final Random random = new Random();

    public SimulationService() {
        initStations();
    }

    private void initStations() {
        Location loc1 = new Location(48.2082, 16.3738);
        Station vienna = new Station("1", "Station Vienna", "Donau", loc1, 150.0, 300.0, 450.0, true);

        Location loc2 = new Location(48.3069, 14.2858);
        Station linz = new Station("2", "Station Linz", "Donau", loc2, 120.0, 250.0, 380.0, true);

        stations.add(vienna);
        stations.add(linz);

        // Ersten Initial-Messwert erzeugen
        for (Station s : stations) {
            generateNextMeasurement(s);
        }
    }

    @Scheduled(fixedRateString = "${simulation.interval:10000}")
    public void runSimulationCycle() {
        for (Station station : stations) {
            if (!station.isActive()) {
                continue;
            }

            List<Measurement> measurements = station.getMeasurements();
            if (!measurements.isEmpty()) {
                Measurement last = measurements.get(measurements.size() - 1);
                if (last.getStatus() == StationStatus.OFFLINE) {
                    continue;
                }
            }

            generateNextMeasurement(station);
        }
    }

    private void generateNextMeasurement(Station station) {
        List<Measurement> history = station.getMeasurements();
        Measurement prev = history.isEmpty() ? null : history.get(history.size() - 1);

        double lastLevel = (prev != null) ? prev.getWaterLevel() : station.getNormalWaterLevel();
        double lastBattery = (prev != null) ? prev.getBatteryLevel() : 100.0;
        double lastTemp = (prev != null) ? prev.getTemperature() : 15.0;

        double rainfall = Math.round((random.nextDouble() * 15.0) * 10.0) / 10.0;
        double temperature = Math.round((lastTemp + (random.nextDouble() - 0.5)) * 10.0) / 10.0;

        double delta = (rainfall > 3.0) ? (rainfall * 0.8) : ((random.nextDouble() - 0.55) * 2.0);
        double currentWaterLevel = Math.max(0.0, Math.round((lastLevel + delta) * 100.0) / 100.0);

        double levelInMeters = currentWaterLevel / 100.0;
        double flowRate = Math.round((5.0 * Math.pow(levelInMeters, 1.5)) * 100.0) / 100.0;

        double batteryLevel = Math.max(0.0, Math.round((lastBattery - 0.2) * 10.0) / 10.0);

        StationStatus status = StationStatus.ONLINE;
        if (batteryLevel < 10.0) {
            status = StationStatus.MAINTENANCE;
        }

        // --- WARNSTUFE ZENTRAL BERECHNEN ---
        WarningLevel warningLevel = calculateWarningLevel(station, currentWaterLevel, prev, rainfall, status);

        Measurement newMeasurement = new Measurement(
                LocalDateTime.now(),
                currentWaterLevel,
                flowRate,
                rainfall,
                temperature,
                batteryLevel,
                status,
                warningLevel
        );

        station.getMeasurements().add(newMeasurement);
    }

    /**
     * Serverseitige Berechnung der Warnstufe basierend auf Schwellenwerten,
     * Anstiegsgeschwindigkeit und Niederschlag.
     */
    public WarningLevel calculateWarningLevel(Station station, Double currentWaterLevel, Measurement previousMeasurement, double rainfall, StationStatus status) {
        // Regel 1: Keine gültige Messung, inaktiv oder OFFLINE -> UNKNOWN
        if (currentWaterLevel == null || status == StationStatus.OFFLINE || !station.isActive()) {
            return WarningLevel.UNKNOWN;
        }

        // Regel 2: Kritischer Wasserstand -> CRITICAL
        if (currentWaterLevel >= station.getCriticalWaterLevel()) {
            return WarningLevel.CRITICAL;
        }

        // Regel 3: Anstiegsgeschwindigkeit & Starkregen (Frühwarnung)
        if (previousMeasurement != null) {
            double waterRise = currentWaterLevel - previousMeasurement.getWaterLevel();
            // Wenn der Pegel rasch ansteigt (> 15cm pro Intervall) ODER extremer Regen fällt (> 10mm/h),
            // wird bereits ab dem Warnwert direkt CRITICAL ausgelöst.
            if ((waterRise > 15.0 || rainfall > 10.0) && currentWaterLevel >= station.getWarningWaterLevel()) {
                return WarningLevel.CRITICAL;
            }
        }

        // Regel 4: Warnwasserstand erreicht -> WARNING
        if (currentWaterLevel >= station.getWarningWaterLevel()) {
            return WarningLevel.WARNING;
        }

        // Regel 5: Standardfall -> NORMAL
        return WarningLevel.NORMAL;
    }

    public List<Station> getStations() {
        return stations;
    }

    public Optional<Station> getStationById(String id) {
        return stations.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Station addStation(Station station) {
        if (station.getId() == null || station.getId().isBlank()) {
            station.setId(String.valueOf(stations.size() + 1));
        }

        // Falls der Client eine Messung mitgeschickt hat: Warnstufe serverseitig erzwingen/berechnen
        if (station.getMeasurements() != null && !station.getMeasurements().isEmpty()) {
            Measurement last = station.getMeasurements().get(station.getMeasurements().size() - 1);

            // Überschreibt jeden vom Client vorgegebenen Wert mit der echten Backend-Berechnung
            WarningLevel calculated = calculateWarningLevel(
                    station,
                    last.getWaterLevel(),
                    null,
                    last.getRainfall(),
                    last.getStatus()
            );
            last.setWarningLevel(calculated);
        }

        stations.add(station);
        return station;
    }
}