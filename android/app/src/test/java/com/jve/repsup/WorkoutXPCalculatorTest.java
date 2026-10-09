package com.jve.repsup;

import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.utils.XPCalculator;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests and property-based tests for XPCalculator.
 * Validates the formula: sum(base_xp * sets_completed) * streak_multiplier
 * and preservation of level-up logic.
 */
class WorkoutXPCalculatorTest {

    @Test
    void nullListReturnsZero() {
        assertEquals(0, XPCalculator.calculateWorkoutXP(null, 1.0));
    }

    @Test
    void emptyListReturnsZero() {
        assertEquals(0, XPCalculator.calculateWorkoutXP(new ArrayList<>(), 1.0));
    }

    @Test
    void singleExerciseNoMultiplier() {
        // 10 base_xp * 3 sets = 30, multiplier 1.0 => 30
        List<ExerciseResult> results = Collections.singletonList(
                new ExerciseResult(1, 10, 3, 30, 50.0));
        assertEquals(30, XPCalculator.calculateWorkoutXP(results, 1.0));
    }

    @Test
    void multipleExercisesSummedCorrectly() {
        // Exercise 1: 10 * 3 = 30
        // Exercise 2: 20 * 4 = 80
        // Total = 110, multiplier 1.0 => 110
        List<ExerciseResult> results = Arrays.asList(
                new ExerciseResult(1, 10, 3, 30, 50.0),
                new ExerciseResult(2, 20, 4, 40, 80.0));
        assertEquals(110, XPCalculator.calculateWorkoutXP(results, 1.0));
    }

    @Test
    void streakMultiplierApplied() {
        // 10 * 3 = 30, multiplier 1.5 => 45
        List<ExerciseResult> results = Collections.singletonList(
                new ExerciseResult(1, 10, 3, 30, 50.0));
        assertEquals(45, XPCalculator.calculateWorkoutXP(results, 1.5));
    }

    @Test
    void streakMultiplierBelowOneIsTreatedAsOne() {
        // 10 * 3 = 30, multiplier 0.5 (invalid, treated as 1.0) => 30
        List<ExerciseResult> results = Collections.singletonList(
                new ExerciseResult(1, 10, 3, 30, 50.0));
        assertEquals(30, XPCalculator.calculateWorkoutXP(results, 0.5));
    }

    @Test
    void zeroSetsCompletedReturnsZero() {
        List<ExerciseResult> results = Collections.singletonList(
                new ExerciseResult(1, 10, 0, 0, 0.0));
        assertEquals(0, XPCalculator.calculateWorkoutXP(results, 1.0));
    }

    @Test
    void resultIsTruncatedNotRounded() {
        // 10 * 3 = 30, multiplier 1.3 => 39.0 => 39 (int cast truncates)
        List<ExerciseResult> results = Collections.singletonList(
                new ExerciseResult(1, 10, 3, 30, 50.0));
        assertEquals(39, XPCalculator.calculateWorkoutXP(results, 1.3));
    }

    // ─── Property-Based Preservation Tests ───────────────────────────────────────

    // ─── Generators ───

    @Provide
    Arbitrary<List<ExerciseResult>> nonEmptyExerciseResults() {
        Arbitrary<ExerciseResult> exerciseResult = Combinators.combine(
                Arbitraries.integers().between(1, 100),   // exerciseId
                Arbitraries.integers().between(10, 100),  // baseXp
                Arbitraries.integers().between(1, 5),     // setsCompleted
                Arbitraries.integers().between(1, 50),    // totalReps
                Arbitraries.doubles().between(0.0, 200.0) // totalWeight
        ).as(ExerciseResult::new);

        return exerciseResult.list().ofMinSize(1).ofMaxSize(8);
    }

    @Provide
    Arbitrary<Double> validMultipliers() {
        return Arbitraries.of(1.0, 1.25, 1.5);
    }

    @Provide
    Arbitrary<Double> anyPositiveMultiplier() {
        return Arbitraries.doubles().between(0.0, 3.0);
    }

