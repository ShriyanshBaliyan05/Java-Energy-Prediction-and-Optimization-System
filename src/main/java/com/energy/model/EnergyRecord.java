package com.energy.model;

public class EnergyRecord {

    private int id;
    private String recordDate;
    private String buildingName;
    private Integer applianceId;
    private String applianceName;
    private String category;
    private double consumptionKwh;
    private double operatingHours;
    private Double meterReading;
    private double tariff;
    private String notes;
    private String createdAt;

    public EnergyRecord() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public Integer getApplianceId() {
        return applianceId;
    }

    public void setApplianceId(Integer applianceId) {
        this.applianceId = applianceId;
    }

    public String getApplianceName() {
        return applianceName;
    }

    public void setApplianceName(String applianceName) {
        this.applianceName = applianceName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getConsumptionKwh() {
        return consumptionKwh;
    }

    public void setConsumptionKwh(double consumptionKwh) {
        this.consumptionKwh = consumptionKwh;
    }

    public double getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(double operatingHours) {
        this.operatingHours = operatingHours;
    }

    public Double getMeterReading() {
        return meterReading;
    }

    public void setMeterReading(Double meterReading) {
        this.meterReading = meterReading;
    }

    public double getTariff() {
        return tariff;
    }

    public void setTariff(double tariff) {
        this.tariff = tariff;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** Estimated Cost = Consumption (kWh) * Tariff (per kWh) */
    public double getEstimatedCost() {
        return consumptionKwh * tariff;
    }
}