package com.jve.repsup.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.SnackbarUtils;
import com.jve.repsup.repositories.WorkoutRepository;

/**
 * Activity for administrators to create a new default exercise.
 * Validates required fields and inserts an Exercise record via WorkoutRepository.
 */
public class AdminCreateExerciseActivity extends AppCompatActivity {

    private TextInputLayout tilExerciseName, tilMuscleGroup, tilDifficulty;
    private TextInputEditText etExerciseName, etDefaultSets, etDefaultReps, etBaseXp;
    private AutoCompleteTextView actvMuscleGroup, actvDifficulty;
    private Button btnSaveExercise;

    private WorkoutRepository workoutRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Must match the muscle groups the user-facing workout UI queries, otherwise
    // admin-created exercises land in a group users can't browse.
    private static final String[] MUSCLE_GROUPS = {
            "Chest", "Back", "Shoulders", "Biceps", "Triceps",
            "Legs", "Glutes", "Abs", "Calves", "Forearms"
    };

    private static final String[] DIFFICULTIES = {"Easy", "Medium", "Hard"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_exercise);

        workoutRepository = new WorkoutRepository();

        initViews();
        setupDropdowns();
        setupListeners();
    }

    /**
     * Initializes view references.
     */
    private void initViews() {
        tilExerciseName = findViewById(R.id.tilExerciseName);
        tilMuscleGroup = findViewById(R.id.tilMuscleGroup);
        tilDifficulty = findViewById(R.id.tilDifficulty);

        etExerciseName = findViewById(R.id.etExerciseName);
        etDefaultSets = findViewById(R.id.etDefaultSets);
        etDefaultReps = findViewById(R.id.etDefaultReps);
        etBaseXp = findViewById(R.id.etBaseXp);

        actvMuscleGroup = findViewById(R.id.actvMuscleGroup);
        actvDifficulty = findViewById(R.id.actvDifficulty);

        btnSaveExercise = findViewById(R.id.btnSaveExercise);
    }

    /**
     * Sets up dropdown adapters for muscle group and difficulty fields.
     */
    private void setupDropdowns() {
        ArrayAdapter<String> muscleGroupAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, MUSCLE_GROUPS);
        actvMuscleGroup.setAdapter(muscleGroupAdapter);

        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, DIFFICULTIES);
        actvDifficulty.setAdapter(difficultyAdapter);
    }

    /**
     * Sets up click listeners.
     */
    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSaveExercise.setOnClickListener(v -> attemptSave());
    }

    /**
     * Validates form fields and attempts to save the exercise.
     */
    private void attemptSave() {
        // Clear previous errors
        tilExerciseName.setError(null);
        tilMuscleGroup.setError(null);
        tilDifficulty.setError(null);

        String name = etExerciseName.getText() != null ? etExerciseName.getText().toString().trim() : "";
        String muscleGroup = actvMuscleGroup.getText() != null ? actvMuscleGroup.getText().toString().trim() : "";
        String difficulty = actvDifficulty.getText() != null ? actvDifficulty.getText().toString().trim() : "";
        String setsStr = etDefaultSets.getText() != null ? etDefaultSets.getText().toString().trim() : "";
        String repsStr = etDefaultReps.getText() != null ? etDefaultReps.getText().toString().trim() : "";
        String xpStr = etBaseXp.getText() != null ? etBaseXp.getText().toString().trim() : "";

        boolean valid = true;

        if (name.isEmpty()) {
            tilExerciseName.setError(getString(R.string.error_exercise_name_required));
            valid = false;
        }

        if (muscleGroup.isEmpty()) {
            tilMuscleGroup.setError(getString(R.string.error_muscle_group_required));
            valid = false;
        }

        if (difficulty.isEmpty()) {
            tilDifficulty.setError(getString(R.string.error_difficulty_required));
            valid = false;
        }

        if (!valid) {
            return;
        }

        // Parse optional numeric fields (default to 0 if empty)
        int defaultSets = setsStr.isEmpty() ? 0 : Integer.parseInt(setsStr);
        int defaultReps = repsStr.isEmpty() ? 0 : Integer.parseInt(repsStr);
        int baseXp = xpStr.isEmpty() ? 0 : Integer.parseInt(xpStr);

        // Build Exercise object
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setMuscleGroup(muscleGroup);
        exercise.setDifficulty(difficulty);
        exercise.setDefaultSets(defaultSets);
        exercise.setDefaultReps(defaultReps);
        exercise.setBaseXp(baseXp);

        // Disable button to prevent double-submission
        btnSaveExercise.setEnabled(false);

        workoutRepository.createExercise(exercise, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long generatedId) {
                mainHandler.post(() -> {
                    SnackbarUtils.show(AdminCreateExerciseActivity.this, R.string.exercise_created_success);
                    findViewById(android.R.id.content).postDelayed(() -> finish(), 1500);
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    btnSaveExercise.setEnabled(true);
                    DialogUtils.showError(AdminCreateExerciseActivity.this,
                            R.string.exercise_created_error);
                });
            }
        });
    }
}