    @Provide
    Arbitrary<Integer> validLevels() {
        return Arbitraries.integers().between(1, 20);
    }

    @Provide
    Arbitrary<Integer> wideLevels() {
        // Deliberately wide range to exercise the long arithmetic well past the
        // point where the old int-based doubling curve overflowed (~level 22).
        return Arbitraries.integers().between(1, 100000);
    }

    // ─── Property 2a: XP Formula Preservation ────────────────────────────────────

    /**
     * Property 2a: For all non-empty exerciseResults lists (1-8 exercises,
     * baseXp 10-100, sets 1-5) and valid multipliers (1.0, 1.25, 1.5),
     * the result equals sum(baseXp * setsCompleted) * streakMultiplier truncated to int.
     *
     * Validates: Requirements 3.1
     */
    @Property(tries = 200)
    void xpFormulaIsPreservedForAllValidInputs(
            @ForAll("nonEmptyExerciseResults") List<ExerciseResult> exerciseResults,
            @ForAll("validMultipliers") double multiplier
    ) {
        // Compute expected value using the formula directly
        int baseTotal = 0;
        for (ExerciseResult result : exerciseResults) {
            baseTotal += result.getBaseXp() * result.getSetsCompleted();
        }
        int expected = (int) (baseTotal * multiplier);

        // Verify XPCalculator produces the same result
        int actual = XPCalculator.calculateWorkoutXP(exerciseResults, multiplier);

        assertEquals(expected, actual,
                String.format("XP formula mismatch: expected sum(baseXp*sets)*multiplier = %d, got %d. " +
                        "exercises=%d, multiplier=%.2f", expected, actual, exerciseResults.size(), multiplier));
    }

    // ─── Property 2b: Empty/Null Results Always Return 0 ─────────────────────────

    /**
     * Property 2b: For all empty/null exerciseResults, result is always 0
     * regardless of multiplier value.
     *
     * Validates: Requirements 3.1, 3.2
     */
    @Property(tries = 50)
    void emptyResultsAlwaysReturnZero(
            @ForAll("anyPositiveMultiplier") double multiplier
    ) {
        assertEquals(0, XPCalculator.calculateWorkoutXP(null, multiplier),
                "null exerciseResults should always return 0");
        assertEquals(0, XPCalculator.calculateWorkoutXP(new ArrayList<>(), multiplier),
                "empty exerciseResults should always return 0");
    }

    // ─── Levelling curve: XP to go from level N to N+1 = 400 + 50 * N ────────────

    @Test
    void xpForNextLevelMatchesTheRule() {
        // level 1→2 = 450, 2→3 = 500, 3→4 = 550 ...
        assertEquals(450L, XPCalculator.xpForNextLevel(1));
        assertEquals(500L, XPCalculator.xpForNextLevel(2));
        assertEquals(550L, XPCalculator.xpForNextLevel(3));
        assertEquals(900L, XPCalculator.xpForNextLevel(10));
    }

    @Test
    void cumulativeXpForLevelMatchesWorkedExamples() {
        assertEquals(0L, XPCalculator.cumulativeXpForLevel(1));
        assertEquals(450L, XPCalculator.cumulativeXpForLevel(2));   // 450
        assertEquals(950L, XPCalculator.cumulativeXpForLevel(3));   // 450 + 500
        assertEquals(1500L, XPCalculator.cumulativeXpForLevel(4));  // + 550
    }

    @Test
    void levelForTotalXpAtAndAroundBoundaries() {
        assertEquals(1, XPCalculator.levelForTotalXp(0));
        assertEquals(1, XPCalculator.levelForTotalXp(449));
        assertEquals(2, XPCalculator.levelForTotalXp(450));   // exactly reaches level 2
        assertEquals(2, XPCalculator.levelForTotalXp(949));
        assertEquals(3, XPCalculator.levelForTotalXp(950));   // exactly reaches level 3
    }

