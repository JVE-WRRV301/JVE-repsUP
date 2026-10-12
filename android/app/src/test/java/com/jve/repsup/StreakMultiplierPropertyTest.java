package com.jve.repsup;

import com.jve.repsup.utils.StreakManager;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for streak multiplier calculation.
 * Uses jqwik to generate arbitrary streak counts and verify the multiplier rules.
 *
 * Validates: Requirements 16.5
 */
class StreakMultiplierPropertyTest {

    // ─── Property 4a: Streak < 7 → multiplier is 1.0 ───

    /**
     * Property 4a: For any streak count less than 7, the XP multiplier
     * must be exactly 1.0 (no bonus applied).
     *
     * Validates: Requirements 16.5
     */
    @Property(tries = 200)
    void streakBelow7ReturnsBaseMultiplier(
            @ForAll @IntRange(min = 0, max = 6) int streakCount
    ) {
        double multiplier = StreakManager.getMultiplier(streakCount);
        assertEquals(1.0, multiplier, 0.0001,
                "Streak " + streakCount + " (< 7) must have multiplier 1.0");
    }

    // ─── Property 4b: Streak >= 7 → multiplier is >= 1.5 ───

    /**
     * Property 4b: For any streak count of 7 or more, the XP multiplier
     * must be at least 1.5 (first bonus tier).
     *
     * Validates: Requirements 16.5
     */
    @Property(tries = 200)
    void streakAtOrAbove7ReturnsAtLeast1Point5(
            @ForAll @IntRange(min = 7, max = 500) int streakCount
    ) {
        double multiplier = StreakManager.getMultiplier(streakCount);
        assertTrue(multiplier >= 1.5,
                "Streak " + streakCount + " (>= 7) must have multiplier >= 1.5, got " + multiplier);
    }

    // ─── Property 4c: Non-decreasing — higher streak never reduces multiplier ───

    /**
     * Property 4c: For any two streak values a <= b, the multiplier for a
     * must be less than or equal to the multiplier for b. The multiplier
     * function is non-decreasing.
     *
     * Validates: Requirements 16.5
     */
    @Property(tries = 200)
    void multiplierIsNonDecreasing(
            @ForAll @IntRange(min = 0, max = 500) int streakA,
            @ForAll @IntRange(min = 0, max = 500) int streakB
    ) {
        int lower = Math.min(streakA, streakB);
        int higher = Math.max(streakA, streakB);

        double multiplierLower = StreakManager.getMultiplier(lower);
        double multiplierHigher = StreakManager.getMultiplier(higher);

        assertTrue(multiplierLower <= multiplierHigher,
                "Multiplier must be non-decreasing: getMultiplier(" + lower + ")="
                        + multiplierLower + " should be <= getMultiplier(" + higher + ")="
                        + multiplierHigher);
    }

    // ─── Property 4d: Determinism — same input always produces same output ───

    /**
     * Property 4d: Calling getMultiplier twice with the same streak count
     * always produces the same result (deterministic behavior).
     *
     * Validates: Requirements 16.5
     */
    @Property(tries = 200)
    void getMultiplierIsDeterministic(
            @ForAll @IntRange(min = 0, max = 500) int streakCount
    ) {
        double first = StreakManager.getMultiplier(streakCount);
        double second = StreakManager.getMultiplier(streakCount);
        assertEquals(first, second, 0.0001,
                "getMultiplier must be deterministic for streak=" + streakCount);
    }
}
