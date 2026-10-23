package com.energy.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.energy.database.ApplianceDAO;
import com.energy.database.SettingsDAO;
import com.energy.model.Appliance;

/**
 * Rule-based energy optimization.
 *
 * Every recommendation traces to a formula:
 *   Daily kWh     = (Power_Watts x Quantity x Hours) / 1000
 *   Monthly kWh   = Daily kWh x Days_Used_Per_Month
 *   Savings kWh   = Baseline - Optimized
 *   Savings %     = (Saved / Baseline) x 100   (0 if baseline is 0)
 *   Savings cost  = Savings kWh x Tariff
 *
 * Assumptions (stated so they can be challenged in a viva):
 *   Relative consumption index by BEE-style star rating:
 *     1 Star = 1.00, 2 Star = 0.90, 3 Star = 0.80, 4 Star = 0.70, 5 Star = 0.60
 *     Upgrading from rating A to B scales monthly kWh by (index_B / index_A).
 *   LED lighting swap: assumed 50% power vs current lamp if Power > 15 W.
 *   Always-on (hours >= 18): suggest switching off 2 hours/day.
 *   High usage (hours > 10, and not always-on): suggest 20% fewer hours.
 *
 * Combined "potential savings" counts only the single highest-saving action
 * per appliance so two overlapping tips are not double-counted.
 */
public class OptimizationService {

    public static final double ALWAYS_ON_HOURS = 18.0;
    public static final double HIGH_USAGE_HOURS = 10.0;
    public static final double HOURS_REDUCTION_FACTOR = 0.80;
    public static final double ALWAYS_ON_HOURS_OFF = 2.0;
    public static final double LIGHTING_LED_POWER_FACTOR = 0.50;
    public static final double LIGHTING_LED_WATT_THRESHOLD = 15.0;

    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final EnergyCalculationService calc = new EnergyCalculationService();

    public static class Recommendation {
        public int applianceId;
        public String applianceName;
        public String category;
        public String actionType;
        public String title;
        public String description;
        public String formula;
        public double baselineMonthlyKwh;
        public double optimizedMonthlyKwh;
        public double savingsKwh;
        public double savingsCost;
        public double savingsPercent;
        public boolean included = true;
    }

    public static class Plan {
        public int applianceCount;
        public double baselineMonthlyKwh;
        public double baselineMonthlyCost;
        public double potentialSavingsKwh;
        public double potentialSavingsCost;
        public double potentialSavingsPercent;
        public double optimizedMonthlyKwh;
        public List<Recommendation> recommendations = new ArrayList<>();
        public String note;
    }

    public Plan buildPlan() {
        List<Appliance> appliances = applianceDAO.getAll();
        double tariff = settingsDAO.getTariff();

        Plan plan = new Plan();
        plan.applianceCount = appliances.size();

        double baseline = 0;
        for (Appliance a : appliances) baseline += a.getMonthlyConsumptionKwh();
        plan.baselineMonthlyKwh = baseline;
        plan.baselineMonthlyCost = calc.calculateEnergyCost(baseline, tariff);

        List<Recommendation> all = new ArrayList<>();
        for (Appliance a : appliances) {
            all.addAll(recommendationsFor(a, tariff));
        }
        all.sort(Comparator.comparingDouble((Recommendation r) -> r.savingsKwh).reversed());
        plan.recommendations = all;

        applySelectionTotals(plan, tariff);
        plan.note = "Potential savings count only the highest-saving selected action per appliance "
                + "(no double-counting). Star-rating and LED factors are documented assumptions, not measurements.";
        return plan;
    }

    /** Recalculate totals after the user ticks/unticks rows. */
    public void applySelectionTotals(Plan plan, double tariff) {
        Map<Integer, Double> bestByAppliance = new HashMap<>();
        for (Recommendation r : plan.recommendations) {
            if (!r.included) continue;
            bestByAppliance.merge(r.applianceId, r.savingsKwh, Math::max);
        }
        double saved = 0;
        for (double v : bestByAppliance.values()) saved += v;
        plan.potentialSavingsKwh = saved;
        plan.potentialSavingsCost = calc.calculateEnergyCost(saved, tariff);
        plan.potentialSavingsPercent = calc.calculateSavingsPercent(plan.baselineMonthlyKwh,
                plan.baselineMonthlyKwh - saved);
        plan.optimizedMonthlyKwh = Math.max(0, plan.baselineMonthlyKwh - saved);
    }

    public double getTariff() {
        return settingsDAO.getTariff();
    }

