package com.energy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.energy.model.ApplianceConsumption;
import com.energy.model.CategoryConsumption;
import com.energy.model.DailyConsumption;
import com.energy.model.EnergyRecord;

public class EnergyRecordDAO {

    public int insert(EnergyRecord r) {
        String sql = "INSERT INTO energy_records (record_date, building_name, appliance_id, category, " +
                "consumption_kwh, operating_hours, meter_reading, tariff, notes) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getRecordDate());
            ps.setString(2, r.getBuildingName());
            if (r.getApplianceId() != null) ps.setInt(3, r.getApplianceId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, r.getCategory());
            ps.setDouble(5, r.getConsumptionKwh());
            ps.setDouble(6, r.getOperatingHours());
            if (r.getMeterReading() != null) ps.setDouble(7, r.getMeterReading()); else ps.setNull(7, Types.REAL);
            ps.setDouble(8, r.getTariff());
            ps.setString(9, r.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(EnergyRecord r) {
        String sql = "UPDATE energy_records SET record_date=?, building_name=?, appliance_id=?, category=?, " +
                "consumption_kwh=?, operating_hours=?, meter_reading=?, tariff=?, notes=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getRecordDate());
            ps.setString(2, r.getBuildingName());
            if (r.getApplianceId() != null) ps.setInt(3, r.getApplianceId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, r.getCategory());
            ps.setDouble(5, r.getConsumptionKwh());
            ps.setDouble(6, r.getOperatingHours());
            if (r.getMeterReading() != null) ps.setDouble(7, r.getMeterReading()); else ps.setNull(7, Types.REAL);
            ps.setDouble(8, r.getTariff());
            ps.setString(9, r.getNotes());
            ps.setInt(10, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM energy_records WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<EnergyRecord> getAll() {
        List<EnergyRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, a.name as appliance_name FROM energy_records r " +
                "LEFT JOIN appliances a ON r.appliance_id = a.id ORDER BY r.record_date DESC, r.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<EnergyRecord> search(String keyword, String fromDate, String toDate) {
        List<EnergyRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, a.name as appliance_name FROM energy_records r " +
                        "LEFT JOIN appliances a ON r.appliance_id = a.id WHERE 1=1");
        if (keyword != null && !keyword.trim().isEmpty())
            sql.append(" AND (LOWER(r.building_name) LIKE ? OR LOWER(a.name) LIKE ? OR LOWER(r.category) LIKE ?)");
        if (fromDate != null && !fromDate.isEmpty()) sql.append(" AND r.record_date >= ?");
        if (toDate != null && !toDate.isEmpty()) sql.append(" AND r.record_date <= ?");
        sql.append(" ORDER BY r.record_date DESC, r.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            if (fromDate != null && !fromDate.isEmpty()) ps.setString(idx++, fromDate);
            if (toDate != null && !toDate.isEmpty()) ps.setString(idx++, toDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getTotalConsumption() {
        return getTotalConsumption(null, null);
    }

    public double getTotalConsumption(String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("SELECT SUM(consumption_kwh) as total FROM energy_records WHERE 1=1");
        if (fromDate != null && !fromDate.isEmpty()) sql.append(" AND record_date >= ?");
        if (toDate != null && !toDate.isEmpty()) sql.append(" AND record_date <= ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (fromDate != null && !fromDate.isEmpty()) ps.setString(idx++, fromDate);
            if (toDate != null && !toDate.isEmpty()) ps.setString(idx++, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<DailyConsumption> getDailySeries(String category) {
        return getDailySeries(category, null, null);
    }

    public List<DailyConsumption> getDailySeries(String category, String fromDate, String toDate) {
        List<DailyConsumption> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT record_date, SUM(consumption_kwh) as total FROM energy_records WHERE 1=1");
        boolean filterCategory = category != null && !category.equals("All Categories");
        if (filterCategory) sql.append(" AND category = ?");
        if (fromDate != null && !fromDate.isEmpty()) sql.append(" AND record_date >= ?");
        if (toDate != null && !toDate.isEmpty()) sql.append(" AND record_date <= ?");
        sql.append(" GROUP BY record_date ORDER BY record_date ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (filterCategory) ps.setString(idx++, category);
            if (fromDate != null && !fromDate.isEmpty()) ps.setString(idx++, fromDate);
            if (toDate != null && !toDate.isEmpty()) ps.setString(idx++, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DailyConsumption(rs.getString("record_date"), rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<CategoryConsumption> getTotalsByCategory(String fromDate, String toDate) {
        List<CategoryConsumption> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(category, 'Uncategorized') as category, SUM(consumption_kwh) as total " +
                        "FROM energy_records WHERE 1=1");
        if (fromDate != null && !fromDate.isEmpty()) sql.append(" AND record_date >= ?");
        if (toDate != null && !toDate.isEmpty()) sql.append(" AND record_date <= ?");
        sql.append(" GROUP BY COALESCE(category, 'Uncategorized') ORDER BY total DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (fromDate != null && !fromDate.isEmpty()) ps.setString(idx++, fromDate);
            if (toDate != null && !toDate.isEmpty()) ps.setString(idx++, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CategoryConsumption(rs.getString("category"), rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ApplianceConsumption> getTotalsByAppliance(String fromDate, String toDate) {
        List<ApplianceConsumption> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.appliance_id, COALESCE(a.name, 'Manual / Unlinked') as appliance_name, " +
                        "COALESCE(r.category, 'Uncategorized') as category, SUM(r.consumption_kwh) as total " +
                        "FROM energy_records r LEFT JOIN appliances a ON r.appliance_id = a.id WHERE 1=1");
        if (fromDate != null && !fromDate.isEmpty()) sql.append(" AND r.record_date >= ?");
        if (toDate != null && !toDate.isEmpty()) sql.append(" AND r.record_date <= ?");
        sql.append(" GROUP BY r.appliance_id, COALESCE(a.name, 'Manual / Unlinked'), COALESCE(r.category, 'Uncategorized') " +
                "ORDER BY total DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (fromDate != null && !fromDate.isEmpty()) ps.setString(idx++, fromDate);
            if (toDate != null && !toDate.isEmpty()) ps.setString(idx++, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int aid = rs.getInt("appliance_id");
                    Integer applianceId = rs.wasNull() ? null : aid;
                    list.add(new ApplianceConsumption(
                            applianceId,
                            rs.getString("appliance_name"),
                            rs.getString("category"),
                            rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM energy_records";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private EnergyRecord mapRow(ResultSet rs) throws SQLException {
        EnergyRecord r = new EnergyRecord();
        r.setId(rs.getInt("id"));
        r.setRecordDate(rs.getString("record_date"));
        r.setBuildingName(rs.getString("building_name"));
        int applianceId = rs.getInt("appliance_id");
        r.setApplianceId(rs.wasNull() ? null : applianceId);
        r.setApplianceName(rs.getString("appliance_name"));
        r.setCategory(rs.getString("category"));
        r.setConsumptionKwh(rs.getDouble("consumption_kwh"));
        r.setOperatingHours(rs.getDouble("operating_hours"));
        double meter = rs.getDouble("meter_reading");
        r.setMeterReading(rs.wasNull() ? null : meter);
        r.setTariff(rs.getDouble("tariff"));
        r.setNotes(rs.getString("notes"));
        r.setCreatedAt(rs.getString("created_at"));
        return r;
    }
}