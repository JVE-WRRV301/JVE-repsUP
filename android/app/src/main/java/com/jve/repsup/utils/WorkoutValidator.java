package com.jve.repsup.utils;

import java.util.List;

/**
 * Utility class for workout validation logic.
 * Centralizes exercise count bounds that were previously only enforced in the UI layer.
 */
public class WorkoutValidator {

    /** Minimum number of exercises allowed in a workout. */
    public static final int MIN_EXERCISES = 1;

    /** Maximum number of exercises allowed in a workout. */
    public static final int MAX_EXERCISES = 10;

    /**
     * Validates whether the given exercise count falls within the accepted bounds [1..10].
     *
     * @param count the number of exercises
     * @return true if count is between MIN_EXERCISES and MAX_EXERCISES inclusive
     */
    public static boolean isValidExerciseCount(int count) {
        return count >= MIN_EXERCISES && count <= MAX_EXERCISES;
    }

    /**
     * Validates whether the given exercise list has a valid size.
     * A valid list is non-null and contains between 1 and 10 exercises inclusive.
     *
     * @param exercises the list of exercises to validate
     * @return true if the list is non-null and its size is between MIN_EXERCISES and MAX_EXERCISES
     */
    public static boolean validateExerciseList(List<?> exercises) {
        if (exercises == null) {
            return false;
        }
        return isValidExerciseCount(exercises.size());
    }
}
