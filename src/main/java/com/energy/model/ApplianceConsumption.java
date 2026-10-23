package com.energy.model;

public class ApplianceConsumption {

    private final Integer applianceId;
    private final String applianceName;
    private final String category;
    private final double totalKwh;

    public ApplianceConsumption(Integer applianceId, String applianceName, String category, double totalKwh) {
        this.applianceId = applianceId;
        this.applianceName = applianceName;
        this.category = category;
        this.totalKwh = totalKwh;
    }

    public Integer getApplianceId() { return applianceId; }
    public String getApplianceName() { return applianceName; }
    public String getCategory() { return category; }
    public double getTotalKwh() { return totalKwh; }
}