package com.jve.repsup.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jve.repsup.R;

/**
 * Streak milestone dialog showing congratulations, fire icon, streak count,
 * XP multiplier, and share/close buttons.
 */
public class StreakMilestoneDialog extends Dialog {

    private static final String TAG = "StreakMilestoneDialog";

    private final int streakCount;
    private final double xpMultiplier;

    public StreakMilestoneDialog(@NonNull Context context, int streakCount, double xpMultiplier) {
        super(context, R.style.Theme_RepsUp_Dialog);
        this.streakCount = streakCount;
        this.xpMultiplier = xpMultiplier;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_streak_milestone);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }

        TextView tvMilestoneSubtitle = findViewById(R.id.tvMilestoneSubtitle);
        TextView tvStreakCount = findViewById(R.id.tvStreakCount);
        TextView tvXpMultiplier = findViewById(R.id.tvXpMultiplier);
        ImageButton btnShare = findViewById(R.id.btnShare);
        ImageButton btnClose = findViewById(R.id.btnClose);

        String multiplierFormatted = String.format("%.1f", xpMultiplier);

        // Calculate weeks for subtitle
        int weeks = streakCount / 7;
        String weeksText = weeks == 1 ? "1 week" : weeks + " weeks";
        tvMilestoneSubtitle.setText(getContext().getString(R.string.streak_milestone_subtitle, weeksText));

        tvStreakCount.setText(getContext().getString(R.string.streak_milestone_day_streak, streakCount));
        tvXpMultiplier.setText(getContext().getString(R.string.streak_milestone_xp_value, multiplierFormatted));

        btnShare.setOnClickListener(v -> shareStreakMilestone());
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void shareStreakMilestone() {
        // Do NOT dismiss() here. This dialog's onDismissListener navigates to the
        // home page, so dismissing before/while sharing would preempt the share
        // sheet and send the user home. Keeping the dialog open lets the native
        // share sheet open over it, and the user stays on the milestone screen.
        try {
            String multiplierFormatted = String.format("%.1f", xpMultiplier);
            String shareText = getContext().getString(
                    R.string.streak_share_text, streakCount, multiplierFormatted);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            getContext().startActivity(Intent.createChooser(shareIntent,
                    getContext().getString(R.string.streak_milestone_share)));
        } catch (Exception e) {
            Log.e(TAG, "Failed to open share sheet for streak milestone", e);
        }
    }
}
