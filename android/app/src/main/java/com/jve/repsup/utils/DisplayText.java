package com.jve.repsup.utils;

import java.util.Locale;

/**
 * Small helpers for showing database text to the user.
 */
public final class DisplayText {

    private DisplayText() {
        // Utility class — no instances.
    }

    /**
     * Capitalises the first letter and lower-cases the rest, so raw database
     * values like "medium" or "HARD" show as "Medium" / "Hard".
     * Returns an empty string for null or blank input.
     */
    public static String capitalise(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return "";
        return trimmed.substring(0, 1).toUpperCase(Locale.ENGLISH)
                + trimmed.substring(1).toLowerCase(Locale.ENGLISH);
    }
}
