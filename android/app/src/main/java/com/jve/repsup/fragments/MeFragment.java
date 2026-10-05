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

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.activities.CustomizeAvatarActivity;
import com.jve.repsup.activities.SettingsActivity;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.CosmeticRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.AvatarCompositor;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.XPCalculator;

import java.util.List;

/**
 * MeFragment displaying the user's full-body avatar, gym rank card with medal icon
 * and circular level progress, gear icon for settings, and locker icon for avatar
 * customization.
 *
 * Validates: Requirements 20.1, 20.2, 20.3, 20.4, 20.5
 */
public class MeFragment extends Fragment {

    private ImageButton btnSettings;
    private ImageButton btnLocker;
    private ImageView ivAvatar;
    private ImageView ivMedal;
    private TextView tvRankName;
    private TextView tvLevelNumber;
    private TextView tvLevelLabel;
    private TextView tvXpProgress;
    private ProgressBar pbLevelProgress;

    private UserRepository userRepository;
    private CosmeticRepository cosmeticRepository;
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
        cosmeticRepository = new CosmeticRepository();
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupClickListeners();
        loadUserProfile();
        loadAvatar();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    /**
     * Fires when this fragment is shown/hidden via the MainActivity hide/show tab
     * pattern. Unlike onResume(), this runs on tab switches (the activity never
     * pauses), so it's needed to reflect XP/level changes made on other tabs
     * (e.g. completing a goal) as soon as the user returns to the Me tab.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshData();
        }
    }

    private void refreshData() {
        loadUserProfile();
        // Refresh the avatar so outfit/skin-tone changes made in the locker show here.
        loadAvatar();
        // Also refresh the top-bar profile picture: the avatar may have changed in
        // the locker, and the top bar otherwise renders the face only once per
        // session. This keeps the profile picture in sync with the avatar.
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshAvatarThumb();
        }
    }

    /**
     * Loads the user's avatar (body + equipped outfit) and composites it into the
     * locker preview box, mirroring the locker screen. Gender comes from the
     * profile, the body/skin tone from the Avatar record, and the equipped items
     * from the outfit slot. Falls back silently if any piece is unavailable.
     */
    private void loadAvatar() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        cosmeticRepository.getUserGender(userId, new ConnectionManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String gender) {
                String g = (gender != null && !gender.trim().isEmpty())
                        ? gender.trim().toLowerCase() : "male";
                loadAvatarBodyThenComposite(userId, g);
            }

            @Override
            public void onError(Exception e) {
                loadAvatarBodyThenComposite(userId, "male");
            }
        });
    }

    private void loadAvatarBodyThenComposite(int userId, String gender) {
        cosmeticRepository.getAvatar(userId, new ConnectionManager.DatabaseCallback<Avatar>() {
            @Override
            public void onSuccess(Avatar avatar) {
                cosmeticRepository.getEquippedItemsForOutfit(userId,
                        new ConnectionManager.DatabaseCallback<List<CosmeticItem>>() {
                            @Override
                            public void onSuccess(List<CosmeticItem> equipped) {
                                compositeInto(gender, avatar, equipped);
                            }

                            @Override
                            public void onError(Exception e) {
                                compositeInto(gender, avatar, null);
                            }
                        });
            }

            @Override
            public void onError(Exception e) { /* leave default avatar image */ }
        });
    }

    private void compositeInto(String gender, Avatar avatar, List<CosmeticItem> equipped) {
        if (avatar == null) return;
        final String bodyType = avatar.getBodyShape();
        final String skinTone = avatar.getSkinTone();
        final String hairColour = avatar.getHairColour();

        new Thread(() -> {
            if (getContext() == null) return;
            Bitmap bmp = AvatarCompositor.composite(
                    requireContext().getApplicationContext(), gender, bodyType, skinTone, hairColour, equipped);
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> ivAvatar.setImageBitmap(bmp));
        }).start();
    }

    private void initViews(View view) {
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLocker = view.findViewById(R.id.btnLocker);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivMedal = view.findViewById(R.id.ivMedal);
        tvRankName = view.findViewById(R.id.tvRankName);
        tvLevelNumber = view.findViewById(R.id.tvLevelNumber);
        tvLevelLabel = view.findViewById(R.id.tvLevelLabel);
        tvXpProgress = view.findViewById(R.id.tvXpProgress);
        pbLevelProgress = view.findViewById(R.id.pbLevelProgress);
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
     * Computes the correct level from total XP and fixes the DB if stale.
     */
    private void displayUserProfile(User user) {
        // Display rank name (uppercase for the design style)
        if (user.getRankName() != null && !user.getRankName().isEmpty()) {
            tvRankName.setText(user.getRankName().toUpperCase());
        } else {
            tvRankName.setText(R.string.me_rank_default);
        }

        int totalXp = user.getXp();

        // Compute the correct level based on total XP
        int correctLevel = 1;
        while (XPCalculator.shouldLevelUp(totalXp, correctLevel)) {
            correctLevel++;
        }

        // Fix DB if level is out of sync
        if (correctLevel != user.getCurrentLevel()) {
            userRepository.updateLevel(user.getUserId(), correctLevel, null);
        }

        // Refresh the top bar to stay in sync
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadTopBarData();
        }

        // Display level number in circle and label below
        tvLevelNumber.setText(String.valueOf(correctLevel));
        tvLevelLabel.setText(getString(R.string.me_level_format, correctLevel));

        // Calculate XP progress within current level for circular indicator
        int xpForNext = XPCalculator.xpForNextLevel(correctLevel);

        int cumulativeXpForCurrentLevel = 0;
        for (int i = 1; i < correctLevel; i++) {
            cumulativeXpForCurrentLevel += XPCalculator.xpForNextLevel(i);
        }
        int xpIntoCurrentLevel = totalXp - cumulativeXpForCurrentLevel;
        int progressPercent = xpForNext > 0 ? (int) ((xpIntoCurrentLevel * 100L) / xpForNext) : 0;
        progressPercent = Math.max(0, Math.min(100, progressPercent));

        pbLevelProgress.setProgress(progressPercent);

        // Display XP progress text (e.g. "250 / 500 XP")
        tvXpProgress.setText(getString(R.string.me_xp_progress_format,
                xpIntoCurrentLevel, xpForNext));
    }
}
