package com.jve.repsup.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session state via SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "RepsUpSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_ADMIN = "is_admin";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createSession(int userId, String username) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public boolean hasActiveSession() {
        return prefs.getInt(KEY_USER_ID, -1) != -1;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    public void setAdmin(boolean isAdmin) {
        prefs.edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply();
    }
}
