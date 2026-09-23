package com.jve.repsup.database;

import android.util.Log;

import com.jve.repsup.BuildConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Singleton helper that provides pooled JDBC connections to the MySQL database.
 * Credentials are injected via BuildConfig fields read from local.properties.
 *
 * <p>Opening a fresh MySQL connection (TCP handshake + auth) costs hundreds of
 * milliseconds. Doing that for every query makes screens that fire several
 * queries feel sluggish and can exhaust the background thread pool, leading to
 * ANRs and connection timeouts. To avoid that, this helper keeps a small pool of
 * reusable connections: callers borrow one via {@link #getConnection()} and
 * return it by closing it (the returned object is a wrapper whose {@code close()}
 * hands the underlying connection back to the pool instead of tearing it down).
 */
public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    /** Max pooled connections kept alive for reuse. */
    private static final int POOL_SIZE = 4;

    /** Seconds to validate a pooled connection before handing it out. */
    private static final int VALIDATION_TIMEOUT_SEC = 3;

    private static volatile DatabaseHelper instance;

    private final String connectionUrl;
    private final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>(POOL_SIZE);

    private DatabaseHelper() {
        // Load JDBC driver (5.1.x driver class — Android-compatible)
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL JDBC Driver not found", e);
        }

        // Build connection URL from BuildConfig values. socketTimeout guards against
        // a stalled query hanging a pooled connection forever.
        connectionUrl = "jdbc:mysql://"
                + BuildConfig.DB_HOST + ":"
                + BuildConfig.DB_PORT + "/"
                + BuildConfig.DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&connectTimeout=5000&socketTimeout=15000"
                + "&useUnicode=true&characterEncoding=UTF-8";
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
     * Borrows a connection from the pool (or opens a new one if none are free and
     * valid). The returned connection is a {@link PooledConnection} wrapper: call
     * {@code close()} (e.g. via try-with-resources) to return it to the pool for
     * reuse rather than closing the real socket.
     *
     * @return a live Connection to the MySQL database
     * @throws SQLException if a connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        Connection pooled = pool.poll();
        while (pooled != null) {
            if (isUsable(pooled)) {
                return new PooledConnection(pooled, this);
            }
            // Dead/stale connection - discard it and try the next one.
            closeQuietly(pooled);
            pooled = pool.poll();
        }
        // Nothing reusable available - open a fresh real connection.
        return new PooledConnection(openRealConnection(), this);
    }

    /**
     * Returns a real connection to the pool if there's room and it's still usable,
     * otherwise closes it. Called by {@link PooledConnection#close()}.
     */
    void release(Connection real) {
        if (real == null) {
            return;
        }
        if (isUsable(real) && pool.offer(real)) {
            return; // back in the pool for reuse
        }
        closeQuietly(real); // pool full or connection dead - drop it
    }

    private Connection openRealConnection() throws SQLException {
        return DriverManager.getConnection(
                connectionUrl,
                BuildConfig.DB_USER,
                BuildConfig.DB_PASSWORD
        );
    }

    private boolean isUsable(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(VALIDATION_TIMEOUT_SEC);
        } catch (SQLException e) {
            return false;
        }
    }

    private void closeQuietly(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {
            // best-effort cleanup
        }
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
