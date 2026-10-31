# Cheatsheet für das Abgabegespräch

## 1. Hauptmerkmale von JSON

JSON ist ein leichtgewichtiges Textformat mit Objekten aus Schlüssel-Wert-Paaren und Arrays. Unterstützte Werte sind Strings, Zahlen, Booleans, `null`, Objekte und Arrays. Es wird direkt von JavaScript verarbeitet und ist kompakt. Im Projekt werden die Model-Objekte durch Spring/Jackson als JSON serialisiert, zum Beispiel in `StationController.getStations()`.

## 2. Hauptmerkmale von XML

XML ist eine textbasierte, hierarchische Auszeichnungssprache mit expliziten Start- und End-Tags. Es kann Attribute, Namespaces und eigene Elementnamen verwenden. XML ist selbstbeschreibend und gut validierbar, aber gegenüber JSON meist umfangreicher. Jackson XML serialisiert hier `Measurement`; der Elementname ist in `Measurement.java` festgelegt.

## 3. Wie wählt die API JSON oder XML?

Der Client sendet den gewünschten Medientyp im HTTP-Header `Accept`, etwa `application/json` oder `application/xml`. Spring wählt den passenden Jackson-Converter aus. Die Routen deklarieren beide Formate bei `produces` in `StationController`; Jackson XML ist in `build.gradle` eingebunden. Ohne Formatwunsch liefert Spring standardmäßig JSON. Ein nicht unterstützter Medientyp wird zentral mit 406 beantwortet.

## 4. REST und seine Verwendung

REST strukturiert die Schnittstelle um Ressourcen und eindeutige URLs. HTTP-Methoden haben einheitliche Bedeutungen, Requests sind stateless, und Ressourcen können über verschiedene Repräsentationen wie JSON und XML übertragen werden. Das erleichtert die Nutzung durch unterschiedliche Clients und Programmiersprachen. Beispiele sind `/api/v1/stations` und `/api/v1/stations/{id}/measurements` im `StationController`.

## 5. GET, POST, PUT und PATCH

- `GET` liest eine Ressource und soll keine Änderung auslösen.
- `POST` sendet Daten zum Anlegen einer Ressource oder zum Auslösen einer Verarbeitung.
- `PUT` ersetzt eine Ressource vollständig und ist bei gleichem Request idempotent.
- `PATCH` ändert ausgewählte Felder einer bestehenden Ressource.

Im Projekt sind die Lesezugriffe `GET`; `POST /api/v1/stations` legt eine Station an.

## 6. Was ändert sich bei einem neuen Request-Pfad?

Die Route wird im `StationController` ergänzt. Geschäftslogik kommt in `SimulationService`, Datenzugriff in `StationRepository`; falls nötig werden Model/DTO und Validierung ergänzt. Danach werden Fehlerbehandlung, OpenAPI-Dokumentation, Tests und gegebenenfalls der Client aktualisiert. Beispielhafte Zuständigkeiten sind in den gleichnamigen Paketen `controller`, `service`, `repository`, `model` und `exception` zu sehen.

## 7. OpenAPI und Swagger

OpenAPI ist eine herstellerneutrale Spezifikation zur maschinenlesbaren Beschreibung einer HTTP-API. Swagger bezeichnet heute vor allem Werkzeuge, die OpenAPI verwenden, etwa Swagger UI zum Anzeigen und Ausprobieren der API. Im Projekt liefern Springdoc und die Annotationen im `StationController` die OpenAPI-Beschreibung; Swagger UI stellt sie unter `/swagger-ui.html` dar.

## 8. Fehler im Beispiel-Endpunkt

```java
@GetMapping("/stations/add")
public String createStation(@RequestParam String name, @RequestParam double warning) {
    stationService.save(new Station(name, warning));
    return "Station successfully created!";
}
```

Der Endpunkt verändert den Serverzustand, verwendet aber `GET`. Das verletzt die übliche und erwartete GET-Semantik und kann durch Browser, Caches oder Crawler unbeabsichtigt ausgelöst werden. Für das Anlegen sollte `POST /stations` mit `@RequestBody` und einem passenden `201 Created` verwendet werden. Im Projekt ist das Muster in `StationController.createStation()` umgesetzt.

## Quellstellen

- Controller und Routen: `src/main/java/com/example/floodmonitor/controller/StationController.java`
- Geschäftslogik und Simulation: `src/main/java/com/example/floodmonitor/service/SimulationService.java`
- Datenzugriff: `src/main/java/com/example/floodmonitor/repository/StationRepository.java`
- Datenmodell und XML-Root: `src/main/java/com/example/floodmonitor/model/Measurement.java`
- Fehlerantworten: `src/main/java/com/example/floodmonitor/exception/GlobalExceptionHandler.java`
- API-Abhängigkeiten: `build.gradle`
