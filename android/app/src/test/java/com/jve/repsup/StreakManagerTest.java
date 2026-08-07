package com.jve.repsup;

import com.jve.repsup.utils.StreakManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StreakManager utility class.
 * Tests all three public methods: evaluateStreak, getMultiplier, isMilestone.
 */
class StreakManagerTest {

    // --- evaluateStreak tests ---

    @Test
    void evaluateStreak_scheduledAndCompleted_incrementsStreak() {
        assertEquals(1, StreakManager.evaluateStreak(0, true, true));
        assertEquals(5, StreakManager.evaluateStreak(4, true, true));
        assertEquals(15, StreakManager.evaluateStreak(14, true, true));
    }

    @Test
    void evaluateStreak_scheduledAndNotCompleted_resetsStreak() {
        assertEquals(0, StreakManager.evaluateStreak(0, true, false));
        assertEquals(0, StreakManager.evaluateStreak(10, true, false));
        assertEquals(0, StreakManager.evaluateStreak(100, true, false));
    }

    @Test
    void evaluateStreak_noWorkoutScheduled_streakUnchanged() {
        assertEquals(0, StreakManager.evaluateStreak(0, false, false));
        assertEquals(5, StreakManager.evaluateStreak(5, false, false));
        assertEquals(14, StreakManager.evaluateStreak(14, false, true));
    }

    // --- getMultiplier tests ---

    @Test
    void getMultiplier_belowFourteen_returnsBaseMultiplier() {
        assertEquals(1.0, StreakManager.getMultiplier(0));
        assertEquals(1.0, StreakManager.getMultiplier(1));
        assertEquals(1.0, StreakManager.getMultiplier(13));
    }

    @Test
    void getMultiplier_atFourteen_returnsFirstBonus() {
        assertEquals(1.5, StreakManager.getMultiplier(14));
    }

    @Test
    void getMultiplier_betweenFourteenAndTwentyEight_returnsFirstBonus() {
        assertEquals(1.5, StreakManager.getMultiplier(15));
        assertEquals(1.5, StreakManager.getMultiplier(27));
    }

    @Test
    void getMultiplier_atTwentyEight_returnsSecondBonus() {
        assertEquals(2.0, StreakManager.getMultiplier(28));
    }

    @Test
    void getMultiplier_atFortyTwo_returnsThirdBonus() {
        assertEquals(2.5, StreakManager.getMultiplier(42));
    }

    @Test
    void getMultiplier_isNonDecreasing() {
        double previous = StreakManager.getMultiplier(0);
        for (int i = 1; i <= 200; i++) {
            double current = StreakManager.getMultiplier(i);
            assertTrue(current >= previous,
                    "Multiplier decreased from " + previous + " to " + current + " at streak " + i);
            previous = current;
        }
    }

    @Test
    void getMultiplier_atLeastOnePointFiveForFourteenOrAbove() {
        for (int i = 14; i <= 200; i++) {
            assertTrue(StreakManager.getMultiplier(i) >= 1.5,
                    "Multiplier < 1.5 at streak " + i);
        }
    }

    // --- isMilestone tests ---

    @Test
    void isMilestone_zeroIsNotMilestone() {
        assertFalse(StreakManager.isMilestone(0));
    }

    @Test
    void isMilestone_fourteenIsMilestone() {
        assertTrue(StreakManager.isMilestone(14));
    }

    @Test
    void isMilestone_multiplesOfFourteenAreMilestones() {
        assertTrue(StreakManager.isMilestone(28));
        assertTrue(StreakManager.isMilestone(42));
        assertTrue(StreakManager.isMilestone(56));
        assertTrue(StreakManager.isMilestone(140));
    }

    @Test
    void isMilestone_nonMultiplesAreNotMilestones() {
        assertFalse(StreakManager.isMilestone(1));
        assertFalse(StreakManager.isMilestone(7));
        assertFalse(StreakManager.isMilestone(13));
        assertFalse(StreakManager.isMilestone(15));
        assertFalse(StreakManager.isMilestone(27));
    }

    @Test
    void isMilestone_negativeIsNotMilestone() {
        assertFalse(StreakManager.isMilestone(-14));
        assertFalse(StreakManager.isMilestone(-1));
    }
}
