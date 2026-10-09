package com.jve.repsup;

import com.jve.repsup.utils.XPCalculator;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Level-Up Threshold (Property 6).
 * Verifies that {@link XPCalculator#shouldLevelUp(long, int)} is deterministic
 * and correctly compares totalXp against the cumulative threshold to reach the
 * next level, which under the current curve (XP for N->N+1 = 400 + 50*N) is
 * {@link XPCalculator#cumulativeXpForLevel(int)} evaluated at currentLevel + 1.
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
     * Property 6b: For any level >= 1, when totalXp >= the cumulative threshold
     * to reach the next level, shouldLevelUp returns true.
     *
     * Validates: Requirements 18.1
     */
    @Property(tries = 200)
    void shouldLevelUpReturnsTrueWhenXpMeetsThreshold(
            @ForAll("levelValues") int currentLevel,
            @ForAll @IntRange(min = 0, max = 1_000_000) int extraXp
    ) {
        long threshold = XPCalculator.cumulativeXpForLevel(currentLevel + 1);
        long totalXp = threshold + extraXp;

        assertTrue(XPCalculator.shouldLevelUp(totalXp, currentLevel),
                "shouldLevelUp must return true when totalXp (" + totalXp +
                        ") >= threshold (" + threshold + ") at level " + currentLevel);
    }

    // ─── Property 6c: Returns false when XP < threshold ───

    /**
     * Property 6c: For any level >= 1, when totalXp < the cumulative threshold
     * to reach the next level, shouldLevelUp returns false.
     *
     * Validates: Requirements 18.1
     */
    @Property(tries = 200)
    void shouldLevelUpReturnsFalseWhenXpBelowThreshold(
            @ForAll("levelValues") int currentLevel,
            @ForAll("xpValues") int rawXp
    ) {
        long threshold = XPCalculator.cumulativeXpForLevel(currentLevel + 1);

        // Constrain totalXp to be strictly below threshold (0 <= totalXp < threshold)
        long totalXp = rawXp % threshold; // threshold is always > 0 for level >= 1

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
        // Level 1: reaching level 2 needs 450 XP
        assertTrue(XPCalculator.shouldLevelUp(450, 1),
                "Level 1 threshold is 450; XP=450 should level up");

        // Level 5: reaching level 6 needs 2750 XP
        assertTrue(XPCalculator.shouldLevelUp(2750, 5),
                "Level 5 threshold is 2750; XP=2750 should level up");

        // Level 10: reaching level 11 needs 6750 XP
        assertTrue(XPCalculator.shouldLevelUp(6750, 10),
                "Level 10 threshold is 6750; XP=6750 should level up");
    }

    /**
     * Edge case: At threshold - 1, shouldLevelUp returns false.
     *
     * Validates: Requirements 18.1
     */
    @Example
    void atThresholdMinusOneReturnsFalse() {
        // Level 1: threshold = 450, so 449 should not level up
        assertFalse(XPCalculator.shouldLevelUp(449, 1),
                "Level 1 threshold is 450; XP=449 should NOT level up");

        // Level 5: threshold = 2750, so 2749 should not level up
        assertFalse(XPCalculator.shouldLevelUp(2749, 5),
                "Level 5 threshold is 2750; XP=2749 should NOT level up");

        // Level 10: threshold = 6750, so 6749 should not level up
        assertFalse(XPCalculator.shouldLevelUp(6749, 10),
                "Level 10 threshold is 6750; XP=6749 should NOT level up");
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
        // Level 100: reaching level 101 needs 100 * (400 + 25*101) = 292500 XP
        assertTrue(XPCalculator.shouldLevelUp(292500, 100),
                "Level 100 threshold is 292500; XP=292500 should level up");
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
