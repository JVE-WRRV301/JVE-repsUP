package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SessionManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for executing a workout one exercise at a time.
 * Displays exercise demo, set tracking table, and navigation between exercises.
 */
public class WorkoutExecutionActivity extends AppCompatActivity {

    public static final String EXTRA_SCHEDULED_WORKOUT_ID = "scheduled_workout_id";
    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_EXERCISE_RESULTS = "exercise_results";

    private TextView tvExerciseProgress;
    private ImageView ivExerciseDemo;
    private TextView tvDifficulty;
    private TextView tvExerciseName;
    private LinearLayout llSetRows;
    private TextView btnAddSet;
    private Button btnNext;

    private int scheduledWorkoutId;
    private int workoutId;
    private List<WorkoutExercise> exercises;
    private int currentExerciseIndex = 0;
    private ArrayList<ExerciseResult> exerciseResults = new ArrayList<>();

    // Raw per-set input the user entered, kept per exercise index so we can
    // repopulate the rows exactly when the user navigates back to edit a
    // previously completed exercise. Indices line up with `exercises`.
    private final Map<Integer, List<SetInput>> enteredSetsByIndex = new HashMap<>();

    private ImageButton btnBack;

    /**
     * Snapshot of a single set row's raw input, used to restore the UI when the
     * user goes back to a previous exercise.
     */
    private static class SetInput {
        final String reps;
        final String weight;
        final boolean done;

        SetInput(String reps, String weight, boolean done) {
            this.reps = reps;
            this.weight = weight;
            this.done = done;
        }
    }

