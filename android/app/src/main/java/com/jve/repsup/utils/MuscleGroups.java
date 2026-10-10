package com.jve.repsup.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The one list of muscle groups the app knows about. The names must match the
 * muscle_group column of the exercise table (see database/seed.sql).
 */
public final class MuscleGroups {

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            "Chest", "Back", "Shoulders", "Biceps", "Triceps",
            "Quads", "Hamstrings", "Glutes", "Abs", "Calves", "Forearms"
    ));

    private static final List<String> LOWER_BODY =
            Arrays.asList("Quads", "Hamstrings", "Glutes", "Calves");

    private MuscleGroups() {}

    /** True for the muscle groups that make up a lower-body workout. */
    public static boolean isLowerBody(String group) {
        return LOWER_BODY.contains(group);
    }
}
