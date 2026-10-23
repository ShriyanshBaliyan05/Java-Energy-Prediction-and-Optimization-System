package com.energy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.energy.model.Appliance;

/**
 * Data Access Object for the 'appliances' table.
 * Every method opens its own short-lived connection (SQLite is file-based,
 * so this is lightweight) and uses PreparedStatement to prevent SQL injection.
 */
public class ApplianceDAO {

    public int insert(Appliance a) {
        String sql = "INSERT INTO appliances "
                + "(name, category, power_watts, quantity, daily_hours, days_per_month, "
                + "efficiency_rating, location, is_sample) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getName());
            ps.setString(2, a.getCategory());
            ps.setDouble(3, a.getPowerWatts());
            ps.setInt(4, a.getQuantity());
            ps.setDouble(5, a.getDailyHours());
            ps.setInt(6, a.getDaysPerMonth());
            ps.setString(7, a.getEfficiencyRating());
            ps.setString(8, a.getLocation());
            ps.setInt(9, a.isSample() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Appliance a) {
        String sql = "UPDATE appliances SET name = ?, category = ?, power_watts = ?, quantity = ?, "
                + "daily_hours = ?, days_per_month = ?, efficiency_rating = ?, location = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getName());
            ps.setString(2, a.getCategory());
            ps.setDouble(3, a.getPowerWatts());
            ps.setInt(4, a.getQuantity());
            ps.setDouble(5, a.getDailyHours());
            ps.setInt(6, a.getDaysPerMonth());
            ps.setString(7, a.getEfficiencyRating());
            ps.setString(8, a.getLocation());
            ps.setInt(9, a.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM appliances WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Appliance getById(int id) {
        String sql = "SELECT * FROM appliances WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Appliance> getAll() {
        List<Appliance> list = new ArrayList<>();
        String sql = "SELECT * FROM appliances ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appliance> search(String keyword, String category) {
        List<Appliance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM appliances WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND LOWER(name) LIKE ? ");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }
        if (category != null && !category.equals("All Categories")) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        sql.append("ORDER BY name ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM appliances";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Appliance mapRow(ResultSet rs) throws SQLException {
        Appliance a = new Appliance();
        a.setId(rs.getInt("id"));
        a.setName(rs.getString("name"));
        a.setCategory(rs.getString("category"));
        a.setPowerWatts(rs.getDouble("power_watts"));
        a.setQuantity(rs.getInt("quantity"));
        a.setDailyHours(rs.getDouble("daily_hours"));
        a.setDaysPerMonth(rs.getInt("days_per_month"));
        a.setEfficiencyRating(rs.getString("efficiency_rating"));
        a.setLocation(rs.getString("location"));
        a.setSample(rs.getInt("is_sample") == 1);
        a.setCreatedAt(rs.getString("created_at"));
        return a;
    }
}