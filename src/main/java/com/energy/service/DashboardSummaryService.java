package com.energy.service;

import com.energy.database.ApplianceDAO;
import com.energy.database.EnergyRecordDAO;
import com.energy.database.SettingsDAO;

public class DashboardSummaryService {

    private final ApplianceDAO applianceDAO = new ApplianceDAO();
    private final EnergyRecordDAO energyRecordDAO = new EnergyRecordDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final EnergyCalculationService energyCalculationService = new EnergyCalculationService();

    public static class Summary {
        public int totalAppliances;
        public int totalRecords;
        public double totalConsumptionKwh;
        /* Simple Energy Cost = Total kWh x Tariff (NOT the full bill) */
        public double energyCost;
    }

    public Summary getSummary() {
        Summary s = new Summary();
        s.totalAppliances = applianceDAO.countAll();
        s.totalRecords = energyRecordDAO.countAll();
        s.totalConsumptionKwh = energyRecordDAO.getTotalConsumption();
        double tariff = settingsDAO.getTariff();
        s.energyCost = energyCalculationService.calculateEnergyCost(s.totalConsumptionKwh, tariff);
        return s;
    }
}