package com.jve.repsup;

import com.jve.repsup.utils.StreakManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StreakManager utility class.
 * Tests all public methods: evaluateStreak, evaluateStreakOnLaunch,
 * getMultiplier, isMilestone.
 *
 * The milestone interval is 7 days: multipliers step up at 7, 14, 21, ...
 */
class StreakManagerTest {

    // --- evaluateStreak tests (completion-time: may increment or reset) ---

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

    // --- evaluateStreakOnLaunch tests (launch check: may ONLY reset) ---

    @Test
    void evaluateStreakOnLaunch_scheduledAndNotCompleted_resetsStreak() {
        assertEquals(0, StreakManager.evaluateStreakOnLaunch(0, true, false));
        assertEquals(0, StreakManager.evaluateStreakOnLaunch(10, true, false));
        assertEquals(0, StreakManager.evaluateStreakOnLaunch(100, true, false));
    }

    @Test
    void evaluateStreakOnLaunch_scheduledAndCompleted_neverIncrements() {
        // The +1 already happened at completion time, so the launch check must
        // leave the streak unchanged rather than counting it again.
        assertEquals(0, StreakManager.evaluateStreakOnLaunch(0, true, true));
        assertEquals(5, StreakManager.evaluateStreakOnLaunch(5, true, true));
        assertEquals(14, StreakManager.evaluateStreakOnLaunch(14, true, true));
    }

    @Test
    void evaluateStreakOnLaunch_noWorkoutScheduled_streakUnchanged() {
        assertEquals(0, StreakManager.evaluateStreakOnLaunch(0, false, false));
        assertEquals(5, StreakManager.evaluateStreakOnLaunch(5, false, false));
        assertEquals(14, StreakManager.evaluateStreakOnLaunch(14, false, true));
    }

    @Test
    void evaluateStreakOnLaunch_neverIncreasesStreak() {
        // Across every input combination, the launch check result is never
        // greater than the current streak.
        for (int streak = 0; streak <= 50; streak++) {
            for (boolean scheduled : new boolean[]{false, true}) {
                for (boolean completed : new boolean[]{false, true}) {
                    int result = StreakManager.evaluateStreakOnLaunch(streak, scheduled, completed);
                    assertTrue(result <= streak,
                            "Launch check increased streak from " + streak + " to " + result
                                    + " (scheduled=" + scheduled + ", completed=" + completed + ")");
                }
            }
        }
    }

    // --- getMultiplier tests (7-day interval) ---

    @Test
    void getMultiplier_belowSeven_returnsBaseMultiplier() {
        assertEquals(1.0, StreakManager.getMultiplier(0));
        assertEquals(1.0, StreakManager.getMultiplier(1));
        assertEquals(1.0, StreakManager.getMultiplier(6));
    }

    @Test
    void getMultiplier_atSeven_returnsFirstBonus() {
        assertEquals(1.5, StreakManager.getMultiplier(7));
    }

    @Test
    void getMultiplier_betweenSevenAndFourteen_returnsFirstBonus() {
        assertEquals(1.5, StreakManager.getMultiplier(8));
        assertEquals(1.5, StreakManager.getMultiplier(13));
    }

    @Test
    void getMultiplier_atFourteen_returnsSecondBonus() {
        assertEquals(2.0, StreakManager.getMultiplier(14));
    }

    @Test
    void getMultiplier_atTwentyOne_returnsThirdBonus() {
        assertEquals(2.5, StreakManager.getMultiplier(21));
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
    void getMultiplier_atLeastOnePointFiveForSevenOrAbove() {
        for (int i = 7; i <= 200; i++) {
            assertTrue(StreakManager.getMultiplier(i) >= 1.5,
                    "Multiplier < 1.5 at streak " + i);
        }
    }

    // --- isMilestone tests (positive multiples of 7) ---

    @Test
    void isMilestone_zeroIsNotMilestone() {
        assertFalse(StreakManager.isMilestone(0));
    }

    @Test
    void isMilestone_sevenIsMilestone() {
        assertTrue(StreakManager.isMilestone(7));
    }

    @Test
    void isMilestone_multiplesOfSevenAreMilestones() {
        assertTrue(StreakManager.isMilestone(14));
        assertTrue(StreakManager.isMilestone(21));
        assertTrue(StreakManager.isMilestone(28));
        assertTrue(StreakManager.isMilestone(70));
    }

    @Test
    void isMilestone_nonMultiplesAreNotMilestones() {
        assertFalse(StreakManager.isMilestone(1));
        assertFalse(StreakManager.isMilestone(6));
        assertFalse(StreakManager.isMilestone(8));
        assertFalse(StreakManager.isMilestone(13));
        assertFalse(StreakManager.isMilestone(15));
    }

    @Test
    void isMilestone_negativeIsNotMilestone() {
        assertFalse(StreakManager.isMilestone(-7));
        assertFalse(StreakManager.isMilestone(-1));
    }
}
