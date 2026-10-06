# Protokoll Floodmonitor Martin Stättner, 4DHIT
## Prompts
(Manche Zeichen wie z.B. + werden nicht richtig dargestellt, da diese als reservierte Chrachter in Markdown zählen. (Z.B. ist das + Zeichen ein Aufzählungspunkt))
### 06.10.2026, 14:29 (Gemini 3.6 Flash)
2. Messwerte simulieren
   Implementieren Sie einen Simulator, der für jede aktive Station regelmäßig eine neue Messung erzeugt. Die Werte müssen realistisch sein und logisch zusammenpassen:
   Wasserstände dürfen nicht negativ sein.
   Der Akkustand liegt zwischen 0 und 100 Prozent.
   Starker Niederschlag soll den Wasserstand tendenziell erhöhen.
   Ein höherer Wasserstand soll zu einer höheren Durchflussmenge führen.
   Der Wasserstand soll sich zwischen zwei Messungen nicht völlig zufällig verändern.
   Bei sehr niedrigem Akkustand kann der Status auf MAINTENANCE wechseln.
   Für deaktivierte oder ausgefallene Stationen werden keine neuen Messungen erzeugt. Wie setze ich hier einen sinvollen waterFlow als Double. Hier die anderen Felder: Station station = new Station();
   station.setId(String.valueOf(i + 1));
   station.setStationName(STATION_NAMES[i]);
   station.setTimestamp(timestamp);
   station.setWaterLevel(Math.round((0.5 + random.nextDouble() * 4.5) * 100.0) / 100.0);
   station.setWaterTemperature(Math.round((4.0 + random.nextDouble() * 16.0) * 10.0) / 10.0);
   station.setUnit("m");
   station.setWaterFlow()
   stations.add(station);

### 06.10.2026, 14:32 (Gemini 3.6 Flash)
Es soll so kurz wie bei den anderen Werten sein 

### 06.10.2026, 14:52 (Gemini 3.6 Flash)
Ist dieser Endpunkt so richtig? @GetMapping("/api/v1/stations/count")
public int countStations(){
List<Station> stations = simulationService.generateStations();
int count = 0;
for(Station station : stations){
count++;
}
return count;
}  Hier die Angabe: Add a GET /api/v1/stations/count endpoint that returns the number of stations as a plain integer.

