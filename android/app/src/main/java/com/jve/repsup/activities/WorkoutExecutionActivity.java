package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.jve.repsup.utils.SessionManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    private WorkoutRepository workoutRepository;
    private ScheduleRepository scheduleRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_execution);

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

        btnAddSet.setOnClickListener(v -> addSetRow());
        btnNext.setOnClickListener(v -> onNextClicked());
    }

    /**
     * Validates that the workout is scheduled for today, then loads exercises.
     */
    private void validateAndLoad() {
        if (scheduledWorkoutId == -1 || workoutId == -1) {
            showNotScheduledError();
            return;
        }

        // Verify this scheduled workout belongs to the user and is for today
        String sql = "SELECT scheduled_workout_id FROM scheduled_workout " +
                "WHERE scheduled_workout_id = ? AND user_id = ? " +
                "AND scheduled_date = CURDATE() AND is_completed = 0";

        int userId = sessionManager.getUserId();

        ConnectionManager.querySingle(sql, new Object[]{scheduledWorkoutId, userId}, rs -> {
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

        // Set demo image placeholder
        ivExerciseDemo.setImageResource(R.drawable.ic_exercise_placeholder);

        // Update button text for last exercise
        if (currentExerciseIndex == exercises.size() - 1) {
            btnNext.setText(R.string.workout_execution_finish);
        } else {
            btnNext.setText(R.string.workout_execution_next);
        }

        // Clear and populate set rows
        llSetRows.removeAllViews();
        int defaultSets = workoutExercise.getSets();
        if (defaultSets <= 0) {
            defaultSets = 3; // fallback
        }
        for (int i = 0; i < defaultSets; i++) {
            addSetRow(workoutExercise.getReps());
        }

        // Reset next button state
        btnNext.setEnabled(false);
        updateNextButtonState();
    }

    /**
     * Adds a new set row with empty reps/weight fields.
     */
    private void addSetRow() {
        addSetRow(0);
    }

    /**
     * Adds a new set row with pre-populated default reps.
     */
    private void addSetRow(int defaultReps) {
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
     * Collects data from set rows and creates an ExerciseResult for the current exercise.
     */
    private void saveCurrentExerciseResult() {
        WorkoutExercise workoutExercise = exercises.get(currentExerciseIndex);
        int exerciseId = workoutExercise.getExerciseId();
        int baseXp = workoutExercise.getExercise().getBaseXp();

        int setsCompleted = 0;
        int totalReps = 0;
        double totalWeight = 0;

        int childCount = llSetRows.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View row = llSetRows.getChildAt(i);
            CheckBox cbDone = row.findViewById(R.id.cbDone);

            if (cbDone.isChecked()) {
                setsCompleted++;

                EditText etReps = row.findViewById(R.id.etReps);
                EditText etWeight = row.findViewById(R.id.etWeight);

                String repsText = etReps.getText().toString().trim();
                String weightText = etWeight.getText().toString().trim();

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

        ExerciseResult result = new ExerciseResult(exerciseId, baseXp, setsCompleted, totalReps, totalWeight);
        exerciseResults.add(result);
    }

    /**
     * Navigates to the WorkoutCompleteActivity with all exercise results.
     */
    private void navigateToComplete() {
        Intent intent = new Intent(this, WorkoutCompleteActivity.class);
        intent.putExtra(EXTRA_SCHEDULED_WORKOUT_ID, scheduledWorkoutId);
        intent.putExtra(EXTRA_WORKOUT_ID, workoutId);
        intent.putExtra(EXTRA_EXERCISE_RESULTS, (Serializable) exerciseResults);
        startActivity(intent);
        finish();
    }
}
