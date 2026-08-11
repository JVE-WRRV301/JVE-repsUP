package com.jve.repsup.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jve.repsup.R;

/**
 * "LEVEL UP!" dialog displayed when a user levels up.
 * Shows a bicep icon, the new level number, and "+50 COINS" reward.
 */
public class LevelUpDialog extends Dialog {

    private final int newLevel;

    public LevelUpDialog(@NonNull Context context, int newLevel) {
        super(context, R.style.Theme_RepsUp_Dialog);
        this.newLevel = newLevel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_level_up);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvNewLevel = findViewById(R.id.tvNewLevel);
        TextView btnClose = findViewById(R.id.btnClose);

        tvNewLevel.setText(getContext().getString(R.string.level_up_new_level, newLevel));

        btnClose.setOnClickListener(v -> dismiss());
    }
}
