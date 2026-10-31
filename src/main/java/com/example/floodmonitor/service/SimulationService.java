package com.example.floodmonitor.service;

import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.model.*;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final StationRepository stationRepository;
    private final Random random = new Random();
    @Value("${simulation.enabled:true}")
    private boolean simulationEnabled = true;

    public SimulationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        initStations();
    }

    private void initStations() {
        Location loc1 = new Location(48.2082, 16.3738);
        Station vienna = new Station("1", "Station Vienna", "Donau", loc1, 150.0, 300.0, 450.0, true);

        Location loc2 = new Location(48.3069, 14.2858);
        Station linz = new Station("2", "Station Linz", "Donau", loc2, 120.0, 250.0, 380.0, true);

        Location loc3 = new Location(47.8095, 13.0550);
        Station salzburg = new Station("3", "Station Salzburg", "Salzach", loc3, 90.0, 190.0, 290.0, true);

        stationRepository.save(vienna);
        stationRepository.save(linz);
        stationRepository.save(salzburg);

        // Ersten Initial-Messwert erzeugen
        for (Station s : stationRepository.findAll()) {
            generateNextMeasurement(s);
        }
    }

    @Scheduled(fixedRateString = "${simulation.interval:10000}")
    public void runSimulationCycle() {
        if (!simulationEnabled) {
            return;
        }
        for (Station station : stationRepository.findAll()) {
            if (!station.isActive()) {
                continue;
            }

            List<Measurement> measurements = station.getMeasurements();
            if (!measurements.isEmpty()) {
                Measurement last = measurements.get(measurements.size() - 1);
                if (last.getStatus() != StationStatus.ONLINE) {
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
        // Korrigiert: Multiplikationszeichen '*' hinzugefügt
        double flowRate = Math.round((5.0 * Math.pow(levelInMeters, 1.5)) * 100.0) / 100.0;

        double batteryLevel = Math.max(0.0, Math.round((lastBattery - 0.2) * 10.0) / 10.0);

        StationStatus status = StationStatus.ONLINE;
        if (batteryLevel < 10.0) {
            status = StationStatus.MAINTENANCE;
        }

        // --- WARNSTUFE ZENTRAL BERECHNEN ---
        WarningLevel warningLevel = calculateWarningLevel(station, currentWaterLevel, prev, rainfall, status);

        Measurement newMeasurement = new Measurement(
                LocalDateTime.now(ZoneOffset.UTC),
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
        return stationRepository.findAll();
    }

    public List<Station> getStations(String river, Boolean active, WarningLevel warningLevel) {
        return stationRepository.findAll().stream()
                .filter(station -> river == null || river.isBlank() || station.getRiver().equalsIgnoreCase(river))
                .filter(station -> active == null || station.isActive() == active)
                .filter(station -> warningLevel == null || latestMeasurement(station)
                        .map(measurement -> measurement.getWarningLevel() == warningLevel).orElse(warningLevel == WarningLevel.UNKNOWN))
                .collect(Collectors.toList());
    }

    public Optional<Station> getStationById(String id) {
        return stationRepository.findById(id);
    }

    public List<Measurement> getMeasurements(String id, LocalDateTime from, LocalDateTime to,
                                             Integer limit, WarningLevel warningLevel) {
        Station station = getStationById(id)
                .orElseThrow(() -> new com.example.floodmonitor.exception.StationNotFoundException("Station " + id + " wurde nicht gefunden"));
        return station.getMeasurements().stream()
                .filter(measurement -> from == null || !measurement.getTimestamp().isBefore(from))
                .filter(measurement -> to == null || !measurement.getTimestamp().isAfter(to))
                .filter(measurement -> warningLevel == null || measurement.getWarningLevel() == warningLevel)
                .sorted(Comparator.comparing(Measurement::getTimestamp).reversed())
                .limit(limit == null ? Long.MAX_VALUE : limit)
                .collect(Collectors.toList());
    }

    public Optional<Measurement> getLatestMeasurement(String id) {
        Station station = getStationById(id)
                .orElseThrow(() -> new com.example.floodmonitor.exception.StationNotFoundException("Station " + id + " wurde nicht gefunden"));
        return latestMeasurement(station);
    }

    public MeasurementStatistics getStatistics(String id) {
        Station station = getStationById(id)
                .orElseThrow(() -> new com.example.floodmonitor.exception.StationNotFoundException("Station " + id + " wurde nicht gefunden"));
        List<Measurement> measurements = station.getMeasurements();
        if (measurements.isEmpty()) {
            throw new com.example.floodmonitor.exception.StationNotFoundException("Für Station " + id + " existieren noch keine Messungen");
        }
        DoubleSummaryStatistics waterLevels = measurements.stream().mapToDouble(Measurement::getWaterLevel).summaryStatistics();
        double averageFlow = measurements.stream().mapToDouble(Measurement::getFlowRate).average().orElse(0.0);
        double totalRainfall = measurements.stream().mapToDouble(Measurement::getRainfall).sum();
        long warningCount = measurements.stream().filter(m -> m.getWarningLevel() == WarningLevel.WARNING
                || m.getWarningLevel() == WarningLevel.CRITICAL).count();
        long criticalCount = measurements.stream().filter(m -> m.getWarningLevel() == WarningLevel.CRITICAL).count();
        return new MeasurementStatistics(waterLevels.getMin(), waterLevels.getMax(), waterLevels.getAverage(),
                averageFlow, totalRainfall, measurements.size(), warningCount, criticalCount);
    }

    public List<StationAlert> getAlerts(String river) {
        return stationRepository.findAll().stream()
                .filter(station -> river == null || river.isBlank()
                        || (station.getRiver() != null && station.getRiver().equalsIgnoreCase(river)))
                .map(station -> {
                    Measurement current = latestMeasurement(station).orElse(null);
                    WarningLevel level = WarningLevel.UNKNOWN;
                    if (current != null && current.getTimestamp() != null && current.getStatus() != null) {
                        level = calculateWarningLevel(station, current.getWaterLevel(), null,
                                current.getRainfall(), current.getStatus());
                    }
                    return new StationAlert(station.getId(), station.getName(), station.getRiver(), current, level);
                })
                .collect(Collectors.toList());
    }

    private Optional<Measurement> latestMeasurement(Station station) {
        return station.getMeasurements().stream().max(Comparator.comparing(Measurement::getTimestamp,
                Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    public Station addStation(Station station) {
        if (station.getId() == null || station.getId().isBlank()) {
            station.setId(String.valueOf(stationRepository.findAll().size() + 1));
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

        return stationRepository.save(station);
    }
}
