package com.jve.repsup.utils;

import com.jve.repsup.R;

/**
 * Finds the demonstration picture for an exercise. Only a few exercises have
 * one so far; every other exercise gets the generic dumbbell placeholder.
 * Shared by the workout screen and the exercise detail popup so both use the
 * same pictures.
 */
public final class ExerciseDemo {

    private ExerciseDemo() {
        // Utility class — no instances.
    }

    /**
     * Maps an exercise name to its demonstration drawable. Uses keyword
     * matching so DB names like "Dumbbell Shoulder Press" or
     * "Dumbbell Lateral Raises" resolve correctly. Falls back to the generic
     * exercise placeholder when no specific demo exists.
     */
    public static int drawableFor(String exerciseName) {
        if (exerciseName == null) {
            return R.drawable.ic_exercise_placeholder;
        }

        String name = exerciseName.toLowerCase();

        if (name.contains("lateral rais")) {
            // Note: drawable filename has a typo ("lataeral") - kept as-is.
            return R.drawable.lataeral_raises_avatar;
        }
        if (name.contains("shoulder press")) {
            return R.drawable.shoulder_press_avatar;
        }
        if (name.contains("bench press")) {
            return R.drawable.bench_press_avatar;
        }

        return R.drawable.ic_exercise_placeholder;
    }

    /** True when the exercise has its own demo picture (not the placeholder). */
    public static boolean hasDemo(String exerciseName) {
        return drawableFor(exerciseName) != R.drawable.ic_exercise_placeholder;
    }
}
