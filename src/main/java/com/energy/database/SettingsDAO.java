package com.energy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDAO {

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public void setSetting(String key, String value) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) "
                + "ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getTariff() {
        try {
            return Double.parseDouble(getSetting("electricity_tariff", "8.0"));
        } catch (NumberFormatException e) {
            return 8.0;
        }
    }

    public void setTariff(double tariff) {
        setSetting("electricity_tariff", String.valueOf(tariff));
    }

    public double getFixedCharges() {
        return readDoubleSetting("fixed_charges", 0.0);
    }

    public void setFixedCharges(double fixedCharges) {
        setSetting("fixed_charges", String.valueOf(fixedCharges));
    }

    public double getTaxPercent() {
        return readDoubleSetting("tax_percent", 0.0);
    }

    public void setTaxPercent(double taxPercent) {
        setSetting("tax_percent", String.valueOf(taxPercent));
    }

    private double readDoubleSetting(String key, double defaultValue) {
        try {
            return Double.parseDouble(getSetting(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
