package com.energy.service;

import com.energy.database.SettingsDAO;

/*
 * Formula chain:
 *   Energy Cost         = Total kWh x Tariff
 *   Subtotal            = Energy Cost + Fixed Charges + Additional Charges
 *   Tax Amount          = Subtotal x (Tax % / 100)
 *   Estimated Bill      = Subtotal + Tax Amount
 */
public class BillCalculationService {

    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final EnergyCalculationService energyCalculationService = new EnergyCalculationService();

    public static class BillBreakdown {
        public double totalKwh;
        public double tariff;
        public double energyCost;
        public double fixedCharges;
        public double additionalCharges;
        public double subtotal;
        public double taxPercent;
        public double taxAmount;
        public double estimatedBill;
    }

    public BillBreakdown calculateBill(double totalKwh, double tariff, double fixedCharges,
                                        double additionalCharges, double taxPercent) {
        BillBreakdown b = new BillBreakdown();
        b.totalKwh = totalKwh;
        b.tariff = tariff;
        b.energyCost = energyCalculationService.calculateEnergyCost(totalKwh, tariff);
        b.fixedCharges = fixedCharges;
        b.additionalCharges = additionalCharges;
        b.subtotal = b.energyCost + b.fixedCharges + b.additionalCharges;
        b.taxPercent = taxPercent;
        b.taxAmount = b.subtotal * (taxPercent / 100.0);
        b.estimatedBill = b.subtotal + b.taxAmount;
        return b;
    }

    public BillBreakdown calculateBillUsingSavedDefaults(double totalKwh, double additionalCharges) {
        return calculateBill(totalKwh, getSavedTariff(), getSavedFixedCharges(), additionalCharges, getSavedTaxPercent());
    }

    public double getSavedTariff() { return settingsDAO.getTariff(); }
    public double getSavedFixedCharges() { return settingsDAO.getFixedCharges(); }
    public double getSavedTaxPercent() { return settingsDAO.getTaxPercent(); }

    public void saveDefaults(double tariff, double fixedCharges, double taxPercent) {
        settingsDAO.setTariff(tariff);
        settingsDAO.setFixedCharges(fixedCharges);
        settingsDAO.setTaxPercent(taxPercent);
    }
}