### 06.10.2026, 14:57 (Gemini 3.6 Flash)
package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.service.SimulationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class StationController {

    private final SimulationService simulationService;

    public StationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/stations")
    public List<Station> getStations() {
        return simulationService.generateStations();
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return simulationService.getStationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/stations/count")
    public int countStations(){
        List<Station> stations = simulationService.generateStations();
        return stations.size();
    }
} Ich sehe hier ja nichts am Endpunkt count.  Add a GET /api/v1/stations/count endpoint that returns the number of stations as a plain integer. Sollte ich nicht etwas sehen bzw. wie kann ich das prüfen

### 06.10.2026, 15:29 (Gemini 3.6 Flash)
Add a POST /api/v1/stations endpoint that accepts a JSON body and adds a new station to the (in-memory) list. wie mache ich das?

### 06.10.2026, 15:31 (Gemini 3.6 Flash)
package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SimulationService {

    private static final String[] STATION_NAMES = {
        "Station Vienna", "Station Linz", "Station Salzburg",
        "Station Graz", "Station Innsbruck"
    };

    private final Random random = new Random();

    public List<Station> generateStations() {
        List<Station> stations = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        for (int i = 0; i < STATION_NAMES.length; i++) {
            Station station = new Station();
            station.setId(String.valueOf(i + 1));
            station.setStationName(STATION_NAMES[i]);
            station.setTimestamp(timestamp);
            station.setWaterLevel(Math.round((0.5 + random.nextDouble() * 4.5) * 100.0) / 100.0);
            station.setWaterTemperature(Math.round((4.0 + random.nextDouble() * 16.0) * 10.0) / 10.0);
            station.setUnit("m");
            station.setWaterFlow(Math.round(5.0 * Math.pow(station.getWaterLevel(), 1.5) * 100.0) / 100.0);
            stations.add(station);
        }

        return stations;
    }

    public Optional<Station> getStationById(String id) {
        return generateStations().stream()
            .filter(s -> s.getId().equals(id))
            .findFirst();
    }

    public Station addStation(Station station){
        if(station.getId() == null || station.getId().isBlank()){
            station.setId(String.valueOf(stationStore.size() + 1));
        }
    }
} Der Code soll passend für die Klasse sein


### 06.10.2026, 15:31 (Gemini 3.6 Flash)
{
"id": "1",
"stationName": "Station Vienna",
"timestamp": "2026-10-06T15:50:44.5795004",
"waterLevel": 2.48,
"waterTemperature": 10.8,
"unit": "m",
"waterFlow": 19.53
} Sollte der Test nicht mit waterFlow sein?



### 06.10.2026, 15:38 (Gemini 3.6 Flash)
ich möchte einfach die post methode mit allen feldern einer station testen (für das Add a POST /api/v1/stations endpoint that accepts a JSON body and adds a new station to the (in-memory) list.
)

### 06.10.2026, 15:46 (Gemini 3.6 Flash)
PS C:\Users\marti\Desktop\Personal Projects\DEZSYS_GK71_FLOODMONITOR_REST_INCOMPLETE> curl -X POST http://localhost:8080/api/v1/stations \ -H "Content-Type: application/json" \ -d '{ "id": "6", "stationName": "Station Klagenfurt", "timestamp": "2026-10-06T15:52:00", "waterLevel": 2.48, "waterTemperature": 10.8, "unit": "m", "waterFlow": 19.53 }'
Invoke-WebRequest : Der Parameter "Headers" kann nicht gebunden werden. Der Wert "Content-Type: application/json" vom Typ "System.String" kann nicht in den Typ
"System.Collections.IDictionary" konvertiert werden.
In Zeile:1 Zeichen:57
+ ... st:8080/api/v1/stations \ -H "Content-Type: application/json" \ -d '{ ...
 + CategoryInfo : InvalidArgument: (:) [Invoke-WebRequest], ParameterBindingException
 + FullyQualifiedErrorId : CannotConvertArgumentNoMessage,Microsoft.PowerShell.Commands.InvokeWebRequestCommand





### 06.10.2026, 15:54 (Gemini 3.6 Flash)
PS C:\Users\marti\Desktop\Personal Projects\DEZSYS_GK71_FLOODMONITOR_REST_INCOMPLETE> curl.exe -X POST http://localhost:8080/api/v1/stations -H "Content-Type: application/json" -d "{\"id\": \"6\", \"stationName\": \"Station Klagenfurt\", \"timestamp\": \"2026-10-06T15:52:00\", \"waterLevel\": 2.48, \"waterTemperature\": 10.8, \"unit\": \"m\", \"waterFlow\": 19.53}"
{"timestamp":"2026-10-06T13:55:53.386+00:00","status":400,"error":"Bad Request","path":"/api/v1/stations"}curl: (3) URL rejected: Malformed input to a URL function
curl: (3) unmatched close brace/bracket in position 127:
Klagenfurt\, \timestamp\: \2026-10-06T15:52:00\, \waterLevel\: 2.48, \waterTemperature\: 10.8, \unit\: \m\, \waterFlow\: 19.53}
^


### 06.10.2026, 15:58 (Gemini 3.6 Flash)
PS C:\Users\marti\Desktop\Personal Projects\DEZSYS_GK71_FLOODMONITOR_REST_INCOMPLETE> curl.exe -X POST http://localhost:8080/api/v1/stations -H "Content-Type: application/json" -d '{"id":"6","stationName":"Station Klagenfurt","timestamp":"2026-10-06T15:52:00","waterLevel":2.48,"waterTemperature":10.8,"unit":"m","waterFlow":19.53}'
{"timestamp":"2026-10-06T13:56:35.189+00:00","status":400,"error":"Bad Request","path":"/api/v1/stations"}

