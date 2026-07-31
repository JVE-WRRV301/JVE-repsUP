package com.jve.repsup.database;

import android.util.Log;

import com.jve.repsup.BuildConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton helper that provides JDBC connections to the MySQL database.
 * Credentials are injected via BuildConfig fields read from local.properties.
 */
public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    private static volatile DatabaseHelper instance;

    private final String connectionUrl;

    private DatabaseHelper() {
        // Load JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC Driver not found", e);
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        // Build connection URL from BuildConfig values
        connectionUrl = "jdbc:mysql://"
                + BuildConfig.DB_HOST + ":"
                + BuildConfig.DB_PORT + "/"
                + BuildConfig.DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000";
    }

    /**
     * Returns the singleton instance of DatabaseHelper.
     */
    public static DatabaseHelper getInstance() {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper();
                }
            }
        }
        return instance;
    }

    /**
     * Opens and returns a new JDBC connection.
     * Caller is responsible for closing the connection (use try-with-resources).
     *
     * @return a live Connection to the MySQL database
     * @throws SQLException if the connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                connectionUrl,
                BuildConfig.DB_USER,
                BuildConfig.DB_PASSWORD
        );
    }

    /**
     * Tests the database connection by executing SELECT 1.
     *
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            Log.e(TAG, "Database connection test failed", e);
            return false;
        }
    }
}