    private List<Recommendation> recommendationsFor(Appliance a, double tariff) {
        List<Recommendation> list = new ArrayList<>();
        double baseline = a.getMonthlyConsumptionKwh();
        if (baseline <= 0) return list;

        if (a.getDailyHours() >= ALWAYS_ON_HOURS) {
            double newHours = Math.max(0, a.getDailyHours() - ALWAYS_ON_HOURS_OFF);
            double optDaily = calc.calculateDailyKwh(a.getPowerWatts(), a.getQuantity(), newHours);
            double optMonthly = calc.calculateMonthlyKwh(optDaily, a.getDaysPerMonth());
            list.add(make(a, "ALWAYS_ON",
                    "Cut 2 always-on hours / day",
                    "This appliance runs " + fmt(a.getDailyHours()) + " hours/day. Switching it off for "
                            + fmt(ALWAYS_ON_HOURS_OFF) + " hours (timer / scheduled shutdown) reduces monthly kWh linearly with hours.",
                    "Optimized monthly kWh = (Power x Qty x (Hours - 2) / 1000) x Days",
                    baseline, optMonthly, tariff));
        } else if (a.getDailyHours() > HIGH_USAGE_HOURS) {
            double newHours = a.getDailyHours() * HOURS_REDUCTION_FACTOR;
            double optDaily = calc.calculateDailyKwh(a.getPowerWatts(), a.getQuantity(), newHours);
            double optMonthly = calc.calculateMonthlyKwh(optDaily, a.getDaysPerMonth());
            list.add(make(a, "HOURS",
                    "Reduce daily hours by 20%",
                    "Current usage is " + fmt(a.getDailyHours()) + " h/day. A 20% reduction "
                            + "(to " + fmt(newHours) + " h/day) scales energy use by the same factor because "
                            + "kWh is linear in operating hours.",
                    "Optimized monthly kWh = (Power x Qty x Hours x 0.80 / 1000) x Days",
                    baseline, optMonthly, tariff));
        }

        Integer stars = parseStars(a.getEfficiencyRating());
        if (stars != null && stars >= 1 && stars <= 3) {
            double fromIdx = starIndex(stars);
            double toIdx = starIndex(5);
            double optMonthly = baseline * (toIdx / fromIdx);
            list.add(make(a, "EFFICIENCY",
                    "Upgrade from " + stars + " Star to 5 Star",
                    "Using the documented relative-consumption index (1 Star=1.00 ... 5 Star=0.60), "
                            + "monthly kWh is scaled by index_5 / index_" + stars + " = "
                            + fmt(toIdx) + " / " + fmt(fromIdx) + ".",
                    "Optimized monthly kWh = Baseline x (0.60 / " + fmt(fromIdx) + ")",
                    baseline, optMonthly, tariff));
        }

        if ("Lighting".equalsIgnoreCase(a.getCategory()) && a.getPowerWatts() > LIGHTING_LED_WATT_THRESHOLD) {
            double newPower = a.getPowerWatts() * LIGHTING_LED_POWER_FACTOR;
            double optDaily = calc.calculateDailyKwh(newPower, a.getQuantity(), a.getDailyHours());
            double optMonthly = calc.calculateMonthlyKwh(optDaily, a.getDaysPerMonth());
            list.add(make(a, "LED",
                    "Replace lamps with LED (assume 50% power)",
                    "Lighting above " + fmt(LIGHTING_LED_WATT_THRESHOLD)
                            + " W is treated as a non-LED lamp. A typical LED replacement uses about half the wattage "
                            + "at the same brightness. Hours and quantity stay unchanged.",
                    "Optimized monthly kWh = ((Power x 0.50) x Qty x Hours / 1000) x Days",
                    baseline, optMonthly, tariff));
        }

        return list;
    }

    private Recommendation make(Appliance a, String type, String title, String description,
                                String formula, double baseline, double optimized, double tariff) {
        Recommendation r = new Recommendation();
        r.applianceId = a.getId();
        r.applianceName = a.getName();
        r.category = a.getCategory();
        r.actionType = type;
        r.title = title;
        r.description = description;
        r.formula = formula;
        r.baselineMonthlyKwh = baseline;
        r.optimizedMonthlyKwh = optimized;
        r.savingsKwh = calc.calculateSavings(baseline, optimized);
        r.savingsPercent = calc.calculateSavingsPercent(baseline, optimized);
        r.savingsCost = calc.calculateEnergyCost(r.savingsKwh, tariff);
        r.included = true;
        return r;
    }

    private Integer parseStars(String rating) {
        if (rating == null) return null;
        if (rating.startsWith("5")) return 5;
        if (rating.startsWith("4")) return 4;
        if (rating.startsWith("3")) return 3;
        if (rating.startsWith("2")) return 2;
        if (rating.startsWith("1")) return 1;
        return null;
    }

    private double starIndex(int stars) {
        switch (stars) {
            case 5: return 0.60;
            case 4: return 0.70;
            case 3: return 0.80;
            case 2: return 0.90;
            default: return 1.00;
        }
    }

    private String fmt(double v) {
        return String.format("%.2f", v);
    }
}