package com.energy.model;

public class CategoryConsumption {

    private final String category;
    private final double totalKwh;

    public CategoryConsumption(String category, double totalKwh) {
        this.category = category;
        this.totalKwh = totalKwh;
    }

    public String getCategory() { return category; }
    public double getTotalKwh() { return totalKwh; }
}