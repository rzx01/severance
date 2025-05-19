package com.main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
// change on your device, temporarily kept for testing, use env
    private static final String DB_URL = "jdbc:mysql://localhost:3306/severance"; 
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = "root";

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
