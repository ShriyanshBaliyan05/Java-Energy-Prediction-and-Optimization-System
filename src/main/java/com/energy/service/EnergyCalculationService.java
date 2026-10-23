package com.energy.service;

public class EnergyCalculationService {

    /** Energy (kWh) = (Power_Watts x Quantity x Hours) / 1000 */
    public double calculateDailyKwh(double powerWatts, int quantity, double hours) {
        return (powerWatts * quantity * hours) / 1000.0;
    }

    /** Monthly kWh = Daily kWh x Days_Used_Per_Month */
    public double calculateMonthlyKwh(double dailyKwh, int daysPerMonth) {
        return dailyKwh * daysPerMonth;
    }

    /** Energy Cost = Total kWh x Tariff */
    public double calculateEnergyCost(double totalKwh, double tariff) {
        return totalKwh * tariff;
    }

    /** Savings = Baseline - Optimized */
    public double calculateSavings(double baselineKwh, double optimizedKwh) {
        return baselineKwh - optimizedKwh;
    }

    /** Savings % = (Saved / Baseline) x 100, guarded against division by zero */
    public double calculateSavingsPercent(double baselineKwh, double optimizedKwh) {
        if (baselineKwh <= 0) return 0.0;
        return ((baselineKwh - optimizedKwh) / baselineKwh) * 100.0;
    }
}