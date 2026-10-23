package com.energy.database;
import com.energy.utils.Constants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:" + Constants.DB_PATH;
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    private DatabaseConnection() {}
}
