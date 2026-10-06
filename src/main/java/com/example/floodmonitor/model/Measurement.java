package com.example.floodmonitor.model;

import com.example.floodmonitor.StationStatus;
import com.example.floodmonitor.WarningLevel;

import java.time.LocalDateTime;

public class Measurement {
    private LocalDateTime timestamp;
    private double waterLevel; // in cm
    private double flowRate;   // in m³/s
    private double rainfall;   // in mm/h
    private double temperature;// in °C
    private double batteryLevel; // in %
    private StationStatus status;
    private WarningLevel warningLevel;

    public Measurement() {}

    public Measurement(LocalDateTime timestamp, double waterLevel, double flowRate,
                       double rainfall, double temperature, double batteryLevel,
                       StationStatus status, WarningLevel warningLevel) {
        this.timestamp = timestamp;
        this.waterLevel = waterLevel;
        this.flowRate = flowRate;
        this.rainfall = rainfall;
        this.temperature = temperature;
        this.batteryLevel = batteryLevel;
        this.status = status;
        this.warningLevel = warningLevel;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getWaterLevel() { return waterLevel; }
    public void setWaterLevel(double waterLevel) { this.waterLevel = waterLevel; }

    public double getFlowRate() { return flowRate; }
    public void setFlowRate(double flowRate) { this.flowRate = flowRate; }

    public double getRainfall() { return rainfall; }
    public void setRainfall(double rainfall) { this.rainfall = rainfall; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(double batteryLevel) { this.batteryLevel = batteryLevel; }

    public StationStatus getStatus() { return status; }
    public void setStatus(StationStatus status) { this.status = status; }

    public WarningLevel getWarningLevel() { return warningLevel; }
    public void setWarningLevel(WarningLevel warningLevel) { this.warningLevel = warningLevel; }
}