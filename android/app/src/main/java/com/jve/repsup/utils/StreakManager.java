package com.jve.repsup.utils;

/**
 * Utility class for streak evaluation, multiplier calculation, and milestone detection.
 * Streaks track consecutive days of completing scheduled workouts.
 * Rest days (no workout scheduled) do not affect the streak.
 */
public class StreakManager {

    private static final int MILESTONE_INTERVAL = 7;
    private static final double BASE_MULTIPLIER = 1.0;
    private static final double FIRST_BONUS_MULTIPLIER = 1.5;
    private static final double MULTIPLIER_INCREMENT = 0.5;

    /**
     * Evaluates the new streak value based on yesterday's workout status.
     *
     * Rules:
     * - Scheduled + completed → streak + 1
     * - Scheduled + not completed → streak = 0
     * - No workout scheduled (rest day) → streak unchanged
     *
     * @param currentStreak      the user's current streak count (>= 0)
     * @param hadScheduledWorkout whether the user had a workout scheduled yesterday
     * @param completedYesterday  whether the user completed yesterday's workout
     * @return the updated streak value
     */
    public static int evaluateStreak(int currentStreak, boolean hadScheduledWorkout, boolean completedYesterday) {
        if (!hadScheduledWorkout) {
            // Rest day — streak unchanged
            return currentStreak;
        }

        if (completedYesterday) {
            // Completed scheduled workout — increment streak
            return currentStreak + 1;
        }

        // Missed scheduled workout — reset streak
        return 0;
    }

    /**
     * Calculates the XP multiplier based on the current streak count.
     *
     * - Streak < 7 → 1.0 (no bonus)
     * - Streak 7-13 → 1.5
     * - Streak 14-20 → 2.0
     * - Pattern: increases by 0.5 every 7 days
     *
     * The multiplier is non-decreasing as streak increases.
     *
     * @param streakCount the user's current streak (>= 0)
     * @return the XP multiplier (>= 1.0)
     */
    public static double getMultiplier(int streakCount) {
        if (streakCount < MILESTONE_INTERVAL) {
            return BASE_MULTIPLIER;
        }

        int intervals = streakCount / MILESTONE_INTERVAL;
        return BASE_MULTIPLIER + (intervals * MULTIPLIER_INCREMENT);
    }

    /**
     * Determines whether the given streak count is a milestone.
     * A milestone occurs at every positive multiple of 7 (7, 14, 21, ...).
     *
     * @param streakCount the user's current streak
     * @return true if the streak is a positive multiple of 7
     */
    public static boolean isMilestone(int streakCount) {
        return streakCount > 0 && streakCount % MILESTONE_INTERVAL == 0;
    }
}