    private WorkoutRepository workoutRepository;
    private ScheduleRepository scheduleRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw edge-to-edge, then keep content clear of the status/nav bars.
        InsetsHelper.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_workout_execution);

        InsetsHelper.applySystemBarsPadding(findViewById(R.id.workoutExecutionContent));

        workoutRepository = new WorkoutRepository();
        scheduleRepository = new ScheduleRepository();
        sessionManager = new SessionManager(this);

        scheduledWorkoutId = getIntent().getIntExtra(EXTRA_SCHEDULED_WORKOUT_ID, -1);
        workoutId = getIntent().getIntExtra(EXTRA_WORKOUT_ID, -1);

        initViews();
        validateAndLoad();
    }

    private void initViews() {
        tvExerciseProgress = findViewById(R.id.tvExerciseProgress);
        ivExerciseDemo = findViewById(R.id.ivExerciseDemo);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        llSetRows = findViewById(R.id.llSetRows);
        btnAddSet = findViewById(R.id.btnAddSet);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        btnAddSet.setOnClickListener(v -> addSetRow());
        btnNext.setOnClickListener(v -> onNextClicked());
        btnBack.setOnClickListener(v -> onBackClicked());
    }

    /**
     * Validates that the workout is scheduled for today, then loads exercises.
     */
    private void validateAndLoad() {
        if (scheduledWorkoutId == -1 || workoutId == -1) {
            showNotScheduledError();
            return;
        }

        // Verify this scheduled workout belongs to the user and is for today.
        // "Today" is the phone's date, not MySQL CURDATE(), so it can't disagree
        // with the server's timezone.
        String sql = "SELECT scheduled_workout_id FROM scheduled_workout " +
                "WHERE scheduled_workout_id = ? AND user_id = ? " +
                "AND scheduled_date = ? AND is_completed = 0";

        int userId = sessionManager.getUserId();
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        ConnectionManager.querySingle(sql, new Object[]{scheduledWorkoutId, userId, today}, rs -> {
            return rs.getInt("scheduled_workout_id");
        }, new ConnectionManager.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (result == null) {
                    runOnUiThread(() -> showNotScheduledError());
                } else {
                    loadWorkoutExercises();
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showNotScheduledError());
            }
        });
    }

    private void showNotScheduledError() {
        Toast.makeText(this, R.string.workout_execution_not_scheduled, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Loads workout with all exercises ordered by workout_exercise_order_index.
     */
    private void loadWorkoutExercises() {
        workoutRepository.getWorkoutWithExercises(workoutId, new ConnectionManager.DatabaseCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout == null || workout.getExercises().isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(WorkoutExecutionActivity.this,
                                R.string.workout_execution_load_error, Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                exercises = workout.getExercises();
                runOnUiThread(() -> displayCurrentExercise());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(WorkoutExecutionActivity.this,
                            R.string.workout_execution_load_error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    /**
     * Displays the current exercise with its details and pre-populated set rows.
     */
    private void displayCurrentExercise() {
        WorkoutExercise workoutExercise = exercises.get(currentExerciseIndex);

        // Update progress
        tvExerciseProgress.setText(getString(R.string.workout_execution_progress,
                currentExerciseIndex + 1, exercises.size()));

        // Update exercise info
        tvExerciseName.setText(workoutExercise.getExercise().getName());
        tvDifficulty.setText(workoutExercise.getExercise().getDifficulty());

        // Show a demo image for the exercise. Named exercises resolve to a
        // bundled drawable; anything else falls back to the placeholder.
        int demoDrawable = demoDrawableFor(workoutExercise.getExercise().getName());
        ivExerciseDemo.setImageResource(demoDrawable);

        // Update button text for last exercise
        if (currentExerciseIndex == exercises.size() - 1) {
            btnNext.setText(R.string.workout_execution_finish);
        } else {
            btnNext.setText(R.string.workout_execution_next);
        }

        // Show the back button on every exercise except the first.
        btnBack.setVisibility(currentExerciseIndex > 0 ? View.VISIBLE : View.GONE);

        // Clear and populate set rows.
        llSetRows.removeAllViews();

        List<SetInput> saved = enteredSetsByIndex.get(currentExerciseIndex);
        if (saved != null && !saved.isEmpty()) {
            // Returning to an exercise the user already worked on: restore exactly
            // what they entered so they can review and edit it.
            for (SetInput input : saved) {
                addSetRow(input);
            }
        } else {
            // First visit: populate with the exercise's default sets/reps/weight.
            int defaultSets = workoutExercise.getSets();
            if (defaultSets <= 0) {
                defaultSets = 3; // fallback
            }
            for (int i = 0; i < defaultSets; i++) {
                addSetRow(workoutExercise.getReps(), workoutExercise.getTargetWeight());
            }
        }

        // Reset next button state
        btnNext.setEnabled(false);
        updateNextButtonState();
    }

    /**
     * Maps an exercise name to its demonstration drawable. Uses keyword
     * matching so DB names like "Dumbbell Shoulder Press" or
     * "Dumbbell Lateral Raises" resolve correctly. Falls back to the generic
     * exercise placeholder when no specific demo exists.
     */
    private int demoDrawableFor(String exerciseName) {
        if (exerciseName == null) {
            return R.drawable.ic_exercise_placeholder;
        }

        String name = exerciseName.toLowerCase();

        if (name.contains("lateral rais")) {
            // Note: drawable filename has a typo ("lataeral") - kept as-is.
            return R.drawable.lataeral_raises_avatar;
        }
        if (name.contains("shoulder press")) {
            return R.drawable.shoulder_press_avatar;
        }
        if (name.contains("bench press")) {
            return R.drawable.bench_press_avatar;
        }

        return R.drawable.ic_exercise_placeholder;
    }

    /**
     * Adds a new set row with empty reps/weight fields.
     */
    private void addSetRow() {
        addSetRow(0, 0.0);
    }

    /**
     * Adds a set row restored from previously-entered input, including the
     * checked state, so a user returning to an exercise sees exactly what they
     * logged and can edit it.
     */
    private void addSetRow(SetInput input) {
        View setRow = LayoutInflater.from(this)
                .inflate(R.layout.item_set_row, llSetRows, false);

        int setNumber = llSetRows.getChildCount() + 1;

        TextView tvSetNumber = setRow.findViewById(R.id.tvSetNumber);
        EditText etReps = setRow.findViewById(R.id.etReps);
        EditText etWeight = setRow.findViewById(R.id.etWeight);
        CheckBox cbDone = setRow.findViewById(R.id.cbDone);

        tvSetNumber.setText(String.valueOf(setNumber));
        etReps.setText(input.reps);
        etWeight.setText(input.weight);
        cbDone.setChecked(input.done);

        cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> updateNextButtonState());

        llSetRows.addView(setRow);
        updateNextButtonState();
    }

    /**
     * Adds a new set row with pre-populated default reps and weight.
     */
    private void addSetRow(int defaultReps, double defaultWeight) {
        View setRow = LayoutInflater.from(this)
                .inflate(R.layout.item_set_row, llSetRows, false);

        int setNumber = llSetRows.getChildCount() + 1;

        TextView tvSetNumber = setRow.findViewById(R.id.tvSetNumber);
        EditText etReps = setRow.findViewById(R.id.etReps);
        EditText etWeight = setRow.findViewById(R.id.etWeight);
        CheckBox cbDone = setRow.findViewById(R.id.cbDone);

        tvSetNumber.setText(String.valueOf(setNumber));

        if (defaultReps > 0) {
            etReps.setText(String.valueOf(defaultReps));
        }

        if (defaultWeight > 0) {
            etWeight.setText(String.valueOf(defaultWeight));
        }

        // Monitor checkbox changes to update Next button state
        cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> updateNextButtonState());

        llSetRows.addView(setRow);
        updateNextButtonState();
    }

    /**
     * Enables the Next button only when all set checkboxes are checked.
     */
    private void updateNextButtonState() {
        boolean allChecked = true;
        int childCount = llSetRows.getChildCount();

        if (childCount == 0) {
            allChecked = false;
        }

        for (int i = 0; i < childCount; i++) {
            View row = llSetRows.getChildAt(i);
            CheckBox cbDone = row.findViewById(R.id.cbDone);
            if (!cbDone.isChecked()) {
                allChecked = false;
                break;
            }
        }

        btnNext.setEnabled(allChecked);
        btnNext.setAlpha(allChecked ? 1.0f : 0.5f);
    }

    /**
     * Handles Next button click: saves current exercise results and navigates forward.
     */
    private void onNextClicked() {
        saveCurrentExerciseResult();

        if (currentExerciseIndex < exercises.size() - 1) {
            // Move to next exercise
            currentExerciseIndex++;
            displayCurrentExercise();
        } else {
            // Last exercise completed - navigate to WorkoutCompleteActivity
            navigateToComplete();
        }
    }

    /**
     * Handles Back button click: preserves whatever the user has entered on the
     * current exercise (so in-progress edits aren't lost), then moves back to the
     * previous exercise and restores its entered data for editing.
     */
    private void onBackClicked() {
        if (currentExerciseIndex <= 0) {
            return;
        }
        // Keep the current exercise's input so returning forward restores it.
        saveCurrentExerciseResult();

        currentExerciseIndex--;
        displayCurrentExercise();
    }

    /**
     * Collects data from the current set rows and records it against the current
     * exercise index. Storing by index (rather than appending) means navigating
     * back and forward re-saves the same exercise in place instead of creating
     * duplicate results. Also snapshots the raw per-set input so the rows can be
     * restored for editing if the user returns to this exercise.
     */
    private void saveCurrentExerciseResult() {
        WorkoutExercise workoutExercise = exercises.get(currentExerciseIndex);
        int exerciseId = workoutExercise.getExerciseId();
        int baseXp = workoutExercise.getExercise().getBaseXp();

        int setsCompleted = 0;
        int totalReps = 0;
        double totalWeight = 0;

        List<SetInput> rawSets = new ArrayList<>();

        int childCount = llSetRows.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View row = llSetRows.getChildAt(i);
            CheckBox cbDone = row.findViewById(R.id.cbDone);
            EditText etReps = row.findViewById(R.id.etReps);
            EditText etWeight = row.findViewById(R.id.etWeight);

            String repsText = etReps.getText().toString().trim();
            String weightText = etWeight.getText().toString().trim();
            boolean done = cbDone.isChecked();

            // Snapshot every row's raw input for restore-on-back.
            rawSets.add(new SetInput(repsText, weightText, done));

            if (done) {
                setsCompleted++;

                int reps = 0;
                double weight = 0;

                if (!repsText.isEmpty()) {
                    try {
                        reps = Integer.parseInt(repsText);
                    } catch (NumberFormatException ignored) {}
                }
                if (!weightText.isEmpty()) {
                    try {
                        weight = Double.parseDouble(weightText);
                    } catch (NumberFormatException ignored) {}
                }

                totalReps += reps;
                totalWeight += weight;
            }
        }

        // Preserve the raw input for this exercise index (overwrites any prior snapshot).
        enteredSetsByIndex.put(currentExerciseIndex, rawSets);

        // Store the aggregate result at this exercise's index, overwriting any
        // previous result for the same exercise rather than appending a duplicate.
        ExerciseResult result = new ExerciseResult(exerciseId, baseXp, setsCompleted, totalReps, totalWeight);
        while (exerciseResults.size() <= currentExerciseIndex) {
            exerciseResults.add(null);
        }
        exerciseResults.set(currentExerciseIndex, result);
    }

    /**
     * Navigates to the WorkoutCompleteActivity with all exercise results.
     */
    private void navigateToComplete() {
        // Drop any null placeholders (padding left by index-based storage) so the
        // complete screen only receives real results.
        ArrayList<ExerciseResult> finalResults = new ArrayList<>();
        for (ExerciseResult r : exerciseResults) {
            if (r != null) {
                finalResults.add(r);
            }
        }

        Intent intent = new Intent(this, WorkoutCompleteActivity.class);
        intent.putExtra(EXTRA_SCHEDULED_WORKOUT_ID, scheduledWorkoutId);
        intent.putExtra(EXTRA_WORKOUT_ID, workoutId);
        intent.putExtra(EXTRA_EXERCISE_RESULTS, (Serializable) finalResults);
        startActivity(intent);
        finish();
    }
}
