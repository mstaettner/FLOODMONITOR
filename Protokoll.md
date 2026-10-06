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