### 06.10.2026, 16:02 (Gemini 3.6 Flash)
![prompt post cmd](/docs/screenshots/prompt post cmd.png)

### 06.10.2026, 16:14 (Gemini 3.6 Flash)
Wie mache ich das: Add XML support: return XML instead of JSON when the client sends Accept: application/xml.

### 06.10.2026, 16:19 (Gemini 3.6 Flash)
Integrate Springdoc OpenAPI (Swagger UI) to document your API. Wie mache ich das?


### 06.10.2026, 16:45 (Gemini 3.6 Flash)
package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.service.SimulationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class StationController {

    private final SimulationService simulationService;

    public StationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping(value = "/stations", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Station> getStations() {
        return simulationService.generateStations();
    }

    @GetMapping(value="/stations/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return simulationService.getStationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value="/stations/count", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public int countStations(){
        List<Station> stations = simulationService.generateStations();
        return stations.size();
    }

    @PostMapping(value="/stations", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Station> createStation(@RequestBody Station station){
        Station createdStation = simulationService.addStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }
} Stimmen die Annotation?


### 06.10.2026, 16:50 (Gemini 3.6 Flash)
aber es passt ja so wie ich es habe auch, oder nicht?

### 06.10.2026, 17:14 (Gemini 3.6 Flash)
package com.example.floodmonitor.model;

public class Station {
private String id;
private String stationName;
private String timestamp;
private double waterLevel;
private double waterTemperature;
private String unit;
private double waterFlow;

    public Station() {}

    public Station(String id, String stationName, String timestamp, double waterLevel, double waterTemperature, String unit) {
        this.id = id;
        this.stationName = stationName;
        this.timestamp = timestamp;
        this.waterLevel = waterLevel;
        this.waterTemperature = waterTemperature;
        this.unit = unit;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public double getWaterLevel() { return waterLevel; }
    public void setWaterLevel(double waterLevel) { this.waterLevel = waterLevel; }
    public double getWaterTemperature() { return waterTemperature; }
    public void setWaterTemperature(double waterTemperature) { this.waterTemperature = waterTemperature; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public double getWaterFlow() { return waterFlow; }
    public void setWaterFlow(double waterFlow) { this.waterFlow=waterFlow; }
} Hier ist meine Klasse Station und hier die Angabe. Schreibe mir diese Klasse fertig: Szenario und Datenmodell
Das Hochwasser-Frühwarnsystem besteht aus mehreren Messstationen. Jede Station befindet sich an einem bestimmten Flussabschnitt und erzeugt regelmäßig Messungen.
Eine Messung enthält mindestens folgende Daten:
EigenschaftBeschreibungEinheittimestampZeitpunkt der MessungISO-8601waterLevelaktueller WasserstandcmflowRateDurchflussmengem³/srainfallNiederschlag der letzten Stundemm/htemperatureLufttemperatur°CbatteryLevelAkkustand der Messstation%statustechnischer Zustand der StationTextwarningLevelberechnete HochwasserwarnstufeText
Eine Messstation enthält mindestens id, name, river, location, normalWaterLevel, warningWaterLevel, criticalWaterLevel und isActive. Die geografische Position besteht aus Breitengrad und Längengrad.
Implementieren Sie mindestens die Klassen Station, Location, Measurement, WarningLevel und StationStatus.
Verwenden Sie folgende Enums:
WarningLevel: NORMAL, WARNING, CRITICAL, UNKNOWN
StationStatus: ONLINE, MAINTENANCE, OFFLINE
Zeitpunkte sollen mit einem geeigneten Java-Zeitdatentyp aus java.time gespeichert werden.

### 06.10.2026, 17:48 (Gemini 3.6 Flash)
3. Warnstufe berechnen
   Die Warnstufe wird serverseitig berechnet und darf nicht vom Client vorgegeben werden.
   BedingungWarnstufeWasserstand kleiner als WarnwertNORMALWasserstand ab WarnwertWARNINGWasserstand ab kritischem WertCRITICALKeine aktuelle oder gültige MessungUNKNOWN
   Optional dürfen Niederschlag und die Geschwindigkeit des Wasseranstiegs berücksichtigt werden. Dokumentieren Sie Ihre verwendeten Regeln. Wäre es nicht schlau dies hier zu implementieren: package com.example.floodmonitor.service;

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

    // Geplante Aufgabe, die das konfigurierte Intervall nutzt
    @Scheduled(fixedRateString = "${simulation.interval:10000}")
    public void runSimulationCycle() {
        for (Station station : stations) {
            // Keine neuen Messungen für inaktive Stationen
            if (!station.isActive()) {
                continue;
            }

            // Prüfe den aktuellen Status des letzten Messwerts (falls vorhanden)
            List<Measurement> measurements = station.getMeasurements();
            if (!measurements.isEmpty()) {
                Measurement last = measurements.get(measurements.size() - 1);
                if (last.getStatus() == StationStatus.OFFLINE) {
                    continue; // Keine neuen Messungen bei OFFLINE
                }
            }

            generateNextMeasurement(station);
        }
    }

    private void generateNextMeasurement(Station station) {
        List<Measurement> history = station.getMeasurements();
        Measurement prev = history.isEmpty() ? null : history.get(history.size() - 1);

        // --- 1. Vorherige Werte holen oder Standardwerte setzen ---
        double lastLevel = (prev != null) ? prev.getWaterLevel() : station.getNormalWaterLevel();
        double lastBattery = (prev != null) ? prev.getBatteryLevel() : 100.0;
        double lastTemp = (prev != null) ? prev.getTemperature() : 15.0;

        // --- 2. Niederschlag & Temperatur simulieren ---
        // Zufälliger Niederschlag zwischen 0.0 und 15.0 mm/h
        double rainfall = Math.round((random.nextDouble() * 15.0) * 10.0) / 10.0;
        double temperature = Math.round((lastTemp + (random.nextDouble() - 0.5)) * 10.0) / 10.0;

        // --- 3. Wasserstand berechnen (abhängig vom vorherigen Wert & Niederschlag) ---
        // Regen lässt den Pegel steigen, sonst leichte Schwankung / Abfluss
        double delta = (rainfall > 3.0) ? (rainfall * 0.8) : ((random.nextDouble() - 0.55) * 2.0);
        double currentWaterLevel = Math.max(0.0, Math.round((lastLevel + delta) * 100.0) / 100.0);

        // --- 4. Durchflussmenge berechnen (höherer Wasserstand -> höherer Durchfluss) ---
        // Formel-Beispiel: Q = 5.0 * (m)^1.5  (Wasserstand in Metern umgerechnet)
        double levelInMeters = currentWaterLevel / 100.0;
        double flowRate = Math.round((5.0 * Math.pow(levelInMeters, 1.5)) * 100.0) / 100.0;

        // --- 5. Akkustand reduzieren ---
        double batteryLevel = Math.max(0.0, Math.round((lastBattery - 0.2) * 10.0) / 10.0);

        // --- 6. Status und WarningLevel bestimmen ---
        StationStatus status = StationStatus.ONLINE;
        if (batteryLevel < 10.0) {
            status = StationStatus.MAINTENANCE; // Bei niedrigem Akku auf MAINTENANCE wechseln
        }

        WarningLevel warningLevel = WarningLevel.NORMAL;
        if (currentWaterLevel >= station.getCriticalWaterLevel()) {
            warningLevel = WarningLevel.CRITICAL;
        } else if (currentWaterLevel >= station.getWarningWaterLevel()) {
            warningLevel = WarningLevel.WARNING;
        }

        // --- 7. Messung erstellen und zur Station hinzufügen ---
        Measurement newMeasurement = new Measurement(LocalDateTime.now(), currentWaterLevel, flowRate, rainfall, temperature, batteryLevel, status, warningLevel);

        station.getMeasurements().add(newMeasurement);
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
        stations.add(station);
        return station;
    }
}