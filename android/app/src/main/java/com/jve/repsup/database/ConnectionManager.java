package com.jve.repsup.database;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class that wraps JDBC operations with proper resource management.
 * All database calls run on a background thread pool to avoid blocking the UI.
 */
public class ConnectionManager {

    private static final String TAG = "ConnectionManager";

    // Thread pool for background database operations
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * Callback interface for async database operations.
     */
    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    /**
     * Functional interface for operations that return a result from a Connection.
     */
    public interface DatabaseOperation<T> {
        T execute(Connection connection) throws SQLException;
    }

    /**
     * Functional interface for row mapping from a ResultSet.
     */
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Executes a database operation asynchronously on the background thread pool.
     *
     * @param operation the operation to perform with a Connection
     * @param callback  receives the result or error on completion
     */
    public static <T> void executeAsync(DatabaseOperation<T> operation, DatabaseCallback<T> callback) {
        executor.execute(() -> {
            try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
                T result = operation.execute(conn);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (SQLException e) {
                Log.e(TAG, "Database operation failed", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Executes a query and maps each row to an object using the provided RowMapper.
     *
     * @param sql       the SQL query with ? placeholders
     * @param params    parameter values to bind
     * @param mapper    maps each ResultSet row to an object
     * @param callback  receives the list of results or error
     */
    public static <T> void queryList(String sql, Object[] params, RowMapper<T> mapper, DatabaseCallback<List<T>> callback) {
        executeAsync(connection -> {
            List<T> results = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindParams(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                }
            }
            return results;
        }, callback);
    }

    /**
     * Executes a query expecting a single row result.
     *
     * @param sql       the SQL query with ? placeholders
     * @param params    parameter values to bind
     * @param mapper    maps the ResultSet row to an object
     * @param callback  receives the result (or null if no row) or error
     */
    public static <T> void querySingle(String sql, Object[] params, RowMapper<T> mapper, DatabaseCallback<T> callback) {
        executeAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindParams(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapper.map(rs);
                    }
                    return null;
                }
            }
        }, callback);
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement.
     *
     * @param sql       the SQL statement with ? placeholders
     * @param params    parameter values to bind
     * @param callback  receives the number of affected rows or error
     */
    public static void executeUpdate(String sql, Object[] params, DatabaseCallback<Integer> callback) {
        executeAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                bindParams(stmt, params);
                return stmt.executeUpdate();
            }
        }, callback);
    }

    /**
     * Executes an INSERT and returns the generated key.
     *
     * @param sql       the INSERT SQL with ? placeholders
     * @param params    parameter values to bind
     * @param callback  receives the generated key or error
     */
    public static void executeInsert(String sql, Object[] params, DatabaseCallback<Long> callback) {
        executeAsync(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                bindParams(stmt, params);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                    return -1L;
                }
            }
        }, callback);
    }

    /**
     * Binds parameter values to a PreparedStatement.
     */
    private static void bindParams(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * Shuts down the executor service. Call on app termination if needed.
     */
    public static void shutdown() {
        executor.shutdown();
    }
}
