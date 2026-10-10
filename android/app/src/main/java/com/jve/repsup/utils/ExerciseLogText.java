package com.jve.repsup.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;
import com.jve.repsup.models.ExerciseResult;

/**
 * Text for an exercise line of a completed workout: what was actually done,
 * e.g. "3 sets × 10 reps · 20 kg" with the weight in blue.
 */
public final class ExerciseLogText {

    private ExerciseLogText() {}

    /** Shows what was logged for an exercise; no log (or no sets done) means it was skipped. */
    public static void apply(TextView tv, ExerciseResult result) {
        Context context = tv.getContext();
        if (result == null || result.getSetsCompleted() <= 0) {
            tv.setText(R.string.calendar_exercise_skipped);
            return;
        }
        int sets = result.getSetsCompleted();
        int reps = Math.round((float) result.getTotalReps() / sets);
        if (result.getMaxWeight() <= 0) {
            tv.setText(context.getString(R.string.calendar_exercise_done_format, sets, reps));
            return;
        }
        String weight = WeightFormat.kg(context, result.getMaxWeight());
        String full = context.getString(R.string.calendar_exercise_done_weight_format, sets, reps, weight);
        SpannableString text = new SpannableString(full);
        int start = full.lastIndexOf(weight);
        if (start >= 0) {
            text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.light_blue)),
                    start, start + weight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(text);
    }
}