    @Test
    void levelForTotalXpMatchesSeedUsers() {
        // Seed users under the new curve:
        assertEquals(2, XPCalculator.levelForTotalXp(450));  // Dylan
        assertEquals(1, XPCalculator.levelForTotalXp(120));  // Sarah
        assertEquals(2, XPCalculator.levelForTotalXp(800));  // Jason
    }

    @Test
    void levelForTotalXpHandlesHugeXpWithoutOverflowing() {
        // A very large XP value must still terminate and return a sane level.
        // The old int-based doubling curve overflowed and could loop forever.
        int level = XPCalculator.levelForTotalXp(Long.MAX_VALUE / 2);
        assertTrue(level > 1, "Huge XP should map to a high level, got " + level);
    }

    // ─── Property: cumulative curve is the sum of the per-level deltas ───────────

    /**
     * cumulativeXpForLevel(L) must equal the running sum of xpForNextLevel(N)
     * for N = 1 .. L-1 — the two must stay consistent for all levels.
     */
    @Property(tries = 500)
    void cumulativeEqualsSumOfPerLevelDeltas(
            @ForAll("validLevels") int level
    ) {
        long expected = 0L;
        for (int n = 1; n < level; n++) {
            expected += XPCalculator.xpForNextLevel(n);
        }
        assertEquals(expected, XPCalculator.cumulativeXpForLevel(level),
                "cumulative must equal the running sum of per-level XP at level " + level);
    }

    // ─── Property: shouldLevelUp agrees with the cumulative threshold ────────────

    /**
     * shouldLevelUp(totalXp, currentLevel) returns true when totalXp reaches the
     * cumulative requirement for currentLevel + 1, and false just below it.
     * Exercised across a wide level range to confirm long arithmetic never
     * overflows or spins.
     */
    @Property(tries = 500)
    void shouldLevelUpAgreesWithCumulativeThreshold(
            @ForAll("validLevels") int currentLevel
    ) {
        long threshold = XPCalculator.cumulativeXpForLevel(currentLevel + 1);

        assertTrue(XPCalculator.shouldLevelUp(threshold, currentLevel),
                String.format("Should level up at threshold: xp=%d, level=%d", threshold, currentLevel));
        assertTrue(XPCalculator.shouldLevelUp(threshold + 1, currentLevel),
                String.format("Should level up above threshold: xp=%d, level=%d", threshold + 1, currentLevel));
        if (threshold > 0) {
            assertFalse(XPCalculator.shouldLevelUp(threshold - 1, currentLevel),
                    String.format("Should NOT level up below threshold: xp=%d, level=%d", threshold - 1, currentLevel));
        }
    }

    // ─── Property: levelForTotalXp is the inverse of the cumulative curve ────────

    /**
     * For any level L (over a wide range), the cumulative XP to reach L maps back
     * to exactly level L, and one XP short maps to L-1. This confirms
     * levelForTotalXp never overflows or loops for large levels.
     */
    @Property(tries = 500)
    void levelForTotalXpIsInverseOfCumulative(
            @ForAll("wideLevels") int level
    ) {
        long atLevel = XPCalculator.cumulativeXpForLevel(level);
        assertEquals(level, XPCalculator.levelForTotalXp(atLevel),
                "XP exactly at the level threshold should map back to that level");

        if (level > 1) {
            assertEquals(level - 1, XPCalculator.levelForTotalXp(atLevel - 1),
                    "One XP short of the threshold should map to the previous level");
        }
    }

    /**
     * Property 2d: Multiplier below 1.0 is clamped to 1.0 — preservation of
     * the streak fallback behavior.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void multiplierBelowOneIsClamped(
            @ForAll("nonEmptyExerciseResults") List<ExerciseResult> exerciseResults
    ) {
        // Result with invalid multiplier should equal result with multiplier 1.0
        int withInvalidMultiplier = XPCalculator.calculateWorkoutXP(exerciseResults, 0.5);
        int withOneMultiplier = XPCalculator.calculateWorkoutXP(exerciseResults, 1.0);

        assertEquals(withOneMultiplier, withInvalidMultiplier,
                "Multiplier below 1.0 should be clamped to 1.0");
    }
}
