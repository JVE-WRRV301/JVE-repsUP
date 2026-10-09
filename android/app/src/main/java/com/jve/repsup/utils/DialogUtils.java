package com.jve.repsup.utils;

import android.content.Context;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jve.repsup.R;

/**
 * DialogUtils centralises simple user-facing dialogs so error/failure feedback
 * is consistent across the app. Error messages that previously used short-lived
 * Toasts are shown here as an AlertDialog the user must dismiss, so failures are
 * noticed rather than flashing by.
 *
 * Validation hints and brief success confirmations intentionally remain Toasts.
 */
public final class DialogUtils {

    private DialogUtils() {
        // Utility class — no instances.
    }

    /**
     * Shows an error dialog with a string resource message.
     * Safe to call with a null context (no-op), which can happen in async
     * callbacks after a screen has been torn down.
     */
    public static void showError(Context context, @StringRes int messageResId) {
        if (context == null) return;
        showError(context, context.getString(messageResId));
    }

    /**
     * Shows an error dialog with a literal message string.
     * Safe to call with a null context (no-op).
     */
    public static void showError(Context context, String message) {
        if (context == null || message == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.error_dialog_ok, null)
                .show();
    }

    /**
     * Shows an error dialog (string resource) and runs the given action once the
     * dialog is dismissed. Useful for terminal errors where the screen should
     * close only after the user has seen and acknowledged the message.
     * Safe to call with a null context (no-op).
     */
    public static void showErrorThen(Context context, @StringRes int messageResId,
                                     Runnable onDismiss) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_dialog_title)
                .setMessage(messageResId)
                .setPositiveButton(R.string.error_dialog_ok, null)
                .setOnDismissListener(d -> {
                    if (onDismiss != null) onDismiss.run();
                })
                .show();
    }
}
