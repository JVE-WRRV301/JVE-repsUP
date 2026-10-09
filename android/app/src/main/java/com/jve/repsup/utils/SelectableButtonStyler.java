package com.jve.repsup.utils;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jve.repsup.R;

/**
 * One place for the app's selectable and enable/disable button looks, so every
 * screen shows "selected", "not selected" and "disabled" the same way.
 *
 * Selected:      light-blue fill, dark text.
 * Not selected:  grey surface fill, white text (or the darkest grey when the
 *                button sits on a grey card, so it doesn't disappear into it).
 * Disabled:      grey surface fill, muted text (same as the assessment Next).
 *
 * Only the tint and text colour change; the button keeps its rounded shape.
 */
public final class SelectableButtonStyler {

    private SelectableButtonStyler() {
        // Utility class — no instances.
    }

    /** Selected / not-selected look for an option button on the page background. */
    public static void applySelected(TextView button, boolean selected) {
        apply(button, selected, R.color.dark_grey_surface);
    }

    /**
     * Selected / not-selected look for an option button that sits on a grey card
     * (e.g. the Metric/Imperial toggle in Settings).
     */
    public static void applySelectedOnCard(TextView button, boolean selected) {
        apply(button, selected, R.color.dark_grey_variant);
    }

    /** Enabled / disabled look for a primary action button (Next, Continue …). */
    public static void applyEnabled(TextView button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(1f);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                button.getContext(), enabled ? R.color.light_blue : R.color.dark_grey_surface)));
        button.setTextColor(ContextCompat.getColor(
                button.getContext(), enabled ? R.color.dark_text : R.color.muted));
    }

    private static void apply(TextView button, boolean selected, int unselectedColourRes) {
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                button.getContext(), selected ? R.color.light_blue : unselectedColourRes)));
        button.setTextColor(ContextCompat.getColor(
                button.getContext(), selected ? R.color.dark_text : R.color.white));
    }
}
