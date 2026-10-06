package com.example.floodmonitor.model;

import java.util.ArrayList;
import java.util.List;

public class Station {
    private String id;
    private String name;
    private String river;
    private Location location;
    private double normalWaterLevel;
    private double warningWaterLevel;
    private double criticalWaterLevel;
    private boolean isActive;

    // Liste der Messungen dieser Station
    private List<Measurement> measurements = new ArrayList<>();

    public Station() {}

    public Station(String id, String name, String river, Location location, double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel, boolean isActive) {
        this.id = id;
        this.name = name;
        this.river = river;
        this.location = location;
        this.normalWaterLevel = normalWaterLevel;
        this.warningWaterLevel = warningWaterLevel;
        this.criticalWaterLevel = criticalWaterLevel;
        this.isActive = isActive;
    }

    // --- Getter und Setter ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Kompatibilitäts-Getter/Setter für bestehenden Code mit stationName
    public String getStationName() { return name; }
    public void setStationName(String stationName) { this.name = stationName; }

    public String getRiver() { return river; }
    public void setRiver(String river) { this.river = river; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public double getNormalWaterLevel() { return normalWaterLevel; }
    public void setNormalWaterLevel(double normalWaterLevel) { this.normalWaterLevel = normalWaterLevel; }

    public double getWarningWaterLevel() { return warningWaterLevel; }
    public void setWarningWaterLevel(double warningWaterLevel) { this.warningWaterLevel = warningWaterLevel; }

    public double getCriticalWaterLevel() { return criticalWaterLevel; }
    public void setCriticalWaterLevel(double criticalWaterLevel) { this.criticalWaterLevel = criticalWaterLevel; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<Measurement> getMeasurements() { return measurements; }
    public void setMeasurements(List<Measurement> measurements) { this.measurements = measurements; }
}