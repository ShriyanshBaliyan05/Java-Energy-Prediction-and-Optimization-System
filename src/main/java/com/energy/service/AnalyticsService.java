package com.energy.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.energy.database.ApplianceDAO;
import com.energy.database.EnergyRecordDAO;
import com.energy.database.SettingsDAO;
import com.energy.model.Appliance;
import com.energy.model.ApplianceConsumption;
import com.energy.model.CategoryConsumption;
import com.energy.model.DailyConsumption;

/**
 * Aggregates real logged energy_records (and registered appliances) into
 * charts/tables. No fabricated values - empty inputs produce empty outputs.
 */
public class AnalyticsService {

    private final EnergyRecordDAO energyRecordDAO = new EnergyRecordDAO();
    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final EnergyCalculationService energyCalculationService = new EnergyCalculationService();

    public static class CategoryShare {
        public String category;
        public double kwh;
        public double percent;
        public double cost;
    }

    public static class ApplianceRank {
        public String name;
        public String category;
        public double kwh;
        public double cost;
        public String source; // "Logged records" or "Projected monthly"
    }

    public static class Snapshot {
        public int recordCount;
        public int dayCount;
        public double totalKwh;
        public double totalCost;
        public double avgDailyKwh;
        public String peakDay;
        public double peakDayKwh;
        public List<DailyConsumption> dailySeries = new ArrayList<>();
        public List<CategoryShare> categoryShares = new ArrayList<>();
        public List<ApplianceRank> topLogged = new ArrayList<>();
        public List<ApplianceRank> topProjected = new ArrayList<>();
    }

    public Snapshot buildSnapshot(String fromDate, String toDate) {
        Snapshot s = new Snapshot();
        double tariff = settingsDAO.getTariff();

        s.dailySeries = energyRecordDAO.getDailySeries("All Categories", fromDate, toDate);
        s.dayCount = s.dailySeries.size();
        s.totalKwh = 0;
        s.peakDayKwh = 0;
        s.peakDay = "-";
        for (DailyConsumption d : s.dailySeries) {
            s.totalKwh += d.getTotalKwh();
            if (d.getTotalKwh() > s.peakDayKwh) {
                s.peakDayKwh = d.getTotalKwh();
                s.peakDay = d.getDate();
            }
        }
        s.avgDailyKwh = s.dayCount == 0 ? 0 : s.totalKwh / s.dayCount;
        s.totalCost = energyCalculationService.calculateEnergyCost(s.totalKwh, tariff);
        s.recordCount = energyRecordDAO.search(null, fromDate, toDate).size();

        List<CategoryConsumption> cats = energyRecordDAO.getTotalsByCategory(fromDate, toDate);
        for (CategoryConsumption c : cats) {
            CategoryShare share = new CategoryShare();
            share.category = c.getCategory();
            share.kwh = c.getTotalKwh();
            share.percent = s.totalKwh <= 0 ? 0 : (c.getTotalKwh() / s.totalKwh) * 100.0;
            share.cost = energyCalculationService.calculateEnergyCost(c.getTotalKwh(), tariff);
            s.categoryShares.add(share);
        }

        for (ApplianceConsumption a : energyRecordDAO.getTotalsByAppliance(fromDate, toDate)) {
            ApplianceRank rank = new ApplianceRank();
            rank.name = a.getApplianceName();
            rank.category = a.getCategory();
            rank.kwh = a.getTotalKwh();
            rank.cost = energyCalculationService.calculateEnergyCost(a.getTotalKwh(), tariff);
            rank.source = "Logged records";
            s.topLogged.add(rank);
        }

        List<Appliance> appliances = applianceDAO.getAll();
        List<ApplianceRank> projected = new ArrayList<>();
        for (Appliance a : appliances) {
            ApplianceRank rank = new ApplianceRank();
            rank.name = a.getName();
            rank.category = a.getCategory();
            rank.kwh = a.getMonthlyConsumptionKwh();
            rank.cost = energyCalculationService.calculateEnergyCost(rank.kwh, tariff);
            rank.source = "Projected monthly";
            projected.add(rank);
        }
        projected.sort(Comparator.comparingDouble((ApplianceRank r) -> r.kwh).reversed());
        s.topProjected = projected;

        return s;
    }
}