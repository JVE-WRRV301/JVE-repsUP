package com.jve.repsup;

import com.jve.repsup.utils.StreakManager;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for streak evaluation logic.
 * Uses jqwik to generate arbitrary streak counts and verify the three transition rules.
 *
 * Validates: Requirements 16.1, 16.2, 16.3
 */
class StreakEvaluationPropertyTest {

    // ─── Property 3a: Scheduled + Completed → streak + 1 ───

    /**
     * Property 3a: For any current streak >= 0, when a workout was scheduled
     * AND completed yesterday, the new streak equals currentStreak + 1.
     *
     * Validates: Requirements 16.1
     */
    @Property(tries = 200)
    void scheduledAndCompletedIncrementsStreak(
            @ForAll @IntRange(min = 0, max = 10000) int currentStreak
    ) {
        int result = StreakManager.evaluateStreak(currentStreak, true, true);
        assertEquals(currentStreak + 1, result,
                "Scheduled + completed must increment streak from " + currentStreak);
    }

    // ─── Property 3b: Scheduled + Not Completed → streak = 0 ───

    /**
     * Property 3b: For any current streak >= 0, when a workout was scheduled
     * AND NOT completed yesterday, the streak resets to 0.
     *
     * Validates: Requirements 16.2
     */
    @Property(tries = 200)
    void scheduledAndNotCompletedResetsStreak(
            @ForAll @IntRange(min = 0, max = 10000) int currentStreak
    ) {
        int result = StreakManager.evaluateStreak(currentStreak, true, false);
        assertEquals(0, result,
                "Scheduled + not completed must reset streak to 0 (was " + currentStreak + ")");
    }

    // ─── Property 3c: No Workout Scheduled (Rest Day) → streak unchanged ───

    /**
     * Property 3c: For any current streak >= 0, when no workout was scheduled
     * yesterday (rest day), the streak remains unchanged regardless of the
     * completedYesterday flag.
     *
     * Validates: Requirements 16.3
     */
    @Property(tries = 200)
    void restDayLeavesStreakUnchanged(
            @ForAll @IntRange(min = 0, max = 10000) int currentStreak,
            @ForAll boolean completedYesterday
    ) {
        int result = StreakManager.evaluateStreak(currentStreak, false, completedYesterday);
        assertEquals(currentStreak, result,
                "Rest day must leave streak unchanged at " + currentStreak);
    }

    // ─── Property 3d: Determinism — same inputs always produce same output ───

    /**
     * Property 3d: Calling evaluateStreak twice with the same inputs always
     * produces the same result (deterministic behavior).
     *
     * Validates: Requirements 16.1, 16.2, 16.3
     */
    @Property(tries = 200)
    void evaluateStreakIsDeterministic(
            @ForAll @IntRange(min = 0, max = 10000) int currentStreak,
            @ForAll boolean hadScheduledWorkout,
            @ForAll boolean completedYesterday
    ) {
        int first = StreakManager.evaluateStreak(currentStreak, hadScheduledWorkout, completedYesterday);
        int second = StreakManager.evaluateStreak(currentStreak, hadScheduledWorkout, completedYesterday);
        assertEquals(first, second,
                "evaluateStreak must be deterministic for streak=" + currentStreak
                        + ", scheduled=" + hadScheduledWorkout
                        + ", completed=" + completedYesterday);
    }
}
