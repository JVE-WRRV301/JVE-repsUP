package com.jve.repsup;

import com.jve.repsup.utils.XPCalculator;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Level-Up Threshold (Property 6).
 * Verifies that {@link XPCalculator#shouldLevelUp(int, int)} is deterministic
 * and correctly compares totalXp against the cumulative threshold
 * (sum of i*100 for i=1..currentLevel = currentLevel * (currentLevel + 1) * 50).
 *
 * Uses jqwik to generate arbitrary levels and XP values.
 *
 * Validates: Requirements 18.1
 */
class LevelUpThresholdPropertyTest {

    // ─── Property 6a: Determinism / Idempotency ───

    /**
     * Property 6a: Calling shouldLevelUp twice with the same inputs
     * always produces the same result.
     *
     * Validates: Requirements 18.1
     */
    @Property(tries = 200)
    void shouldLevelUpIsDeterministic(
            @ForAll("xpValues") int totalXp,
            @ForAll("levelValues") int currentLevel
    ) {
        boolean first = XPCalculator.shouldLevelUp(totalXp, currentLevel);
        boolean second = XPCalculator.shouldLevelUp(totalXp, currentLevel);
        assertEquals(first, second,
                "shouldLevelUp must return the same result for identical inputs. " +
                        "totalXp=" + totalXp + ", currentLevel=" + currentLevel);
    }

    // ─── Property 6b: Returns true when XP >= threshold ───

    /**
     * Property 6b: For any level >= 1, when totalXp >= cumulative threshold,
     * shouldLevelUp returns true.
     * Cumulative threshold = currentLevel * (currentLevel + 1) * 50.
     *
     * Validates: Requirements 18.1
     */
    @Property(tries = 200)
    void shouldLevelUpReturnsTrueWhenXpMeetsThreshold(
            @ForAll("levelValues") int currentLevel,
            @ForAll @IntRange(min = 0, max = 1_000_000) int extraXp
    ) {
        int threshold = currentLevel * (currentLevel + 1) * 50;
        int totalXp = threshold + extraXp;

        assertTrue(XPCalculator.shouldLevelUp(totalXp, currentLevel),
                "shouldLevelUp must return true when totalXp (" + totalXp +
                        ") >= threshold (" + threshold + ") at level " + currentLevel);
    }

    // ─── Property 6c: Returns false when XP < threshold ───

    /**
     * Property 6c: For any level >= 1, when totalXp < cumulative threshold,
     * shouldLevelUp returns false.
     * Cumulative threshold = currentLevel * (currentLevel + 1) * 50.
     *
     * Validates: Requirements 18.1
     */
    @Property(tries = 200)
    void shouldLevelUpReturnsFalseWhenXpBelowThreshold(
            @ForAll("levelValues") int currentLevel,
            @ForAll("xpValues") int rawXp
    ) {
        int threshold = currentLevel * (currentLevel + 1) * 50;

        // Constrain totalXp to be strictly below threshold (at least 1 below)
        int totalXp = rawXp % threshold; // Guarantees 0 <= totalXp < threshold

        assertFalse(XPCalculator.shouldLevelUp(totalXp, currentLevel),
                "shouldLevelUp must return false when totalXp (" + totalXp +
                        ") < threshold (" + threshold + ") at level " + currentLevel);
    }

    // ─── Edge Cases ───

    /**
     * Edge case: At exactly the threshold, shouldLevelUp returns true.
     *
     * Validates: Requirements 18.1
     */
    @Example
    void atExactThresholdReturnsTrue() {
        // Level 1: threshold = 1 * 2 * 50 = 100
        assertTrue(XPCalculator.shouldLevelUp(100, 1),
                "Level 1 threshold is 100; XP=100 should level up");

        // Level 5: threshold = 5 * 6 * 50 = 1500
        assertTrue(XPCalculator.shouldLevelUp(1500, 5),
                "Level 5 threshold is 1500; XP=1500 should level up");

        // Level 10: threshold = 10 * 11 * 50 = 5500
        assertTrue(XPCalculator.shouldLevelUp(5500, 10),
                "Level 10 threshold is 5500; XP=5500 should level up");
    }

    /**
     * Edge case: At threshold - 1, shouldLevelUp returns false.
     *
     * Validates: Requirements 18.1
     */
    @Example
    void atThresholdMinusOneReturnsFalse() {
        // Level 1: threshold = 100, so 99 should not level up
        assertFalse(XPCalculator.shouldLevelUp(99, 1),
                "Level 1 threshold is 100; XP=99 should NOT level up");

        // Level 5: threshold = 1500, so 1499 should not level up
        assertFalse(XPCalculator.shouldLevelUp(1499, 5),
                "Level 5 threshold is 1500; XP=1499 should NOT level up");

        // Level 10: threshold = 5500, so 5499 should not level up
        assertFalse(XPCalculator.shouldLevelUp(5499, 10),
                "Level 10 threshold is 5500; XP=5499 should NOT level up");
    }

    /**
     * Edge case: Level 1 with 0 XP should not level up.
     *
     * Validates: Requirements 18.1
     */
    @Example
    void level1WithZeroXpReturnsFalse() {
        assertFalse(XPCalculator.shouldLevelUp(0, 1),
                "Level 1 with 0 XP should NOT level up");
    }

    /**
     * Edge case: Very high level with enough XP should level up.
     *
     * Validates: Requirements 18.1
     */
    @Example
    void highLevelWithSufficientXpReturnsTrue() {
        // Level 100: threshold = 100 * 101 * 50 = 505000
        assertTrue(XPCalculator.shouldLevelUp(505000, 100),
                "Level 100 threshold is 505000; XP=505000 should level up");
    }

    // ─── Custom Providers ───

    @Provide
    Arbitrary<Integer> levelValues() {
        return Arbitraries.integers().between(1, 100);
    }

    @Provide
    Arbitrary<Integer> xpValues() {
        return Arbitraries.integers().between(0, 1_000_000);
    }
}
