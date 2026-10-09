package com.jve.repsup.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The app's date formats, kept in one place so every screen shows dates the
 * same way.
 *
 * Short:  "Fri, 9 Oct"       (Home workout card, Calendar day title)
 * Long:   "9 October 2026"   (date fields, goal deadlines)
 * Day + month: "9 Oct"       (week range, My Plan day headers, where the
 *                            weekday is already shown next to it)
 *
 * The week starts on Monday everywhere (Home week strip, Calendar, My Plan).
 *
 * Locale.ENGLISH keeps the month names fixed (some UK locales write "Sept").
 * A new SimpleDateFormat is made on each call because the class isn't
 * thread-safe and these run from both the UI and callback threads.
 */
public final class DateFormats {

    private static final String SHORT = "EEE, d MMM";
    private static final String LONG = "d MMMM yyyy";
    private static final String DAY_MONTH = "d MMM";
    private static final String MONTH_YEAR = "MMMM yyyy";
    private static final String WEEKDAY = "EEEE";

    private DateFormats() {
        // Utility class — no instances.
    }

    /** "Fri, 9 Oct" */
    public static String formatShort(Date date) {
        return format(SHORT, date);
    }

    /** "9 October 2026" */
    public static String formatLong(Date date) {
        return format(LONG, date);
    }

    /** "9 Oct" */
    public static String formatDayMonth(Date date) {
        return format(DAY_MONTH, date);
    }

    /** "October 2026" (Calendar month header) */
    public static String formatMonthYear(Date date) {
        return format(MONTH_YEAR, date);
    }

    /** "Friday" */
    public static String formatWeekday(Date date) {
        return format(WEEKDAY, date);
    }

    /**
     * How many days a given Calendar.DAY_OF_WEEK is after Monday:
     * Monday = 0, Tuesday = 1 … Sunday = 6.
     */
    public static int daysSinceMonday(int calendarDayOfWeek) {
        return (calendarDayOfWeek - Calendar.MONDAY + 7) % 7;
    }

    private static String format(String pattern, Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(pattern, Locale.ENGLISH).format(date);
    }
}
