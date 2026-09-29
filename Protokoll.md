# Protokoll Floodmonitor Martin Stättner, 4DHIT
## Prompts
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