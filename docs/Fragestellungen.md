# Fragestellungen Floodmonitor Martin Stättner, 4DHIT
## Was sind die Hauptmerkmale von JSON?
JSON steht für JavaScript Object Notation und ist ein Klartextformat zum Speichern und Austauschen von strukturierten Daten.
JSON ist der Standard für Rest-APIs. Jeder Browser und Sprache kann es nativ verarbeiten.
Daten werden in JSON als Schlüssel-Wertepaare gespeichert, wobei der Schlüssel ein String in doppelten Anführungszeichen sein muss. Nachgestellte Kommas machen die Schlüssel ungültig, Kommentare gibt es in JSON nicht.
Folgende sechs Datenrypen gibt es in JSON: Zeichenfolgen, Zahlen, Booleans, null, Arrays und Objekte

## Was sind die Hauptmerkmale von XML?
XML steht für eXtensible Markup Language und wurde zur Datenspeicherung und Transport entwickelt.
XML weißt keine native Unterstützung von Sprachen und Browser auf und benötigt XML Parser. (z.B. DOMParser)
Daten werden in Tags gespeichert. wodruch auch benutzerdefinierte Tags, sprich "Datentypen", erstellt werden können.

## Wie unterscheidet die Applikation in diesem Beispiel, ob die Rückgabewerte in JSON oder XML zurückgegeben werden?
Die Application entscheidet dies über den HTTP-Header. Standardmäßig wird JSON verwendet (nativ unterstützt, kein Jackson XML).

## Was zeichnet ein REST Interface aus und warum wird es heute sehr oft als standardisierte Schnittstelle verwendet?
Ein REST Interface zeichnet sich durch ein einheitliches Interface (HTTP-Methoden), der Trennung zwischen Client und Server, dem Fehlen von Zustand (jede Anfrage hat alle Infos) und der Möglichkeit, Daten in verschieden Formaten (z.B. JSON & XML) übertargen zu können, aus.
REST Interfaces wird heute oft als standardisierte Schnittstelle verwendet, da es Sprach- und Plattformunabhängig ist, einfach integrierbar ist, skalierbar ist, breit unterstützt wird, und REST optimalst für HTTP-Standards, sowie Endpunkte geeignet ist.

## Was ist der Unterschied zwischen den HTTP Methoden GET, POST, PUT und PATCH?
- GET: Lesen bzw. holen von Daten
- POST: Erstellen bzw. senden neuer Daten
- PUT: Bereits bestehende Daten ersetzen (kann wie POST verwendet werden, nicht empfohlen)
- PATCH: Einen Teil bereits bestehender Daten ändern

## Ich will eine neue Request Methode / Pfad in meinem REST Interface zur Verfügung stellen, welche Stellen des Quellcodes muss ich verändern?
Im StationContoller selbst muss der Pfad erstellt werden. Gegebenenfalls trifft man Anpassungen/Ergänzungen im SimulationSerivce und kümmert sich um die Daten im Repository. Eventuell passt man Fehlercodes (Exceptions) im ExceptionHandler an.
Der Kernpunkt ist der Controller. Die anderen Klassen ruft man dann aus diesem Controller aus auf, sofern man bestimmte benötigt. 

## Kann man OpenAPI und Swagger vergleichen? Wenn ja, dann vergleichen Sie beide Themengebiete. Wenn nein, dann begründe die Antwort.
Man kann OpenAPI und Swagger nur bedingt vergleichen. OpenAPI spezifiziert die API in YAML oder JSON, ist ein Open Standard und wird durch die OpenAPI Initiative und der Linux Foundatin gewartet, während Swagger ein Tool zum Bauen, Dokumentieren, Testen, Visualisieren und Code generieren für APIs, die OpenAPI folgen, ist. Swagger selbst in ein Markenname und wird duch SmartBear gewartet.

### Wo liegt im folgenden Code-Snippet der Fehler? Begründe die Antwort.
```
@GetMapping("/stations/add")
public String createStation(@RequestParam String name, @RequestParam double warning) {
stationService.save(new Station(name, warning));
return "Station successfully created!";
}
```
Hier legt der Fehler darin, dass die Route auf add endet, was ein Hinweiß auf POST ist, die Methode createStation heißt, ebenfalls ein Hiniweiß auf POST und im Methodenkörper eine neue Station erstellt wird, allerdings als Mapping ein GET und kein POST-Mapping verwendent wird.
Somit ist also das Mapping falsch und es muss ein POST statt ein GET-Mapping verwendet werden.

## Quellen:
https://contabo.com/blog/de/json-ein-vollstaendiger-guide-zu-syntax-und-datenformat-2026/ <br>
https://contabo.com/blog/de/xml-vs-json/ <br>
https://www.knguru.de/blog/was-ist-eine-rest-api <br>
https://www.w3schools.com/tags/ref_httpmethods.asp <br>
https://www.digitalapi.ai/blogs/swagger-vs-openapi#what-is-the-difference-between-swagger-and-openapi