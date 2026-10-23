package com.energy.database;
import com.energy.utils.Constants;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseInitializer {
    public static void initialize() {
        File dataDir = new File(Constants.DATA_DIRECTORY);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        String createAppliances = """
            CREATE TABLE IF NOT EXISTS appliances (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                power_watts REAL NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 1,
                daily_hours REAL NOT NULL,
                days_per_month INTEGER NOT NULL DEFAULT 30,
                efficiency_rating TEXT,
                location TEXT,
                is_sample INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;
        String createEnergyRecords = """
            CREATE TABLE IF NOT EXISTS energy_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                record_date TEXT NOT NULL,
                building_name TEXT,
                appliance_id INTEGER,
                category TEXT,
                consumption_kwh REAL NOT NULL,
                operating_hours REAL,
                meter_reading REAL,
                tariff REAL,
                notes TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (appliance_id) REFERENCES appliances(id) ON DELETE SET NULL
            );
        """;
        String createPredictions = """
            CREATE TABLE IF NOT EXISTS predictions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                prediction_date TEXT NOT NULL,
                prediction_period TEXT NOT NULL,
                predicted_consumption REAL NOT NULL,
                actual_consumption REAL,
                algorithm TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;
        String createSettings = """
            CREATE TABLE IF NOT EXISTS settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                setting_key TEXT UNIQUE NOT NULL,
                setting_value TEXT NOT NULL
            );
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createAppliances);
            stmt.execute(createEnergyRecords);
            stmt.execute(createPredictions);
            stmt.execute(createSettings);
            insertDefaultSettingIfMissing(conn, "electricity_tariff", "8.0");
            insertDefaultSettingIfMissing(conn, "currency_symbol", "rupee");
            insertDefaultSettingIfMissing(conn, "energy_unit", "kWh");
            System.out.println("[DatabaseInitializer] Database ready at: " + Constants.DB_PATH);
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Failed to initialize database.");
            e.printStackTrace();
        }
    }
    private static void insertDefaultSettingIfMissing(Connection conn, String key, String value) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM settings WHERE setting_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setString(1, key);
                        insertPs.setString(2, value);
                        insertPs.executeUpdate();
                    }
                }
            }
        }
    }
    private DatabaseInitializer() {}
}
