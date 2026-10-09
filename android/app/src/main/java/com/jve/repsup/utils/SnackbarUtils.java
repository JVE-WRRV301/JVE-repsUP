package com.jve.repsup.utils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.jve.repsup.R;

/**
 * SnackbarUtils centralises in-app feedback messages (validation hints and brief
 * success confirmations) as Snackbars instead of Toasts.
 *
 * Why Snackbars: on modern Android, Toasts can't be restyled or made to last
 * longer, and they get overlapped by the soft keyboard's key-preview bubbles.
 * Snackbars are larger, readable, consistently styled, controllable in duration,
 * and anchored to the app's own view so they don't collide with system UI.
 *
 * Error/failure messages use DialogUtils instead; this is for lighter feedback.
 */
public final class SnackbarUtils {

    private SnackbarUtils() {
        // Utility class — no instances.
    }

    /**
     * Shows a Snackbar anchored to the given view (any view in the current
     * layout works; the Snackbar finds a suitable parent).
     */
    public static void show(View view, @StringRes int messageResId) {
        if (view == null) return;
        show(view, view.getContext().getString(messageResId));
    }

    /**
     * Shows a Snackbar anchored to the given view with a literal message.
     */
    public static void show(View view, String message) {
        if (view == null || message == null) return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        style(snackbar);
        snackbar.show();
    }

    /**
     * Shows a Snackbar from an Activity, anchored to its content root.
     * Convenient when there isn't a specific view handy.
     */
    public static void show(Activity activity, @StringRes int messageResId) {
        if (activity == null) return;
        View root = activity.findViewById(android.R.id.content);
        show(root, activity.getString(messageResId));
    }

    /**
     * Shows a Snackbar from an Activity with a literal message.
     */
    public static void show(Activity activity, String message) {
        if (activity == null) return;
        View root = activity.findViewById(android.R.id.content);
        show(root, message);
    }

    /**
     * Applies the app's dark theme styling and makes the text easier to read:
     * surface-coloured background, white text, up to three lines, and a
     * light-blue "Dismiss" action so the user can clear it immediately or let
     * it auto-hide.
     *
     * The background is set with a tint list, not setBackgroundColor, so
     * Material's rounded Snackbar background (radius_control) is kept instead of
     * being replaced by a flat rectangle. Side and bottom margins let it float.
     */
    private static void style(Snackbar snackbar) {
        View sbView = snackbar.getView();

        // Give the Snackbar a rounded (radius_control) background and tint it,
        // instead of setBackgroundColor which flattens it to a square rectangle.
        float radius = sbView.getResources().getDimension(R.dimen.radius_control);
        sbView.setBackground(buildRoundedBackground(sbView, radius));
        sbView.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(sbView.getContext(), R.color.dark_grey_surface)));

        // Side and bottom margins so the Snackbar floats above the screen edge.
        if (sbView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) sbView.getLayoutParams();
            int margin = (int) (16 * sbView.getResources().getDisplayMetrics().density);
            params.setMargins(margin, margin, margin, margin);
            sbView.setLayoutParams(params);
        }

        TextView text = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(sbView.getContext(), R.color.white));
            text.setTextSize(16f);
            text.setMaxLines(3);
        }

        snackbar.setActionTextColor(ContextCompat.getColor(sbView.getContext(), R.color.light_blue));
        snackbar.setAction(R.string.snackbar_dismiss, v -> snackbar.dismiss());
    }

    /**
     * Builds a surface-coloured rounded rectangle at the given corner radius,
     * used so the Snackbar keeps a rounded (radius_control) shape.
     */
    private static android.graphics.drawable.GradientDrawable buildRoundedBackground(
            View sbView, float radius) {
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radius);
        bg.setColor(ContextCompat.getColor(sbView.getContext(), R.color.dark_grey_surface));
        return bg;
    }
}
