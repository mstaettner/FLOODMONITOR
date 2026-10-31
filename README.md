# Flood Monitor REST API

Spring-Boot-Anwendung zur Simulation von Hochwassermessstationen mit REST-API und Webclient. Java 17 und Gradle Wrapper werden benötigt.

## Start

```powershell
./gradlew.bat bootRun
```

Die Anwendung läuft unter <http://localhost:8080>, der Client unter <http://localhost:8080/>. Das Simulationsintervall lässt sich in `src/main/resources/application.properties` mit `simulation.interval` in Millisekunden konfigurieren. Mit `simulation.enabled=false` kann die Simulation für Tests oder Vorführungen angehalten werden.

## Architektur und Datenmodell

- `model`: Station, Location, Measurement und Status-/Warnstufen-Enums
- `repository`: In-Memory-Zugriff auf Stationen und Messungen
- `service`: Simulation, Filter, Statistik und Warnstufenberechnung
- `controller`: REST-Routen unter `/api/v1`
- `exception`: zentrale JSON-/XML-Fehlerantworten
- `src/main/resources/static/index.html`: jQuery-Webclient

Die drei Startstationen liegen an der Donau in Wien und Linz sowie an der Salzach in Salzburg. Messungen verwenden `LocalDateTime` im UTC-Bezug. Wasserstände werden in cm gespeichert; Durchfluss wird aus dem Wasserstand abgeleitet. Regen über 3 mm/h erhöht den Pegel stärker, ansonsten gibt es kleine Schwankungen. Pegel ab Warnschwelle sind `WARNING`, ab kritischer Schwelle `CRITICAL`; bei Starkregen über 10 mm/h oder einem Anstieg über 15 cm pro Intervall wird ab der Warnschwelle `CRITICAL` ausgelöst. Der Akku sinkt pro Intervall um 0,2 Prozentpunkte; unter 10 % wechselt die Station in `MAINTENANCE` und wird danach nicht weiter simuliert. Inaktive Stationen werden übersprungen.

## API

Alle GET-Routen unterstützen `Accept: application/json` (Standard) und `Accept: application/xml`.

| Methode | Route | Zweck |
|---|---|---|
| GET | `/api/v1/stations` | Stationen; optionale Filter `river`, `isActive`, `warningLevel` |
| GET | `/api/v1/stations/{id}` | Eine Station |
| GET | `/api/v1/stations/count` | Anzahl der Stationen |
| GET | `/api/v1/stations/{id}/measurements/latest` | Neuester Messwert |
| GET | `/api/v1/stations/{id}/measurements` | Historie; `from`, `to`, `limit` (1–500), `warningLevel` |
| GET | `/api/v1/stations/{id}/measurements/statistics` | Minimum/Maximum/Mittelwerte, Niederschlagssumme und Warnungszahlen |
| GET | `/api/v1/alerts` | Stationen, deren neueste Messung `WARNING` oder `CRITICAL` ist; optionaler Filter `river` |
| POST | `/api/v1/stations` | Station als JSON oder XML anlegen |

Beispiel:

```text
GET /api/v1/stations/1/measurements?from=2026-10-01T08:00:00Z&to=2026-10-31T12:00:00Z&limit=100&warningLevel=WARNING
```

Beispiel für den neuesten Messwert (`GET /api/v1/stations/1/measurements/latest`):

```http
Accept: application/json
```

```json
{
  "timestamp": "2026-10-31T12:00:00",
  "waterLevel": 318.4,
  "flowRate": 28.4,
  "rainfall": 12.7,
  "temperature": 14.3,
  "batteryLevel": 87.0,
  "status": "ONLINE",
  "warningLevel": "WARNING"
}
```

Mit `Accept: application/xml` enthält dieselbe Antwort dieselben Felder:

```xml
<measurement>
  <timestamp>2026-10-31T12:00:00</timestamp>
  <waterLevel>318.4</waterLevel>
  <flowRate>28.4</flowRate>
  <rainfall>12.7</rainfall>
  <temperature>14.3</temperature>
  <batteryLevel>87.0</batteryLevel>
  <status>ONLINE</status>
  <warningLevel>WARNING</warningLevel>
</measurement>
```

Ungültige Datumswerte, `from` nach `to`, unbekannte Warnstufen und Limits außerhalb 1–500 ergeben `400`. Unbekannte Stations-IDs ergeben `404`, nicht unterstützte Accept-Formate `406`. Fehlerantworten enthalten Zeitstempel, Status, Meldung und Pfad; interne Ausnahmen geben keine Stacktraces an den Client zurück. Die API implementiert keine Authentifizierung.

OpenAPI/Swagger UI: <http://localhost:8080/swagger-ui.html>; JSON-Spezifikation: <http://localhost:8080/v3/api-docs>.
Die Antworten auf die Fragestellungen mit Quellverweisen stehen in [docs/Abgabegespraech.md](docs/Abgabegespraech.md).

## Webclient und Abgabeunterlagen

Der Client ruft Stationen, aktuellen Messwert und Historie ab. Er bietet JSON/XML-Umschaltung, Zeitraum-/Warnstufenfilter, Aktualisierung und farbige Warnstufen. Screenshots befinden sich in `docs/screenshots/`. Die verwendeten AI-Prompts sind in [Protokoll.md](Protokoll.md) dokumentiert.

## Tests, Reflexion und bekannte Grenzen

Automatisierte Controller- und Logiktests liegen unter `src/test/java`. Bei der Umsetzung waren die Trennung zwischen Repository und Geschäftslogik, die ISO-8601-Zeitfilter und die parallele JSON/XML-Ausgabe die wichtigsten Arbeitsschritte. Die Messhistorie ist jetzt über eigene Endpunkte abrufbar, damit die Stationsliste nicht unnötig groß wird. Die Daten liegen nur im Speicher und gehen beim Neustart verloren. Die Simulation läuft im selben Prozess; es gibt keine getrennten Stationsprozesse und keine Sicherheitsrollen. Die AI-Prompts und Notizen zum Abgabegespräch befinden sich in `Protokoll.md` und `notes.md`.
