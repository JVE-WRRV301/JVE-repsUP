package com.jve.repsup.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jve.repsup.R;

/**
 * "STREAK MILESTONE!" dialog displayed when the user reaches a 14-day streak milestone.
 * Shows a fire icon, streak count, XP multiplier value, and Share/Close buttons.
 */
public class StreakMilestoneDialog extends Dialog {

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
        }

        TextView tvStreakCount = findViewById(R.id.tvStreakCount);
        TextView tvXpMultiplier = findViewById(R.id.tvXpMultiplier);
        TextView btnShare = findViewById(R.id.btnShare);
        TextView btnClose = findViewById(R.id.btnClose);

        String multiplierFormatted = String.format("%.1f", xpMultiplier);

        tvStreakCount.setText(getContext().getString(R.string.streak_milestone_days, streakCount));
        tvXpMultiplier.setText(getContext().getString(R.string.streak_milestone_multiplier, multiplierFormatted));

        btnShare.setOnClickListener(v -> shareStreakMilestone());
        btnClose.setOnClickListener(v -> dismiss());
    }

    /**
     * Invokes native Android share intent with streak count and XP multiplier.
     */
    private void shareStreakMilestone() {
        String multiplierFormatted = String.format("%.1f", xpMultiplier);
        String shareText = getContext().getString(R.string.streak_share_text, streakCount, multiplierFormatted);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        getContext().startActivity(Intent.createChooser(shareIntent, getContext().getString(R.string.streak_milestone_share)));
        dismiss();
    }
}
