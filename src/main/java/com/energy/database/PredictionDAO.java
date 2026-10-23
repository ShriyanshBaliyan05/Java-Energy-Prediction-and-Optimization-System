package com.energy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.energy.model.Prediction;

public class PredictionDAO {

    public int insert(Prediction p) {
        String sql = "INSERT INTO predictions (prediction_date, prediction_period, predicted_consumption, " +
                "actual_consumption, algorithm) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPredictionDate());
            ps.setString(2, p.getPredictionPeriod());
            ps.setDouble(3, p.getPredictedConsumption());
            if (p.getActualConsumption() != null) ps.setDouble(4, p.getActualConsumption()); else ps.setNull(4, Types.REAL);
            ps.setString(5, p.getAlgorithm());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateActual(int id, double actualConsumption) {
        String sql = "UPDATE predictions SET actual_consumption = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, actualConsumption);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM predictions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Prediction> getAll() {
        List<Prediction> list = new ArrayList<>();
        String sql = "SELECT * FROM predictions ORDER BY created_at DESC, id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Prediction mapRow(ResultSet rs) throws SQLException {
        Prediction p = new Prediction();
        p.setId(rs.getInt("id"));
        p.setPredictionDate(rs.getString("prediction_date"));
        p.setPredictionPeriod(rs.getString("prediction_period"));
        p.setPredictedConsumption(rs.getDouble("predicted_consumption"));
        double actual = rs.getDouble("actual_consumption");
        p.setActualConsumption(rs.wasNull() ? null : actual);
        p.setAlgorithm(rs.getString("algorithm"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }
}