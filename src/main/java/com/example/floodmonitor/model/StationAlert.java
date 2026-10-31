package com.example.floodmonitor.model;

public class StationAlert {
    private String stationId;
    private String stationName;
    private String river;
    private Measurement measurement;
    private WarningLevel warningLevel;

    public StationAlert() { }

    public StationAlert(String stationId, String stationName, String river,
                        Measurement measurement, WarningLevel warningLevel) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.river = river;
        this.measurement = measurement;
        this.warningLevel = warningLevel;
    }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getRiver() { return river; }
    public void setRiver(String river) { this.river = river; }
    public Measurement getMeasurement() { return measurement; }
    public void setMeasurement(Measurement measurement) { this.measurement = measurement; }
    public WarningLevel getWarningLevel() { return warningLevel; }
    public void setWarningLevel(WarningLevel warningLevel) { this.warningLevel = warningLevel; }
}
