package com.energy.model;

public class DailyConsumption {

    private final String date;
    private final double totalKwh;

    public DailyConsumption(String date, double totalKwh) {
        this.date = date;
        this.totalKwh = totalKwh;
    }

    public String getDate() { return date; }
    public double getTotalKwh() { return totalKwh; }
}