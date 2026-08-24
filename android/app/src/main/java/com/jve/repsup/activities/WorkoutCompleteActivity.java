package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.dialogs.LevelUpDialog;
import com.jve.repsup.dialogs.RankUpDialog;
import com.jve.repsup.dialogs.StreakMilestoneDialog;
import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.models.Rank;
import com.jve.repsup.models.User;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.StreakManager;
import com.jve.repsup.utils.XPCalculator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Displays workout completion summary including total XP earned and muscles worked.
 * Persists completion data (scheduled_workout, user XP, XP log) then navigates home.
 */
public class WorkoutCompleteActivity extends AppCompatActivity {

    private static final String EXTRA_SCHEDULED_WORKOUT_ID = "scheduled_workout_id";
    private static final String EXTRA_WORKOUT_ID = "workout_id";
    private static final String EXTRA_EXERCISE_RESULTS = "exercise_results";

    private TextView tvXpEarned;
    private TextView tvMusclesList;
    private Button btnContinue;

    private int scheduledWorkoutId;
    private int workoutId;
    private ArrayList<ExerciseResult> exerciseResults;

    private ScheduleRepository scheduleRepository;
    private UserRepository userRepository;
    private WorkoutRepository workoutRepository;
    private SessionManager sessionManager;

    private int totalXp;
    private int totalSets;
    private int totalReps;

    private int updatedStreak = -1; // -1 means streak update hasn't completed yet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_complete);

        scheduleRepository = new ScheduleRepository();
        userRepository = new UserRepository();
        workoutRepository = new WorkoutRepository();
        sessionManager = new SessionManager(this);

