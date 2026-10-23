package com.energy.model;

public class Prediction {

    private int id;
    private String predictionDate;
    private String predictionPeriod;
    private double predictedConsumption;
    private Double actualConsumption;
    private String algorithm;
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPredictionDate() { return predictionDate; }
    public void setPredictionDate(String predictionDate) { this.predictionDate = predictionDate; }

    public String getPredictionPeriod() { return predictionPeriod; }
    public void setPredictionPeriod(String predictionPeriod) { this.predictionPeriod = predictionPeriod; }

    public double getPredictedConsumption() { return predictedConsumption; }
    public void setPredictedConsumption(double predictedConsumption) { this.predictedConsumption = predictedConsumption; }

    public Double getActualConsumption() { return actualConsumption; }
    public void setActualConsumption(Double actualConsumption) { this.actualConsumption = actualConsumption; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /** Accuracy % = 100 - |actual - predicted| / actual * 100, clamped at 0. Null if no actual value recorded yet. */
    public Double getAccuracyPercent() {
        if (actualConsumption == null || actualConsumption == 0) return null;
        double errorPct = Math.abs(actualConsumption - predictedConsumption) / actualConsumption * 100.0;
        return Math.max(0, 100 - errorPct);
    }
}