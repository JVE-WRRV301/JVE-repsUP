package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;

/**
 * Repository handling Admin table authentication operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class AdminRepository {

    /**
     * Authenticates an admin by username and password against the Admins table.
     * Returns true if credentials match, false otherwise.
     */
    public void authenticate(String username, String password, ConnectionManager.DatabaseCallback<Boolean> callback) {
        String sql = "SELECT COUNT(*) AS cnt FROM admins WHERE admin_username = ? AND admin_password = ?";

        ConnectionManager.querySingle(sql, new Object[]{username, password}, rs -> rs.getInt("cnt") > 0, callback);
    }
}
