package com.jve.repsup.utils;

import com.jve.repsup.models.ExerciseResult;

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
     * @param results          list of exercise results from the completed workout
     * @param streakMultiplier multiplier based on current streak (minimum 1.0)
     * @return total XP earned, cast to int
     */
    public static int calculateWorkoutXP(List<ExerciseResult> results, double streakMultiplier) {
        if (results == null || results.isEmpty()) {
            return 0;
        }

        if (streakMultiplier < 1.0) {
            streakMultiplier = 1.0;
        }

        int baseTotal = 0;
        for (ExerciseResult result : results) {
            baseTotal += result.getBaseXp() * result.getSetsCompleted();
        }

        return (int) (baseTotal * streakMultiplier);
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
