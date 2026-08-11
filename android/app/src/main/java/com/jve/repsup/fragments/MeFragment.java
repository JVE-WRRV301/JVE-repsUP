package com.jve.repsup.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.CustomizeAvatarActivity;
import com.jve.repsup.activities.SettingsActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Rank;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.XPCalculator;

/**
 * MeFragment displaying the user's avatar, gym rank with medal icon,
 * current level with progress indicator, gear icon → SettingsActivity,
 * and locker icon → CustomizeAvatarActivity.
 *
 * Validates: Requirements 20.1, 20.2, 20.3, 20.4, 20.5
 */
public class MeFragment extends Fragment {

    private ImageButton btnSettings;
    private ImageButton btnLocker;
    private ImageView ivAvatar;
    private ImageView ivMedal;
    private TextView tvRankName;
    private TextView tvLevelLabel;
    private ProgressBar pbLevelProgress;
    private TextView tvLevelProgress;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    public MeFragment() {
        // Required empty public constructor
    }

    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository();
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupClickListeners();
        loadUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void initViews(View view) {
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLocker = view.findViewById(R.id.btnLocker);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivMedal = view.findViewById(R.id.ivMedal);
        tvRankName = view.findViewById(R.id.tvRankName);
        tvLevelLabel = view.findViewById(R.id.tvLevelLabel);
        pbLevelProgress = view.findViewById(R.id.pbLevelProgress);
        tvLevelProgress = view.findViewById(R.id.tvLevelProgress);
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        btnLocker.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CustomizeAvatarActivity.class));
        });
    }

    /**
     * Loads the user's profile data (rank, level, XP) from the database
     * and updates the UI accordingly.
     */
    private void loadUserProfile() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (user == null) return;
                    displayUserProfile(user);
                });
            }

            @Override
            public void onError(Exception e) {
                // Silently fail — don't disrupt UI
            }
        });
    }

    /**
     * Updates the UI with the user's rank and level progress.
     */
    private void displayUserProfile(User user) {
        // Display rank name
        if (user.getRankName() != null && !user.getRankName().isEmpty()) {
            tvRankName.setText(user.getRankName());
        } else {
            tvRankName.setText(R.string.me_rank_default);
        }

        // Display level progress
        int currentLevel = user.getCurrentLevel();
        int totalXp = user.getXp();
        int xpForNext = XPCalculator.xpForNextLevel(currentLevel);

        // Calculate XP progress within current level
        int cumulativeXpForCurrentLevel = 0;
        for (int i = 1; i < currentLevel; i++) {
            cumulativeXpForCurrentLevel += XPCalculator.xpForNextLevel(i);
        }
        int xpIntoCurrentLevel = totalXp - cumulativeXpForCurrentLevel;
        int progressPercent = xpForNext > 0 ? (int) ((xpIntoCurrentLevel * 100L) / xpForNext) : 0;
        progressPercent = Math.max(0, Math.min(100, progressPercent));

        pbLevelProgress.setProgress(progressPercent);
        tvLevelProgress.setText(getString(R.string.me_level_progress_format,
                currentLevel, xpIntoCurrentLevel, xpForNext));
    }
}
