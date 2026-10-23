package com.energy.model;
public class Appliance {

    private int id;
    private String name;
    private String category;
    private double powerWatts;
    private int quantity;
    private double dailyHours;
    private int daysPerMonth;
    private String efficiencyRating;
    private String location;
    private boolean sample;
    private String createdAt;

    public Appliance() {
    }

    public Appliance(int id, String name, String category, double powerWatts, int quantity,
                      double dailyHours, int daysPerMonth, String efficiencyRating,
                      String location, boolean sample, String createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.powerWatts = powerWatts;
        this.quantity = quantity;
        this.dailyHours = dailyHours;
        this.daysPerMonth = daysPerMonth;
        this.efficiencyRating = efficiencyRating;
        this.location = location;
        this.sample = sample;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPowerWatts() {
        return powerWatts;
    }

    public void setPowerWatts(double powerWatts) {
        this.powerWatts = powerWatts;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(double dailyHours) {
        this.dailyHours = dailyHours;
    }

    public int getDaysPerMonth() {
        return daysPerMonth;
    }

    public void setDaysPerMonth(int daysPerMonth) {
        this.daysPerMonth = daysPerMonth;
    }

    public String getEfficiencyRating() {
        return efficiencyRating;
    }

    public void setEfficiencyRating(String efficiencyRating) {
        this.efficiencyRating = efficiencyRating;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** Daily Energy (kWh) = (Power_Watts * Quantity * Daily_Hours) / 1000 */
    public double getDailyConsumptionKwh() {
        return (powerWatts * quantity * dailyHours) / 1000.0;
    }

    /** Monthly Energy (kWh) = Daily Energy * Days_Used_Per_Month */
    public double getMonthlyConsumptionKwh() {
        return getDailyConsumptionKwh() * daysPerMonth;
    }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
