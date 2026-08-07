package com.jve.repsup.utils;

import java.util.List;

/**
 * Utility class for XP calculations throughout the Reps Up app.
 * Handles workout XP, goal XP, level thresholds, and level-up checks.
 */
public class XPCalculator {

    /**
     * Returns XP for completing a goal based on priority.
     * Top=1000, Medium=500, Low=250, any other value=0.
     *
     * @param priority the goal priority string
     * @return XP value for the given priority
     */
    public static int calculateGoalXP(String priority) {
        if (priority == null) {
            return 0;
        }
        switch (priority) {
            case "Top":
                return 1000;
            case "Medium":
                return 500;
            case "Low":
                return 250;
            default:
                return 0;
        }
    }

    /**
     * Calculates XP earned from completing a workout.
     * Formula: sum(base_xp * sets_completed per exercise) * streak_multiplier
     *
     * TODO: Implement in task 13.1
     */
    public static int calculateWorkoutXP(/* List<ExerciseResult> results, double streakMultiplier */) {
        throw new UnsupportedOperationException("Not yet implemented — see task 13.1");
    }

    /**
     * Determines the XP threshold needed for the next level.
     * Level N requires N * 100 XP to advance to N+1.
     *
     * TODO: Implement in task 13.1
     */
    public static int xpForNextLevel(int currentLevel) {
        throw new UnsupportedOperationException("Not yet implemented — see task 13.1");
    }

    /**
     * Checks if total XP exceeds the threshold for the next level.
     *
     * TODO: Implement in task 13.1
     */
    public static boolean shouldLevelUp(int totalXp, int currentLevel) {
        throw new UnsupportedOperationException("Not yet implemented — see task 13.1");
    }
}
