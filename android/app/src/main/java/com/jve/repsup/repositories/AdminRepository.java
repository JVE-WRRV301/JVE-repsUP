package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.utils.PasswordHasher;

/**
 * Repository handling Admin table authentication operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class AdminRepository {

    /**
     * Authenticates an admin by username and password against the admins table.
     *
     * The admin row is fetched by username only, then the supplied password is
     * verified in Java against the stored PBKDF2 "salt:hash" value (same
     * approach as user login). Returns true if the admin exists and the password
     * matches, false otherwise.
     */
    public void authenticate(String username, String password, ConnectionManager.DatabaseCallback<Boolean> callback) {
        String sql = "SELECT admin_password FROM admins WHERE admin_username = ?";

        ConnectionManager.querySingle(sql, new Object[]{username},
                rs -> rs.getString("admin_password"),
                new ConnectionManager.DatabaseCallback<String>() {
                    @Override
                    public void onSuccess(String storedPassword) {
                        boolean matches = PasswordHasher.verify(password, storedPassword);
                        if (callback != null) {
                            callback.onSuccess(matches);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                });
    }
}
