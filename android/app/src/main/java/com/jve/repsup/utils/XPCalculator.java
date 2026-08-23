package com.jve.repsup.utils;

import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.repositories.UserRepository;

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
     * Level 1→2 requires 500 XP, level 2→3 requires 1000 XP,
     * level 3→4 requires 2000 XP, etc. (doubles each level).
     * Formula: 500 * 2^(currentLevel - 1)
     *
     * @param currentLevel the user's current level (must be >= 1)
     * @return XP required to advance from currentLevel to currentLevel+1
     */
    public static int xpForNextLevel(int currentLevel) {
        return 500 * (int) Math.pow(2, currentLevel - 1);
    }

    /**
     * Checks if total XP meets or exceeds the cumulative threshold for the current level.
     * The cumulative threshold to reach level N+1 is 500 * (2^N - 1),
     * which is the sum of the doubling series: 500 + 1000 + 2000 + ...
     *
     * @param totalXp      the user's total accumulated XP
     * @param currentLevel the user's current level (must be >= 1)
     * @return true if the user has enough XP to level up
     */
    public static boolean shouldLevelUp(int totalXp, int currentLevel) {
        int cumulativeThreshold = 500 * ((int) Math.pow(2, currentLevel) - 1);
        return totalXp >= cumulativeThreshold;
    }

    /**
     * Awards XP to a user by inserting an XP_Log record and updating the user's total XP.
     * This wires XP awarding per Requirement 17.3.
     *
     * @param userId         the user's ID
     * @param xpEarned       the amount of XP earned
     * @param sourceType     the source of XP (e.g., "workout", "goal")
     * @param sourceId       the ID of the source entity (workout_id or goal_id)
     * @param userRepository the repository instance for database operations
     */
    public static void awardXP(int userId, int xpEarned, String sourceType, int sourceId,
                               UserRepository userRepository) {
        // Update the user's total XP
        userRepository.updateXp(userId, xpEarned, null);

        // Insert an XP_Log record to track this earning
        userRepository.insertXpLog(userId, xpEarned, sourceType, sourceId, xpEarned, 0, null);
    }
}
