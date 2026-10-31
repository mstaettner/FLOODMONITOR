package com.example.floodmonitor.model;

public class MeasurementStatistics {
    private double minWaterLevel;
    private double maxWaterLevel;
    private double averageWaterLevel;
    private double averageFlowRate;
    private double totalRainfall;
    private long measurementCount;
    private long warningCount;
    private long criticalWarningCount;

    public MeasurementStatistics() { }

    public MeasurementStatistics(double minWaterLevel, double maxWaterLevel, double averageWaterLevel,
                                 double averageFlowRate, double totalRainfall, long measurementCount,
                                 long warningCount, long criticalWarningCount) {
        this.minWaterLevel = minWaterLevel;
        this.maxWaterLevel = maxWaterLevel;
        this.averageWaterLevel = averageWaterLevel;
        this.averageFlowRate = averageFlowRate;
        this.totalRainfall = totalRainfall;
        this.measurementCount = measurementCount;
        this.warningCount = warningCount;
        this.criticalWarningCount = criticalWarningCount;
    }

    public double getMinWaterLevel() { return minWaterLevel; }
    public double getMaxWaterLevel() { return maxWaterLevel; }
    public double getAverageWaterLevel() { return averageWaterLevel; }
    public double getAverageFlowRate() { return averageFlowRate; }
    public double getTotalRainfall() { return totalRainfall; }
    public long getMeasurementCount() { return measurementCount; }
    public long getWarningCount() { return warningCount; }
    public long getCriticalWarningCount() { return criticalWarningCount; }
}
