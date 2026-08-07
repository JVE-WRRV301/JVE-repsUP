package com.jve.repsup.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.dialogs.StreakMilestoneDialog;
import com.jve.repsup.models.ExerciseResult;
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
        displaySummary();
        loadMuscleGroups();
        persistCompletion();
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
                // TODO: Task 13 — check for level-up after XP update
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

        // 4. Update streak — user completed today's scheduled workout
        userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer streak) {
                int currentStreak = streak != null ? streak : 0;
                int newStreak = StreakManager.evaluateStreak(currentStreak, true, true);
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

    /**
     * Navigates to MainActivity (HomeFragment) clearing the back stack
     * so user can't go back to the execution flow.
     */
    private void navigateHome() {
        // TODO: Task 13 — trigger level-up check dialog before navigating

        // Check for streak milestone before navigating
        int userId = sessionManager.getUserId();
        userRepository.getCurrentStreak(userId, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer streak) {
                int currentStreak = streak != null ? streak : 0;
                if (StreakManager.isMilestone(currentStreak)) {
                    runOnUiThread(() -> {
                        double multiplier = StreakManager.getMultiplier(currentStreak);
                        StreakMilestoneDialog dialog = new StreakMilestoneDialog(
                                WorkoutCompleteActivity.this, currentStreak, multiplier);
                        dialog.setOnDismissListener(d -> proceedToHome());
                        dialog.show();
                    });
                } else {
                    proceedToHome();
                }
            }

            @Override
            public void onError(Exception e) {
                proceedToHome();
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
