package com.jve.repsup.utils;

import android.app.Activity;
import android.view.View;
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
     * surface-coloured background, white text, two lines allowed, and a light-blue
     * "Dismiss" action so the user can clear it immediately or let it auto-hide.
     */
    private static void style(Snackbar snackbar) {
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(sbView.getContext(), R.color.dark_grey_surface));

        TextView text = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (text != null) {
            text.setTextColor(ContextCompat.getColor(sbView.getContext(), R.color.white));
            text.setTextSize(16f);
            text.setMaxLines(3);
        }

        snackbar.setActionTextColor(ContextCompat.getColor(sbView.getContext(), R.color.light_blue));
        snackbar.setAction(R.string.snackbar_dismiss, v -> snackbar.dismiss());
    }
}
