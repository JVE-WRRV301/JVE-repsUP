package com.jve.repsup.utils;

/**
 * Resolves the assets path for an exercise's animated demonstration.
 *
 * Demo files are stored under assets/exercise_demos/ using a normalized form of
 * the exercise name, e.g. "Barbell Bench Press" resolves to
 * "exercise_demos/barbell_bench_press.webp".
 *
 * Glide loads these via the "file:///android_asset/" scheme and auto-loops
 * animated GIF/WebP. When the file is absent, Glide falls back to the error
 * drawable supplied by the caller, so a missing demo never crashes playback.
 */
public class ExerciseDemoLocator {

    private static final String DEMO_DIR = "exercise_demos/";
    private static final String DEMO_EXT = ".webp";

    /** Glide/AssetManager URI prefix for files bundled in src/main/assets. */
    private static final String ANDROID_ASSET_URI = "file:///android_asset/";

    private ExerciseDemoLocator() {
        // Utility class - no instances
    }

    /**
     * Builds the Glide-loadable asset URI for an exercise's demo animation.
     *
     * @param exerciseName the exercise display name (e.g. "Push Up")
     * @return a "file:///android_asset/exercise_demos/push_up.webp" style URI,
     *         or null when the name is null/blank
     */
    public static String demoUriFor(String exerciseName) {
        String fileName = fileNameFor(exerciseName);
        if (fileName == null) {
            return null;
        }
        return ANDROID_ASSET_URI + DEMO_DIR + fileName;
    }

    /**
     * Builds just the demo file name (e.g. "push_up.webp") for an exercise,
     * or null when the name is null/blank. Exposed for testing.
     */
    static String fileNameFor(String exerciseName) {
        if (exerciseName == null) {
            return null;
        }
        String normalized = exerciseName
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_") // non-alphanumerics -> underscore
                .replaceAll("^_+|_+$", "");    // trim leading/trailing underscores
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized + DEMO_EXT;
    }
}
