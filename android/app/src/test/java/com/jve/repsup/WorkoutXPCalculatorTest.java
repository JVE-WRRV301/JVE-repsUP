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

    // ─── Property 2c: shouldLevelUp Preservation ─────────────────────────────────

    /**
     * Property 2c: shouldLevelUp(totalXp, currentLevel) returns true when
     * totalXp >= 500 * (2^currentLevel - 1), and false otherwise.
     *
     * The cumulative threshold to reach level N+1 is 500 * (2^N - 1).
     *
     * Validates: Requirements 3.6, 3.7
     */
    @Property(tries = 200)
    void shouldLevelUpIsCorrectForAllLevelsAndXp(
            @ForAll("validLevels") int currentLevel
    ) {
        int cumulativeThreshold = 500 * ((int) Math.pow(2, currentLevel) - 1);

        // Exactly at threshold — should level up
        assertTrue(XPCalculator.shouldLevelUp(cumulativeThreshold, currentLevel),
                String.format("Should level up at threshold: totalXp=%d, level=%d, threshold=%d",
                        cumulativeThreshold, currentLevel, cumulativeThreshold));

        // Above threshold — should level up
        assertTrue(XPCalculator.shouldLevelUp(cumulativeThreshold + 1, currentLevel),
                String.format("Should level up above threshold: totalXp=%d, level=%d",
                        cumulativeThreshold + 1, currentLevel));

        // Below threshold — should NOT level up
        if (cumulativeThreshold > 0) {
            assertFalse(XPCalculator.shouldLevelUp(cumulativeThreshold - 1, currentLevel),
                    String.format("Should NOT level up below threshold: totalXp=%d, level=%d, threshold=%d",
                            cumulativeThreshold - 1, currentLevel, cumulativeThreshold));
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
