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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.jve.repsup.R;
import com.jve.repsup.utils.DisplayText;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ExerciseResult;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.ExerciseLogRepository;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.ExerciseDemo;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.WeightFormat;

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
    private ImageButton btnQuit;
    private TextView btnSkipExercise;

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
    // Last logged weight (kg) per exercise id; used to pre-fill the weight boxes.
    private final Map<Integer, Double> lastWeights = new HashMap<>();

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
        btnQuit = findViewById(R.id.btnQuit);
        btnSkipExercise = findViewById(R.id.btnSkipExercise);

        btnAddSet.setOnClickListener(v -> addSetRow());
        btnNext.setOnClickListener(v -> onNextClicked());
        btnBack.setOnClickListener(v -> onBackClicked());
        btnSkipExercise.setOnClickListener(v -> onSkipClicked());

        // The X button and the phone's Back button both ask before leaving, so one
        // tap can't throw away the whole workout.
        btnQuit.setOnClickListener(v -> confirmQuit());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmQuit();
            }
        });
    }

    /** Asks "Quit workout?" and leaves (losing progress) only if the user confirms. */
    private void confirmQuit() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.workout_quit_title)
                .setMessage(R.string.workout_quit_message)
                .setPositiveButton(R.string.workout_quit_confirm, (dialog, which) -> finish())
                .setNegativeButton(R.string.workout_quit_stay, null)
                .show();
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
        DialogUtils.showErrorThen(this, R.string.workout_execution_not_scheduled, this::finish);
    }

    /**
     * Loads workout with all exercises ordered by workout_exercise_order_index.
     */
    private void loadWorkoutExercises() {
        workoutRepository.getWorkoutWithExercises(workoutId, new ConnectionManager.DatabaseCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout == null || workout.getExercises().isEmpty()) {
                    runOnUiThread(() -> DialogUtils.showErrorThen(WorkoutExecutionActivity.this,
                            R.string.workout_execution_load_error, WorkoutExecutionActivity.this::finish));
                    return;
                }

                exercises = workout.getExercises();
                loadLastWeightsThenDisplay();
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showErrorThen(WorkoutExecutionActivity.this,
                        R.string.workout_execution_load_error, WorkoutExecutionActivity.this::finish));
            }
        });
    }

    /**
     * Looks up the weights the user last logged, so the weight boxes can start
     * with them, then shows the first exercise. If the lookup fails the boxes just
     * start with the workout's target weight.
     */
    private void loadLastWeightsThenDisplay() {
        new ExerciseLogRepository().getLastWeights(sessionManager.getUserId(),
                new ConnectionManager.DatabaseCallback<Map<Integer, Double>>() {
                    @Override
                    public void onSuccess(Map<Integer, Double> weights) {
                        if (weights != null) lastWeights.putAll(weights);
                        runOnUiThread(() -> displayCurrentExercise());
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> displayCurrentExercise());
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
        tvDifficulty.setText(DisplayText.capitalise(workoutExercise.getExercise().getDifficulty()));

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
            // Start with the weight last logged for this exercise, if there is one.
            Double lastWeight = lastWeights.get(workoutExercise.getExerciseId());
            double startWeight = lastWeight != null && lastWeight > 0
                    ? lastWeight : workoutExercise.getTargetWeight();
            for (int i = 0; i < defaultSets; i++) {
                addSetRow(workoutExercise.getReps(), startWeight);
            }
        }

        // Reset next button state
        btnNext.setEnabled(false);
        updateNextButtonState();
    }

    /**
     * Maps an exercise name to its demonstration drawable (shared with the
     * exercise detail popup in {@link ExerciseDemo}).
     */
    private int demoDrawableFor(String exerciseName) {
        return ExerciseDemo.drawableFor(exerciseName);
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

        bindRowControls(setRow);
        llSetRows.addView(setRow);
        refreshRows();
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
            etWeight.setText(WeightFormat.plain(defaultWeight));
        }

        bindRowControls(setRow);
        llSetRows.addView(setRow);
        refreshRows();
    }

    /** Wires a set row's tick box (updates Next) and remove button. */
    private void bindRowControls(View setRow) {
        CheckBox cbDone = setRow.findViewById(R.id.cbDone);
        cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> updateNextButtonState());

        setRow.findViewById(R.id.btnRemoveSet).setOnClickListener(v -> {
            // Always keep at least one set.
            if (llSetRows.getChildCount() > 1) {
                llSetRows.removeView(setRow);
                refreshRows();
            }
        });
    }

    /**
     * Renumbers the set rows, hides the remove button when only one set is
     * left, and updates the Next button.
     */
    private void refreshRows() {
        int count = llSetRows.getChildCount();
        for (int i = 0; i < count; i++) {
            View row = llSetRows.getChildAt(i);
            ((TextView) row.findViewById(R.id.tvSetNumber)).setText(String.valueOf(i + 1));
            row.findViewById(R.id.btnRemoveSet)
                    .setVisibility(count > 1 ? View.VISIBLE : View.INVISIBLE);
        }
        updateNextButtonState();
    }

    /**
     * Enables the Next button once at least one set is ticked. Sets the user
     * does not do can stay unticked (or be removed), and earn no XP.
     */
    private void updateNextButtonState() {
        boolean anyChecked = false;
        int childCount = llSetRows.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View row = llSetRows.getChildAt(i);
            CheckBox cbDone = row.findViewById(R.id.cbDone);
            if (cbDone.isChecked()) {
                anyChecked = true;
                break;
            }
        }

        // Same disabled look as the assessment Next button (grey, muted text).
        SelectableButtonStyler.applyEnabled(btnNext, anyChecked);
    }

    /**
     * Handles Next button click: saves current exercise results and navigates forward.
     */
    private void onNextClicked() {
        saveCurrentExerciseResult();
        goForward();
    }

    /**
     * Skips the current exercise: nothing is counted for it (0 XP) and every set
     * is left unticked in case the user comes back to it.
     */
    private void onSkipClicked() {
        WorkoutExercise workoutExercise = exercises.get(currentExerciseIndex);

        List<SetInput> rawSets = new ArrayList<>();
        for (int i = 0; i < llSetRows.getChildCount(); i++) {
            View row = llSetRows.getChildAt(i);
            EditText etReps = row.findViewById(R.id.etReps);
            EditText etWeight = row.findViewById(R.id.etWeight);
            rawSets.add(new SetInput(etReps.getText().toString().trim(),
                    etWeight.getText().toString().trim(), false));
        }
        enteredSetsByIndex.put(currentExerciseIndex, rawSets);

        storeResult(new ExerciseResult(workoutExercise.getExerciseId(),
                workoutExercise.getExercise().getBaseXp(), 0, 0, 0));
        goForward();
    }

    /** Moves to the next exercise, or finishes the workout after the last one. */
    private void goForward() {
        if (currentExerciseIndex < exercises.size() - 1) {
            currentExerciseIndex++;
            displayCurrentExercise();
        } else {
            finishWorkout();
        }
    }

    /**
     * Finishes the workout if at least one set was done. If every exercise was
     * skipped there is nothing to save, so the user can go back or quit.
     */
    private void finishWorkout() {
        int setsDone = 0;
        for (ExerciseResult r : exerciseResults) {
            if (r != null) setsDone += r.getSetsCompleted();
        }
        if (setsDone == 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.workout_nothing_logged_title)
                    .setMessage(R.string.workout_nothing_logged_message)
                    .setPositiveButton(R.string.workout_nothing_logged_back, null)
                    .setNegativeButton(R.string.workout_nothing_logged_quit, (d, w) -> finish())
                    .show();
            return;
        }
        navigateToComplete();
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
        double maxWeight = 0;

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

                // Only a ticked set with at least 1 rep counts (and earns XP).
                if (reps > 0) {
                    setsCompleted++;
                    totalReps += reps;
                    totalWeight += weight;
                    maxWeight = Math.max(maxWeight, weight);
                }
            }
        }

        // Preserve the raw input for this exercise index (overwrites any prior snapshot).
        enteredSetsByIndex.put(currentExerciseIndex, rawSets);

        // Store the aggregate result at this exercise's index, overwriting any
        // previous result for the same exercise rather than appending a duplicate.
        storeResult(new ExerciseResult(exerciseId, baseXp, setsCompleted, totalReps, totalWeight, maxWeight));
    }

    /** Stores the result at the current exercise's index (replacing an earlier one). */
    private void storeResult(ExerciseResult result) {
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
