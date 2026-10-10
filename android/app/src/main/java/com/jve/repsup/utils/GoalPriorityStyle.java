package com.jve.repsup.utils;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;

/**
 * One place for how a goal's priority is shown (label and colour), so the Goals
 * list, the Home goal card and the Calendar day panel all match.
 *
 * Matching ignores case and a null priority is treated as Medium, so values saved
 * as "top"/"medium"/"low" (or null) still map correctly.
 */
public final class GoalPriorityStyle {

    private GoalPriorityStyle() {
        // Utility class — no instances.
    }

    /** Sets the badge text to the full label (e.g. "Top Priority") and colours it. */
    public static void apply(TextView badge, String priority) {
        Context context = badge.getContext();
        String normalised = priority == null ? "medium" : priority.trim().toLowerCase();
        int labelRes;
        int colourRes;
        switch (normalised) {
            case "top":
                labelRes = R.string.importance_top_priority;
                colourRes = R.color.red;
                break;
            case "low":
                labelRes = R.string.importance_low_priority;
                colourRes = R.color.green;
                break;
            default:
                labelRes = R.string.importance_medium_priority;
                colourRes = R.color.yellow;
                break;
        }
        badge.setText(context.getString(labelRes));
        badge.setTextColor(ContextCompat.getColor(context, colourRes));
    }
}
