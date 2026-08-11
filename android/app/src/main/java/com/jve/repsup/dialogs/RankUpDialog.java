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
 * "CONGRATULATIONS! NEW RANK UNLOCKED" dialog displayed when a user ranks up.
 * Shows a medal icon, the new rank name, "+100 COINS", and Share/Close buttons.
 */
public class RankUpDialog extends Dialog {

    private final String rankName;

    public RankUpDialog(@NonNull Context context, String rankName) {
        super(context, R.style.Theme_RepsUp_Dialog);
        this.rankName = rankName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rank_up);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvRankName = findViewById(R.id.tvRankName);
        TextView btnShare = findViewById(R.id.btnShare);
        TextView btnClose = findViewById(R.id.btnClose);

        tvRankName.setText(rankName);

        btnShare.setOnClickListener(v -> shareRankUp());
        btnClose.setOnClickListener(v -> dismiss());
    }

    /**
     * Invokes native Android share intent with rank name and bonus coins.
     */
    private void shareRankUp() {
        String shareText = getContext().getString(R.string.rank_up_share_text, rankName);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        getContext().startActivity(Intent.createChooser(shareIntent, getContext().getString(R.string.rank_up_share)));
        dismiss();
    }
}
