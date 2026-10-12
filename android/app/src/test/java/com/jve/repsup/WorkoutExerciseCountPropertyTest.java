package com.jve.repsup;

import com.jve.repsup.utils.WorkoutValidator;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for workout exercise count bounds.
 * Uses jqwik to generate random exercise counts and verify acceptance/rejection.
 *
 * Validates: Requirements 12.4, 25.4
 */
class WorkoutExerciseCountPropertyTest {

    // ─── Property 8: Workout Exercise Count Bounded ───

    /**
     * Property 8a: Any exercise count in [1..10] is accepted.
     * For all counts between MIN_EXERCISES and MAX_EXERCISES inclusive,
     * isValidExerciseCount returns true.
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Property(tries = 200)
    void validExerciseCountsAreAccepted(
            @ForAll @IntRange(min = 1, max = 10) int count
    ) {
        assertTrue(WorkoutValidator.isValidExerciseCount(count),
                "Exercise count " + count + " should be accepted (within [1..10])");
    }

    /**
     * Property 8b: Any exercise count outside [1..10] is rejected.
     * For counts 0 and 11-20, isValidExerciseCount returns false.
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Property(tries = 200)
    void invalidExerciseCountsAreRejected(
            @ForAll("invalidExerciseCounts") int count
    ) {
        assertFalse(WorkoutValidator.isValidExerciseCount(count),
                "Exercise count " + count + " should be rejected (outside [1..10])");
    }

    /**
     * Property 8c: For any random count in [0..20], the validator correctly
     * partitions into accepted (1-10) and rejected (0, 11-20).
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Property(tries = 200)
    void exerciseCountPartitionsCorrectly(
            @ForAll @IntRange(min = 0, max = 20) int count
    ) {
        boolean result = WorkoutValidator.isValidExerciseCount(count);
        if (count >= 1 && count <= 10) {
            assertTrue(result, "Count " + count + " is in [1..10] and should be accepted");
        } else {
            assertFalse(result, "Count " + count + " is outside [1..10] and should be rejected");
        }
    }

    /**
     * Property 8d: validateExerciseList correctly validates list sizes.
     * A list of size n is accepted iff n is in [1..10]; null lists are rejected.
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Property(tries = 200)
    void exerciseListValidationMatchesCountValidation(
            @ForAll @IntRange(min = 0, max = 20) int size
    ) {
        List<Object> exercises = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            exercises.add(new Object());
        }
        boolean expected = WorkoutValidator.isValidExerciseCount(size);
        assertEquals(expected, WorkoutValidator.validateExerciseList(exercises),
                "List validation for size " + size + " should match count validation");
    }

    // ─── Boundary Examples ───

    /**
     * Boundary: count 0 is rejected (below minimum).
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Example
    void zeroExercisesIsRejected() {
        assertFalse(WorkoutValidator.isValidExerciseCount(0),
                "0 exercises must be rejected");
    }

    /**
     * Boundary: count 1 is accepted (lower bound).
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Example
    void oneExerciseIsAccepted() {
        assertTrue(WorkoutValidator.isValidExerciseCount(1),
                "1 exercise must be accepted (lower bound)");
    }

    /**
     * Boundary: count 10 is accepted (upper bound).
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Example
    void tenExercisesIsAccepted() {
        assertTrue(WorkoutValidator.isValidExerciseCount(10),
                "10 exercises must be accepted (upper bound)");
    }

    /**
     * Boundary: count 11 is rejected (above maximum).
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Example
    void elevenExercisesIsRejected() {
        assertFalse(WorkoutValidator.isValidExerciseCount(11),
                "11 exercises must be rejected");
    }

    /**
     * Null list is always rejected.
     *
     * Validates: Requirements 12.4, 25.4
     */
    @Example
    void nullListIsRejected() {
        assertFalse(WorkoutValidator.validateExerciseList(null),
                "Null exercise list must be rejected");
    }

    // ─── Custom Providers ───

    @Provide
    Arbitrary<Integer> invalidExerciseCounts() {
        return Arbitraries.oneOf(
                Arbitraries.just(0),
                Arbitraries.integers().between(11, 20)
        );
    }
}
