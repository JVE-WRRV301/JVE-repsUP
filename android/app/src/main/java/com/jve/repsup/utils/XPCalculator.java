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
     * Matching ignores case and a null priority is treated as Medium, so values
     * saved as "top"/"medium"/"low" (or null) still award the right XP.
     *
     * @param priority the goal priority string
     * @return XP value for the given priority
     */
    public static int calculateGoalXP(String priority) {
        String normalised = priority == null ? "medium" : priority.trim().toLowerCase();
        switch (normalised) {
            case "top":
                return 1000;
            case "medium":
                return 500;
            case "low":
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
     * Determines the XP needed to advance from the current level to the next.
     * The gentle, linear curve is: XP to go from level N to N+1 = 400 + 50 * N.
     * So level 1→2 = 450, level 2→3 = 500, level 3→4 = 550, and so on.
     *
     * The arithmetic uses long values so it can never overflow at high levels.
     *
     * @param currentLevel the user's current level (must be >= 1)
     * @return XP required to advance from currentLevel to currentLevel+1
     */
    public static long xpForNextLevel(int currentLevel) {
        return 400L + 50L * currentLevel;
    }

    /**
     * Returns the cumulative XP needed to reach the given level from level 1.
     * This is the sum of (400 + 50 * N) for N = 1 .. level-1, which simplifies
     * to (level - 1) * (400 + 25 * level). Reaching level 1 needs 0 XP.
     *
     * All arithmetic is done in long to avoid overflow at high levels.
     *
     * @param level the target level (must be >= 1)
     * @return cumulative XP required to reach that level
     */
    public static long cumulativeXpForLevel(int level) {
        if (level <= 1) {
            return 0L;
        }
        long l = level;
        return (l - 1L) * (400L + 25L * l);
    }

    /**
     * Works out the level a user is at for a given total XP under the current
     * curve. Returns the highest level L (>= 1) whose cumulative XP requirement
     * is met by {@code xp}.
     *
     * Walks the cumulative thresholds in long arithmetic so it can't overflow or
     * spin forever: the loop stops as soon as the next level's requirement is
     * beyond the supplied XP.
     *
     * @param xp the user's total accumulated XP
     * @return the level the user should be at (minimum 1)
     */
    public static int levelForTotalXp(long xp) {
        int level = 1;
        while (xp >= cumulativeXpForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    /**
     * Checks if total XP meets or exceeds the cumulative threshold to advance
     * past the current level (i.e. to reach currentLevel + 1).
     *
     * @param totalXp      the user's total accumulated XP
     * @param currentLevel the user's current level (must be >= 1)
     * @return true if the user has enough XP to level up
     */
    public static boolean shouldLevelUp(long totalXp, int currentLevel) {
        return totalXp >= cumulativeXpForLevel(currentLevel + 1);
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
        userRepository.insertXpLog(userId, sourceType, sourceId, xpEarned, 0, null);
    }
}
