package com.jve.repsup.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.jve.repsup.R;
import com.jve.repsup.activities.CustomizeAvatarActivity;
import com.jve.repsup.activities.SettingsActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.Rank;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.AvatarCompositor;
import com.jve.repsup.utils.SessionManager;

/**
 * MeFragment displays the user's avatar, gym rank, and level progress.
 * Provides navigation to SettingsActivity (gear icon) and CustomizeAvatarActivity (locker icon).
 */
public class MeFragment extends Fragment {

    private static final String TAG = "MeFragment";

    // Views
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvRankName;
    private ImageView ivMedal;
    private CircularProgressIndicator progressLevel;
    private TextView tvLevelNumber;
    private TextView tvXpProgress;
    private ImageButton btnSettings;
    private ImageButton btnLocker;

    // Data layer
    private UserRepository userRepository;
    private SessionManager sessionManager;

    public MeFragment() {
        // Required empty public constructor
    }

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
        setupListeners();
        loadUserData();
    }

    /**
     * Initializes all view references from the layout.
     */
    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvRankName = view.findViewById(R.id.tvRankName);
        ivMedal = view.findViewById(R.id.ivMedal);
        progressLevel = view.findViewById(R.id.progressLevel);
        tvLevelNumber = view.findViewById(R.id.tvLevelNumber);
        tvXpProgress = view.findViewById(R.id.tvXpProgress);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLocker = view.findViewById(R.id.btnLocker);
    }

    /**
     * Sets up click listeners for navigation icons.
     */
    private void setupListeners() {
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        btnLocker.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CustomizeAvatarActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Loads user data from the database and updates the UI.
     */
    private void loadUserData() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Log.w(TAG, "No active session, cannot load user data");
            return;
        }

        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null || !isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    displayUserInfo(user);
                    displayLevelProgress(user);
                });

                // Load rank if available
                if (user.getRankId() != null) {
                    loadRank(user.getRankId());
                } else {
                    requireActivity().runOnUiThread(() -> {
                        tvRankName.setText(R.string.me_no_rank);
                        ivMedal.setVisibility(View.GONE);
                    });
                }

                // Load avatar if available
                if (user.getAvatarId() != null) {
                    loadAvatar(user.getAvatarId());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load user data", e);
            }
        });
    }

    /**
     * Displays basic user info (username).
     */
    private void displayUserInfo(User user) {
        tvUsername.setText(user.getUsername());
    }

    /**
     * Displays level and circular progress toward next level.
     * Level N requires N × 100 XP to advance.
     */
    private void displayLevelProgress(User user) {
        int currentLevel = user.getLevel();
        int totalXp = user.getXp();

        // Calculate XP thresholds
        int xpForCurrentLevel = xpRequiredToReachLevel(currentLevel);
        int xpForNextLevel = currentLevel * 100;
        int xpIntoCurrentLevel = totalXp - xpForCurrentLevel;

        // Clamp progress percentage to [0, 100]
        int progressPercent = 0;
        if (xpForNextLevel > 0) {
            progressPercent = Math.max(0, Math.min(100, (xpIntoCurrentLevel * 100) / xpForNextLevel));
        }

        tvLevelNumber.setText(String.valueOf(currentLevel));
        tvXpProgress.setText(xpIntoCurrentLevel + " / " + xpForNextLevel + " XP");
        progressLevel.setProgress(progressPercent);
    }

    /**
     * Calculates total XP required to reach a given level.
     * Sum of (level * 100) for levels 1 through (targetLevel - 1).
     */
    private int xpRequiredToReachLevel(int targetLevel) {
        int total = 0;
        for (int i = 1; i < targetLevel; i++) {
            total += i * 100;
        }
        return total;
    }

    /**
     * Loads the user's rank from the database and updates the UI.
     */
    private void loadRank(int rankId) {
        userRepository.getRankById(rankId, new ConnectionManager.DatabaseCallback<Rank>() {
            @Override
            public void onSuccess(Rank rank) {
                if (rank == null || !isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    tvRankName.setText(rank.getDisplayName());
                    ivMedal.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load rank", e);
            }
        });
    }

    /**
     * Loads the user's avatar from the database and composites it for display.
     */
    private void loadAvatar(int avatarId) {
        userRepository.getAvatarById(avatarId, new ConnectionManager.DatabaseCallback<Avatar>() {
            @Override
            public void onSuccess(Avatar avatar) {
                if (avatar == null || !isAdded()) return;
                Bitmap compositedAvatar = AvatarCompositor.composite(requireContext(), avatar, null);
                requireActivity().runOnUiThread(() -> {
                    ivAvatar.setImageBitmap(compositedAvatar);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load avatar", e);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning from settings or customization
        loadUserData();
    }
}
