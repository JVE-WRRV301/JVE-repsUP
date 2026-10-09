package com.jve.repsup.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jve.repsup.R;

/**
 * "CONGRATULATIONS!" dialog displayed when a user completes a goal.
 * Shows the bicep icon, goal title, XP earned, and Share/Close circular buttons.
 */
public class GoalCompleteDialog extends Dialog {

    private final String goalTitle;
    private final int xpEarned;

    public GoalCompleteDialog(@NonNull Context context, String goalTitle, int xpEarned) {
        super(context, R.style.Theme_RepsUp_Dialog);
        this.goalTitle = goalTitle;
        this.xpEarned = xpEarned;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_goal_complete);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvGoalTitle = findViewById(R.id.tvGoalTitle);
        TextView tvXpEarned = findViewById(R.id.tvXpEarned);
        ImageView btnShare = findViewById(R.id.btnShare);
        ImageView btnClose = findViewById(R.id.btnClose);

        tvGoalTitle.setText(goalTitle);
        tvXpEarned.setText(getContext().getString(R.string.goal_xp_earned_format, xpEarned));

        btnShare.setOnClickListener(v -> shareGoalCompletion());
        btnClose.setOnClickListener(v -> dismiss());
    }

    /**
     * Invokes native Android share intent with goal title and XP earned.
     */
    private void shareGoalCompletion() {
        String shareText = getContext().getString(R.string.goal_share_text, goalTitle, xpEarned);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        getContext().startActivity(Intent.createChooser(shareIntent, getContext().getString(R.string.goal_share)));
        dismiss();
    }
}
