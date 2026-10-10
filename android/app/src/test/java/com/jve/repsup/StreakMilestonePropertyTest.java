package com.jve.repsup;

import com.jve.repsup.utils.StreakManager;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for streak milestone detection.
 * Uses jqwik to generate arbitrary streak counts and verify that milestones
 * occur at exact positive multiples of 7.
 *
 * Validates: Requirements 16.4
 */
class StreakMilestonePropertyTest {

    // ─── Property 5a: Positive multiples of 7 are milestones ───

    /**
     * Property 5a: For any positive multiple of 7 (7, 14, 21, ...),
     * isMilestone returns true.
     *
     * Validates: Requirements 16.4
     */
    @Property(tries = 200)
    void positiveMultiplesOf7AreMilestones(
            @ForAll @IntRange(min = 1, max = 100) int factor
    ) {
        int streakCount = factor * 7;
        assertTrue(StreakManager.isMilestone(streakCount),
                "Streak " + streakCount + " (= " + factor + " * 7) must be a milestone");
    }

    // ─── Property 5b: Non-multiples of 7 are NOT milestones ───

    /**
     * Property 5b: For any positive integer that is NOT a multiple of 7,
     * isMilestone returns false.
     *
     * Validates: Requirements 16.4
     */
    @Property(tries = 200)
    void nonMultiplesOf7AreNotMilestones(
            @ForAll @IntRange(min = 1, max = 500) int streakCount
    ) {
        Assume.that(streakCount % 7 != 0);
        assertFalse(StreakManager.isMilestone(streakCount),
                "Streak " + streakCount + " (not a multiple of 7) must NOT be a milestone");
    }

    // ─── Property 5c: Zero is NOT a milestone ───

    /**
     * Property 5c: isMilestone(0) returns false because milestones
     * require a positive streak count.
     *
     * Validates: Requirements 16.4
     */
    @Property(tries = 200)
    void zeroIsNotAMilestone() {
        assertFalse(StreakManager.isMilestone(0),
                "Streak 0 must NOT be a milestone (must be positive)");
    }

    // ─── Property 5d: Negative values are NOT milestones ───

    /**
     * Property 5d: For any negative integer, isMilestone returns false.
     *
     * Validates: Requirements 16.4
     */
    @Property(tries = 200)
    void negativeValuesAreNotMilestones(
            @ForAll @IntRange(min = -500, max = -1) int streakCount
    ) {
        assertFalse(StreakManager.isMilestone(streakCount),
                "Streak " + streakCount + " (negative) must NOT be a milestone");
    }

    // ─── Property 5e: Determinism ───

    /**
     * Property 5e: Calling isMilestone twice with the same input always
     * produces the same result (deterministic behavior).
     *
     * Validates: Requirements 16.4
     */
    @Property(tries = 200)
    void isMilestoneIsDeterministic(
            @ForAll @IntRange(min = -500, max = 500) int streakCount
    ) {
        boolean first = StreakManager.isMilestone(streakCount);
        boolean second = StreakManager.isMilestone(streakCount);
        assertEquals(first, second,
                "isMilestone must be deterministic for streak=" + streakCount);
    }
}
