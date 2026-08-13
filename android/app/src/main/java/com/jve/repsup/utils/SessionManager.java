package com.jve.repsup.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session using SharedPreferences.
 * Stores user_id, username, and isAdmin flag.
 */
public class SessionManager {

    private static final String PREF_NAME = "RepsUpSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final int DEFAULT_USER_ID = -1;

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Creates a new session with the given user ID, username, and first name.
     */
    public void createSession(int userId, String username, String name) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    /**
     * Creates a new session with the given user ID and username (backward compatible).
     */
    public void createSession(int userId, String username) {
        createSession(userId, username, null);
    }

    /**
     * Returns true if a valid session exists (user_id is stored and not default).
     */
    public boolean hasActiveSession() {
        return preferences.getInt(KEY_USER_ID, DEFAULT_USER_ID) != DEFAULT_USER_ID;
    }

    /**
     * Returns the stored user ID, or -1 if no session exists.
     */
    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, DEFAULT_USER_ID);
    }

    /**
     * Returns the stored username, or null if no session exists.
     */
    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    /**
     * Returns the stored first name, or null if not set.
     */
    public String getName() {
        return preferences.getString(KEY_NAME, null);
    }

    /**
     * Clears all session data.
     */
    public void clearSession() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Returns true if the current session user is an admin.
     */
    public boolean isAdmin() {
        return preferences.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Sets the admin flag for the current session.
     */
    public void setAdmin(boolean isAdmin) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }
}
