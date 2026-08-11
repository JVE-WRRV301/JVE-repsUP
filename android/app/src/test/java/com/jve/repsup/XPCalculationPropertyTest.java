package com.jve.repsup;

import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.utils.XPCalculator;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for XP Calculation (Property 1).
 * Verifies that workout XP is non-negative and monotonically non-decreasing
 * as sets_completed increases for any exercise.
 *
 * Uses jqwik to generate arbitrary exercise result lists and streak multipliers.
 *
 * Validates: Requirements 15.2, 17.1
 */
class XPCalculationPropertyTest {

    // ─── Property 1a: XP is Always Non-Negative ───

    /**
     * Property 1a: For any list of exercise results with baseXp >= 0 and
     * setsCompleted >= 0, and any streak multiplier >= 1.0, the calculated
     * workout XP is always non-negative.
     *
     * Validates: Requirements 15.2, 17.1
     */
    @Property(tries = 200)
    void workoutXPIsAlwaysNonNegative(
            @ForAll("exerciseResultLists") List<ExerciseResult> results,
            @ForAll("streakMultipliers") Double multiplier
    ) {
        int xp = XPCalculator.calculateWorkoutXP(results, multiplier);
        assertTrue(xp >= 0,
                "Workout XP must be non-negative, but got: " + xp);
    }

    // ─── Property 1b: XP is Monotonically Non-Decreasing as Sets Increase ───

    /**
     * Property 1b: Increasing sets_completed for any exercise in the list
     * should yield XP that is >= the original XP.
     * This verifies monotonicity — more sets means equal or more XP.
     *
     * Validates: Requirements 15.2, 17.1
     */
    @Property(tries = 200)
    void workoutXPIsMonotonicWithSetsCompleted(
            @ForAll("nonEmptyExerciseResultLists") List<ExerciseResult> results,
            @ForAll("streakMultipliers") Double multiplier,
            @ForAll @IntRange(min = 1, max = 10) int additionalSets
    ) {
        // Calculate original XP
        int originalXP = XPCalculator.calculateWorkoutXP(results, multiplier);

        // Pick the first exercise and increase its sets
        List<ExerciseResult> modifiedResults = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ExerciseResult original = results.get(i);
            if (i == 0) {
                // Increase sets_completed for the first exercise
                ExerciseResult modified = new ExerciseResult(
                        original.getExerciseId(),
                        original.getBaseXp(),
                        original.getSetsCompleted() + additionalSets,
                        original.getTotalReps(),
                        original.getTotalWeight()
                );
                modifiedResults.add(modified);
            } else {
                modifiedResults.add(original);
            }
        }

        int increasedXP = XPCalculator.calculateWorkoutXP(modifiedResults, multiplier);

        assertTrue(increasedXP >= originalXP,
                "XP must be monotonically non-decreasing when sets increase. " +
                        "Original XP: " + originalXP + ", Increased XP: " + increasedXP);
    }

    // ─── Edge Cases ───

    /**
     * Edge case: Empty list always returns 0 XP.
     *
     * Validates: Requirements 15.2, 17.1
     */
    @Example
    void emptyListReturns0XP() {
        int xp = XPCalculator.calculateWorkoutXP(new ArrayList<>(), 1.5);
        assertEquals(0, xp, "Empty exercise list must yield 0 XP");
    }

    /**
     * Edge case: Null list always returns 0 XP.
     *
     * Validates: Requirements 15.2, 17.1
     */
    @Example
    void nullListReturns0XP() {
        int xp = XPCalculator.calculateWorkoutXP(null, 2.0);
        assertEquals(0, xp, "Null exercise list must yield 0 XP");
    }

    /**
     * Edge case: Single exercise result with multiplier exactly 1.0.
     * XP should equal baseXp * setsCompleted.
     *
     * Validates: Requirements 15.2, 17.1
     */
    @Example
    void singleExerciseWithMultiplier1() {
        ExerciseResult result = new ExerciseResult(1, 50, 3, 30, 100.0);
        List<ExerciseResult> results = new ArrayList<>();
        results.add(result);

        int xp = XPCalculator.calculateWorkoutXP(results, 1.0);
        assertEquals(150, xp, "Single exercise: 50 baseXp * 3 sets * 1.0 multiplier = 150 XP");
    }

    /**
     * Edge case: Multiplier exactly 1.0 should not modify the base total.
     *
     * Validates: Requirements 15.2, 17.1
     */
    @Property(tries = 100)
    void multiplierOf1DoesNotChangeBaseTotal(
            @ForAll("exerciseResultLists") List<ExerciseResult> results
    ) {
        int xpWithMultiplier1 = XPCalculator.calculateWorkoutXP(results, 1.0);

        // Calculate expected base total manually
        int expectedBase = 0;
        for (ExerciseResult r : results) {
            expectedBase += r.getBaseXp() * r.getSetsCompleted();
        }

        assertEquals(expectedBase, xpWithMultiplier1,
                "With multiplier 1.0, XP must equal the sum of (baseXp * setsCompleted)");
    }

    // ─── Custom Providers ───

    @Provide
    Arbitrary<List<ExerciseResult>> exerciseResultLists() {
        return exerciseResultArbitrary()
                .list()
                .ofMinSize(0)
                .ofMaxSize(8);
    }

    @Provide
    Arbitrary<List<ExerciseResult>> nonEmptyExerciseResultLists() {
        return exerciseResultArbitrary()
                .list()
                .ofMinSize(1)
                .ofMaxSize(8);
    }

    @Provide
    Arbitrary<Double> streakMultipliers() {
        return Arbitraries.doubles()
                .between(1.0, 5.0);
    }

    private Arbitrary<ExerciseResult> exerciseResultArbitrary() {
        Arbitrary<Integer> exerciseIds = Arbitraries.integers().between(1, 100);
        Arbitrary<Integer> baseXps = Arbitraries.integers().between(0, 200);
        Arbitrary<Integer> setsCompleted = Arbitraries.integers().between(0, 10);
        Arbitrary<Integer> totalReps = Arbitraries.integers().between(0, 100);
        Arbitrary<Double> totalWeights = Arbitraries.doubles().between(0.0, 300.0);

        return Combinators.combine(exerciseIds, baseXps, setsCompleted, totalReps, totalWeights)
                .as(ExerciseResult::new);
    }
}
