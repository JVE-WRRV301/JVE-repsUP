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
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.ProgressionManager;
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

        // Draw edge-to-edge, then keep content clear of the status/nav bars.
        InsetsHelper.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_workout_complete);

        InsetsHelper.applySystemBarsPadding(findViewById(R.id.workoutCompleteRoot));

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
     * Persists workout completion: marks the scheduled workout complete, and —
     * only if that update actually changed a row — updates user XP, inserts an
     * XP log record and updates the streak.
     *
     * The {@code completeScheduledWorkout} update carries an {@code is_completed = 0}
     * guard, so a repeat run (e.g. after this screen is recreated on rotation)
     * affects 0 rows. Gating the XP/log/streak work on "exactly 1 row changed"
     * means completion is safe to repeat and XP is never awarded twice.
     */
    private void persistCompletion() {
        int userId = sessionManager.getUserId();

        // 1. Mark scheduled workout as completed. Everything else is gated on
        //    this update changing exactly 1 row.
        scheduleRepository.completeScheduledWorkout(scheduledWorkoutId, totalXp, totalSets, totalReps,
                new ConnectionManager.DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer rowsChanged) {
                        if (rowsChanged != null && rowsChanged == 1) {
                            // First completion of this scheduled workout — award XP,
                            // log it and update the streak.
                            awardXpAndStreak(userId);
                        }
                        // Otherwise it was already completed: do nothing (no double XP).
                    }

                    @Override
                    public void onError(Exception e) {
                        // Log error but don't block user
                    }
                });
    }

    /**
     * Updates the user's XP, inserts the XP log row and updates the streak.
     * Only called when the scheduled workout was marked complete for the first
     * time (the completion UPDATE changed exactly 1 row).
     */
    private void awardXpAndStreak(int userId) {
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

        // 4. Update streak — only increment if this is the first completed workout today.
        //    "Today" comes from the phone's date, not MySQL CURDATE().
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        scheduleRepository.countOtherCompletedToday(userId, scheduledWorkoutId, today,
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
     * Called when user taps Continue. Hands the XP change to the shared
     * {@link ProgressionManager}, which works out the new level, saves it,
     * awards coins and promotes the rank, then shows the Level Up / Rank Up
     * dialogs it reports, then checks the streak.
     * Dialog order: Level Up → Rank Up → Streak Milestone → Navigate Home
     */
    private void navigateHome() {
        int userId = sessionManager.getUserId();
        btnContinue.setEnabled(false); // Prevent double-tap

        ProgressionManager.applyProgression(userId, userRepository,
                new ProgressionManager.ProgressionCallback() {
                    @Override
                    public void onResult(ProgressionManager.ProgressionResult result) {
                        if (result.leveledUp()) {
                            runOnUiThread(() -> {
                                LevelUpDialog dialog = new LevelUpDialog(
                                        WorkoutCompleteActivity.this, result.getNewLevel());
                                dialog.setOnDismissListener(d ->
                                        showRankUpThenStreak(userId, result));
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
     * After the Level Up dialog, shows the Rank Up dialog if the progression
     * result reported a rank gain, then continues to the streak check.
     */
    private void showRankUpThenStreak(int userId, ProgressionManager.ProgressionResult result) {
        if (result.rankedUp() && result.getNewRankName() != null) {
            runOnUiThread(() -> {
                RankUpDialog dialog = new RankUpDialog(
                        WorkoutCompleteActivity.this, result.getNewRankName());
                dialog.setOnDismissListener(d -> checkStreakAndNavigate(userId));
                dialog.show();
            });
        } else {
            checkStreakAndNavigate(userId);
        }
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
     * Actually performs the navigation to MainActivity.
     */
    private void proceedToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
