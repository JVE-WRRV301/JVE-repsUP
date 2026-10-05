package com.jve.repsup.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.fragments.AdminWorkoutReviewFragment;
import com.jve.repsup.fragments.CreateWorkoutExercisesFragment;
import com.jve.repsup.fragments.CreateWorkoutMuscleGroupFragment;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.SnackbarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Multi-step activity for admins to create (or edit) a default workout.
 *
 * Flow: Muscle Group list (10 real groups) -> Exercises for group (tap exercise,
 * enter its sets/reps, Add) -> Review (name + per-exercise sets/reps, remove,
 * add more, finish/save).
 *
 * Default workouts have user_id = NULL and is_default = true.
 */
public class AdminCreateWorkoutActivity extends AppCompatActivity {

    public static final int MAX_EXERCISES = 8;

    /**
     * The only muscle groups the app recognises (match the exercise table and
     * the user-facing workout UI). The admin create flow lists these directly;
     * there is no "category" layer.
     */
    public static final List<String> MUSCLE_GROUPS = Arrays.asList(
            "Chest", "Back", "Shoulders", "Biceps", "Triceps",
            "Legs", "Glutes", "Abs", "Calves", "Forearms"
    );

    // Running selection: each entry carries the exercise plus its chosen sets/reps.
    private final List<WorkoutExercise> selectedExercises = new ArrayList<>();

    // Edit mode: when set, saving updates this existing default workout instead
    // of creating a new one.
    private boolean isEditMode = false;
    private int editWorkoutId = -1;
    private String editWorkoutName;

    private WorkoutRepository workoutRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_workout);

        workoutRepository = new WorkoutRepository();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        editWorkoutId = getIntent().getIntExtra(
                AdminManageWorkoutsActivity.EXTRA_EDIT_WORKOUT_ID, -1);
        isEditMode = editWorkoutId != -1;

        if (savedInstanceState == null) {
            if (isEditMode) {
                // Load the existing workout, then open the review/detail screen so
                // the admin sees its exercises + sets/reps immediately. "Add More
                // Exercises" from there drops into the muscle group list.
                loadExistingWorkout(editWorkoutId);
            } else {
                // New workout: start at the muscle group list (no category step).
                showFragment(new CreateWorkoutMuscleGroupFragment(), false);
            }
        }
    }

    /**
     * Loads the workout being edited so its current exercises (with their sets/reps)
     * seed the running selection. Saving later updates this workout in place.
     */
    private void loadExistingWorkout(int workoutId) {
        workoutRepository.getWorkoutWithExercises(workoutId,
                new ConnectionManager.DatabaseCallback<Workout>() {
                    @Override
                    public void onSuccess(Workout workout) {
                        if (workout == null) return;
                        runOnUiThread(() -> {
                            editWorkoutName = workout.getName();
                            selectedExercises.clear();
                            if (workout.getExercises() != null) {
                                selectedExercises.addAll(workout.getExercises());
                            }
                            // Open the detail/review screen with the loaded data.
                            showFragment(new AdminWorkoutReviewFragment(), false);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> DialogUtils.showError(
                                AdminCreateWorkoutActivity.this,
                                getString(R.string.admin_load_workouts_error)));
                    }
                });
    }

    /**
     * Replaces the current fragment in the container.
     */
    private void showFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // --- Navigation callbacks from fragments ---

    /** A muscle group was tapped: show its exercises. */
    public void onMuscleGroupSelected(String muscleGroup) {
        showFragment(CreateWorkoutExercisesFragment.newInstance(muscleGroup), true);
    }

    /**
     * Adds a single exercise with its chosen sets/reps to the running selection.
     * If the exercise is already in the workout, updates its sets/reps instead of
     * adding a duplicate. Returns false if the workout is already full.
     */
    public boolean addExerciseWithSetsReps(Exercise exercise, int sets, int reps) {
        // Update in place if already present.
        for (WorkoutExercise we : selectedExercises) {
            if (we.getExerciseId() == exercise.getExerciseId()) {
                we.setSets(sets);
                we.setReps(reps);
                return true;
            }
        }
        if (selectedExercises.size() >= MAX_EXERCISES) {
            return false;
        }
        WorkoutExercise we = new WorkoutExercise();
        we.setExerciseId(exercise.getExerciseId());
        we.setExercise(exercise);
        we.setSets(sets);
        we.setReps(reps);
        selectedExercises.add(we);
        return true;
    }

    /** Opens the review page (name + per-exercise sets/reps + save). */
    public void openReview() {
        showFragment(new AdminWorkoutReviewFragment(), true);
    }

    /**
     * From the review screen, go to the muscle group list to add more exercises.
     * In the create flow a muscle group screen already sits beneath review, so we
     * pop back to it; in the edit flow review is the root, so we push a fresh one.
     */
    public void addMoreExercises() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            showFragment(new CreateWorkoutMuscleGroupFragment(), true);
        }
    }

    /**
     * Saves the workout from the review page: creates a new default workout, or
     * updates the existing one in edit mode (never duplicates).
     */
    public void saveWorkout(String workoutName) {
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        for (int i = 0; i < selectedExercises.size(); i++) {
            WorkoutExercise src = selectedExercises.get(i);
            WorkoutExercise we = new WorkoutExercise();
            we.setExerciseId(src.getExerciseId());
            we.setSets(src.getSets());
            we.setReps(src.getReps());
            we.setOrderIndex(i + 1);
            workoutExercises.add(we);
        }

        Workout workout = new Workout();
        workout.setUserId(null); // default workout has no user
        workout.setName(workoutName);
        workout.setDefault(true);
        workout.setOrderIndex(0);

        if (isEditMode) {
            workout.setWorkoutId(editWorkoutId);
            workoutRepository.updateWorkout(workout, workoutExercises,
                    new ConnectionManager.DatabaseCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            runOnUiThread(() -> onSaveSuccess());
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> DialogUtils.showError(
                                    AdminCreateWorkoutActivity.this,
                                    getString(R.string.admin_workout_create_error)));
                        }
                    });
            return;
        }

        workoutRepository.createWorkout(workout, workoutExercises,
                new ConnectionManager.DatabaseCallback<Long>() {
                    @Override
                    public void onSuccess(Long workoutId) {
                        runOnUiThread(() -> onSaveSuccess());
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> DialogUtils.showError(
                                AdminCreateWorkoutActivity.this,
                                getString(R.string.admin_workout_create_error)));
                    }
                });
    }

    private void onSaveSuccess() {
        SnackbarUtils.show(this, getString(R.string.admin_workout_created));
        findViewById(android.R.id.content).postDelayed(this::finish, 1500);
    }

    // --- Accessors used by fragments ---

    public List<WorkoutExercise> getSelectedExercises() {
        return selectedExercises;
    }

    /** Number of exercises added so far (across all muscle groups). */
    public int getSelectedCount() {
        return selectedExercises.size();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public String getEditWorkoutName() {
        return editWorkoutName;
    }
}
