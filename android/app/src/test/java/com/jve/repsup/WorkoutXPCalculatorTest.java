package com.jve.repsup;

import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.utils.XPCalculator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for XPCalculator.calculateWorkoutXP().
 * Validates the formula: sum(base_xp * sets_completed) * streak_multiplier
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
}