        extractExtras();
        initViews();
        calculateResults();
        loadMuscleGroups();
    }

    private void extractExtras() {
        scheduledWorkoutId = getIntent().getIntExtra(EXTRA_SCHEDULED_WORKOUT_ID, -1);
        workoutId = getIntent().getIntExtra(EXTRA_WORKOUT_ID, -1);

        @SuppressWarnings("unchecked")
        ArrayList<ExerciseResult> results = (ArrayList<ExerciseResult>)
                getIntent().getSerializableExtra(EXTRA_EXERCISE_RESULTS);
        exerciseResults = results != null ? results : new ArrayList<>();
    }

    private void initViews() {
        tvXpEarned = findViewById(R.id.tvXpEarned);
        tvMusclesList = findViewById(R.id.tvMusclesList);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> navigateHome());
    }

    private void calculateResults() {
        // Get the user's current streak and calculate the multiplier
        int userId = sessionManager.getUserId();
        userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer streak) {
                int currentStreak = streak != null ? streak : 0;
                double streakMultiplier = StreakManager.getMultiplier(currentStreak);

                totalXp = XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier);

                totalSets = 0;
                totalReps = 0;
                for (ExerciseResult result : exerciseResults) {
                    totalSets += result.getSetsCompleted();
                    totalReps += result.getTotalReps();
                }

                runOnUiThread(() -> displaySummary());
                persistCompletion();  // Now safe — totalXp is assigned
            }

            @Override
            public void onError(Exception e) {
                // Fallback to 1.0 multiplier if streak cannot be retrieved
                double streakMultiplier = 1.0;

                totalXp = XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier);

                totalSets = 0;
                totalReps = 0;
                for (ExerciseResult result : exerciseResults) {
                    totalSets += result.getSetsCompleted();
                    totalReps += result.getTotalReps();
                }

                runOnUiThread(() -> displaySummary());
                persistCompletion();  // Now safe — totalXp is assigned with fallback multiplier
            }
        });
    }

    private void displaySummary() {
        tvXpEarned.setText(getString(R.string.workout_complete_xp_earned, totalXp));
    }

    /**
     * Loads the workout's exercises from DB to extract unique muscle groups.
     */
    private void loadMuscleGroups() {
        workoutRepository.getWorkoutWithExercises(workoutId, new ConnectionManager.DatabaseCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout == null || workout.getExercises() == null) {
                    runOnUiThread(() -> tvMusclesList.setText("—"));
                    return;
                }

                Set<String> muscleGroups = new LinkedHashSet<>();
                for (WorkoutExercise we : workout.getExercises()) {
                    if (we.getExercise() != null && we.getExercise().getMuscleGroup() != null) {
                        muscleGroups.add(we.getExercise().getMuscleGroup());
                    }
                }

                String musclesText = String.join(", ", muscleGroups);
                runOnUiThread(() -> tvMusclesList.setText(musclesText));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> tvMusclesList.setText("—"));
            }
        });
    }

    /**
     * Persists workout completion: marks scheduled workout complete,
     * updates user XP, and inserts an XP log record.
     */
    private void persistCompletion() {
        int userId = sessionManager.getUserId();

        // 1. Mark scheduled workout as completed
        scheduleRepository.completeScheduledWorkout(scheduledWorkoutId, totalXp, totalSets, totalReps,
                new ConnectionManager.DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        // Scheduled workout marked complete
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log error but don't block user
                    }
                });

        // 2. Update user's total XP
        userRepository.updateXp(userId, totalXp, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                // XP updated — level/rank checks happen when user taps Continue
            }

            @Override
            public void onError(Exception e) {
                // Log error but don't block user
            }
        });

        // 3. Insert XP log record
        int coinsEarned = 0; // Coins are awarded on level-up only (task 13)
        userRepository.insertXpLog(userId, totalXp, "workout", workoutId, totalXp, coinsEarned,
                new ConnectionManager.DatabaseCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        // XP log inserted
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log error but don't block user
                    }
                });

        // 4. Update streak — only increment if this is the first completed workout today
        scheduleRepository.countOtherCompletedToday(userId, scheduledWorkoutId,
                new ConnectionManager.DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer otherCompletedCount) {
                        int alreadyCompletedToday = otherCompletedCount != null ? otherCompletedCount : 0;
                        if (alreadyCompletedToday > 0) {
                            // Streak was already incremented today — skip
                            // Store current streak for milestone check (no change)
                            userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer streak) {
                                    updatedStreak = streak != null ? streak : 0;
                                }
                                @Override
                                public void onError(Exception e) {}
                            });
                            return;
                        }
                        // This is the first completion today — increment streak
                        userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer streak) {
                                int currentStreak = streak != null ? streak : 0;
                                int newStreak = StreakManager.evaluateStreak(currentStreak, true, true);
                                updatedStreak = newStreak; // Store for milestone check
                                userRepository.updateStreak(userId, newStreak, new ConnectionManager.DatabaseCallback<Integer>() {
                                    @Override
                                    public void onSuccess(Integer result) {
                                        // Streak updated successfully
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // Log error but don't block user
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                // Log error but don't block user
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log error but don't block user — default to incrementing
                        // (better to over-count once than silently fail)
                        userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer streak) {
                                int currentStreak = streak != null ? streak : 0;
                                int newStreak = StreakManager.evaluateStreak(currentStreak, true, true);
                                userRepository.updateStreak(userId, newStreak, null);
                            }

                            @Override
                            public void onError(Exception e2) {
                                // Silent fail
                            }
                        });
                    }
                });
    }

    /**
     * Called when user taps Continue. Reads latest user state from DB,
     * checks for level-up and rank-up, shows dialogs in order, then checks streak.
     * Dialog order: Level Up → Rank Up → Streak Milestone → Navigate Home
     */
    private void navigateHome() {
        int userId = sessionManager.getUserId();
        btnContinue.setEnabled(false); // Prevent double-tap

        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    checkStreakAndNavigate(userId);
                    return;
                }

                int currentLevel = user.getCurrentLevel();
                int totalUserXp = user.getXp();

                // Check for multiple level-ups
                int newLevel = currentLevel;
                while (XPCalculator.shouldLevelUp(totalUserXp, newLevel)) {
                    newLevel++;
                }

                if (newLevel > currentLevel) {
                    int finalLevel = newLevel;
                    int coinsEarned = (finalLevel - currentLevel) * 50;

                    // Update level in DB
                    userRepository.updateLevel(userId, finalLevel, null);
                    // Award coins for level up
                    userRepository.updateCoins(userId, coinsEarned, null);

                    // Show level-up dialog
                    runOnUiThread(() -> {
                        LevelUpDialog dialog = new LevelUpDialog(WorkoutCompleteActivity.this, finalLevel);
                        dialog.setOnDismissListener(d -> {
                            // After level-up dismissed, check rank promotion
                            checkRankPromotion(userId, finalLevel, () -> checkStreakAndNavigate(userId));
                        });
                        dialog.show();
                    });
                } else {
                    checkStreakAndNavigate(userId);
                }
            }

            @Override
            public void onError(Exception e) {
                checkStreakAndNavigate(userId);
            }
        });
    }

    /**
     * Checks for streak milestone and then navigates home.
     * Uses the locally stored updatedStreak if available (avoids race condition),
     * otherwise falls back to reading from DB.
     */
    private void checkStreakAndNavigate(int userId) {
        if (updatedStreak >= 0) {
            // Use the locally stored streak value (avoids race condition)
            handleStreakMilestone(updatedStreak);
        } else {
            // Fallback: read from DB (e.g. if streak update hasn't run yet)
            userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
                @Override
                public void onSuccess(Integer streak) {
                    int currentStreak = streak != null ? streak : 0;
                    handleStreakMilestone(currentStreak);
                }

                @Override
                public void onError(Exception e) {
                    proceedToHome();
                }
            });
        }
    }

    /**
     * Shows the streak milestone dialog if the streak is at a milestone, then navigates home.
     */
    private void handleStreakMilestone(int streakValue) {
        if (StreakManager.isMilestone(streakValue)) {
            runOnUiThread(() -> {
                double multiplier = StreakManager.getMultiplier(streakValue);
                StreakMilestoneDialog dialog = new StreakMilestoneDialog(
                        WorkoutCompleteActivity.this, streakValue, multiplier);
                dialog.setOnDismissListener(d -> proceedToHome());
                dialog.show();
            });
        } else {
            proceedToHome();
        }
    }

    /**
     * Checks if the user's new level qualifies for a rank promotion.
     * If a next rank is available, updates the user's rank_id, awards 100 bonus coins,
     * and shows a RankUpDialog before continuing the flow.
     *
     * @param userId   the user's ID
     * @param newLevel the user's new level after levelling up
     * @param onComplete callback to run after rank check completes
     */
    private void checkRankPromotion(int userId, int newLevel, Runnable onComplete) {
        userRepository.getUserById(userId, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    onComplete.run();
                    return;
                }

                int currentRankId = user.getRankId() != null ? user.getRankId() : 0;

                userRepository.getNextRank(newLevel, currentRankId, new ConnectionManager.DatabaseCallback<Rank>() {
                    @Override
                    public void onSuccess(Rank nextRank) {
                        if (nextRank != null) {
                            // User qualifies for rank promotion — update rank
                            userRepository.updateRank(userId, nextRank.getRankId(), null);

                            // Award 100 bonus coins for rank up
                            userRepository.updateCoins(userId, 100, null);

                            // Show RankUpDialog on the UI thread
                            runOnUiThread(() -> {
                                RankUpDialog dialog = new RankUpDialog(
                                        WorkoutCompleteActivity.this, nextRank.getDisplayName());
                                dialog.setOnDismissListener(d -> onComplete.run());
                                dialog.show();
                            });
                        } else {
                            onComplete.run();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        onComplete.run();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                onComplete.run();
            }
        });
    }

    /**
     * Actually performs the navigation to MainActivity.
     */
    private void proceedToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
