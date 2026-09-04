package com.jve.repsup.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.fragments.CreateWorkoutModeFragment;
import com.jve.repsup.fragments.GenerateWorkoutOptionsFragment;
import com.jve.repsup.fragments.UserExerciseSelectionFragment;
import com.jve.repsup.fragments.UserMuscleGroupSelectionFragment;
import com.jve.repsup.fragments.UserWorkoutDetailsFragment;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multi-step activity for users to create or generate a custom workout.
 * Flow options:
 * - Create mode: Mode → Muscle Groups → Exercise Selection → Details/Save
 * - Generate mode: Mode → Difficulty + Muscle Groups → Review/Details → Save
 * - Edit mode: Skip mode selection, go directly to details pre-populated with existing data
 */
public class CreateWorkoutActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_WORKOUT_ID = "extra_edit_workout_id";
    public static final int MODE_CREATE = 0;
    public static final int MODE_GENERATE = 1;
    public static final int MODE_EDIT = 2;
    public static final int MAX_EXERCISES = 8;

    private int selectedMode = -1;
    private List<String> selectedMuscleGroups = new ArrayList<>();
    private List<Exercise> selectedExercises = new ArrayList<>();
    private String selectedDifficulty;

    // Edit mode fields
    private boolean isEditMode = false;
    private int editWorkoutId = -1;
    private String editWorkoutName = "";
    private List<Integer> editRepsList = new ArrayList<>();
    private List<Double> editWeightList = new ArrayList<>();

    private WorkoutRepository workoutRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        workoutRepository = new WorkoutRepository();
        sessionManager = new SessionManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        // Check if we are in edit mode
        editWorkoutId = getIntent().getIntExtra(EXTRA_EDIT_WORKOUT_ID, -1);
        isEditMode = editWorkoutId != -1;

        if (savedInstanceState == null) {
            if (isEditMode) {
                selectedMode = MODE_EDIT;
                loadWorkoutForEditing();
            } else {
                // Start with mode selection
                showFragment(new CreateWorkoutModeFragment(), false);
            }
        }
    }

    /**
     * Loads workout data for editing and navigates directly to the details screen.
     */
    private void loadWorkoutForEditing() {
        workoutRepository.getWorkoutWithExercises(editWorkoutId, new ConnectionManager.DatabaseCallback<Workout>() {
            @Override
            public void onSuccess(Workout workout) {
                if (workout == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateWorkoutActivity.this,
                                R.string.edit_workout_load_error, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                // Extract exercise data from the workout
                editWorkoutName = workout.getName();
                List<WorkoutExercise> workoutExercises = workout.getExercises();
                selectedExercises.clear();
                editRepsList.clear();
                editWeightList.clear();

                if (workoutExercises != null) {
                    for (WorkoutExercise we : workoutExercises) {
                        Exercise exercise = we.getExercise();
                        if (exercise != null) {
                            selectedExercises.add(exercise);
                            editRepsList.add(we.getReps());
                            editWeightList.add(we.getTargetWeight());
                        }
                    }
                }

                runOnUiThread(() -> showFragment(new UserWorkoutDetailsFragment(), false));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateWorkoutActivity.this,
                            R.string.edit_workout_load_error, Toast.LENGTH_SHORT).show();
                    finish();
                });
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

    // ─── Navigation callbacks from fragments ───

    /**
     * Called when user selects Create or Generate mode.
     */
    public void onModeSelected(int mode) {
        this.selectedMode = mode;
        if (mode == MODE_CREATE) {
            showFragment(new UserMuscleGroupSelectionFragment(), true);
        } else {
            showFragment(new GenerateWorkoutOptionsFragment(), true);
        }
    }

    /**
     * Called when user confirms muscle group selection (Create mode).
     */
    public void onMuscleGroupsSelected(List<String> muscleGroups) {
        this.selectedMuscleGroups = muscleGroups;
        showFragment(new UserExerciseSelectionFragment(), true);
    }

    /**
     * Called when user confirms exercise selection (Create mode).
     */
    public void onExercisesSelected(List<Exercise> exercises) {
        this.selectedExercises = exercises;
        showFragment(new UserWorkoutDetailsFragment(), true);
    }

    /**
     * Called when user requests workout generation (Generate mode).
     * Loads exercises matching difficulty + muscle groups, picks up to MAX_EXERCISES randomly.
     */
    public void onGenerateRequested(String difficulty, List<String> muscleGroups) {
        this.selectedDifficulty = difficulty;
        this.selectedMuscleGroups = muscleGroups;

        // Load exercises matching the criteria from all selected muscle groups
        List<Exercise> allMatching = new ArrayList<>();
        final int[] loadedCount = {0};
        final int totalGroups = muscleGroups.size();

        for (String group : muscleGroups) {
            workoutRepository.getExercisesByMuscleGroup(group, new ConnectionManager.DatabaseCallback<List<Exercise>>() {
                @Override
                public void onSuccess(List<Exercise> result) {
                    synchronized (allMatching) {
                        if (result != null) {
                            for (Exercise exercise : result) {
                                if (exercise.getDifficulty() != null &&
                                        exercise.getDifficulty().equalsIgnoreCase(difficulty)) {
                                    allMatching.add(exercise);
                                }
                            }
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] >= totalGroups) {
                            runOnUiThread(() -> onExercisesLoaded(allMatching));
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    synchronized (allMatching) {
                        loadedCount[0]++;
                        if (loadedCount[0] >= totalGroups) {
                            runOnUiThread(() -> onExercisesLoaded(allMatching));
                        }
                    }
                }
            });
        }
    }

    /**
     * After exercises are loaded for generate mode, pick up to MAX_EXERCISES randomly
     * and navigate to the details/review screen.
     */
    private void onExercisesLoaded(List<Exercise> exercises) {
        if (exercises.isEmpty()) {
            Toast.makeText(this, R.string.generate_workout_no_exercises, Toast.LENGTH_SHORT).show();
            return;
        }

        // Shuffle and pick up to MAX_EXERCISES
        Collections.shuffle(exercises);
        if (exercises.size() > MAX_EXERCISES) {
            exercises = exercises.subList(0, MAX_EXERCISES);
        }

        this.selectedExercises = new ArrayList<>(exercises);
        showFragment(new UserWorkoutDetailsFragment(), true);
    }

    /**
     * Called when the user saves the workout with name and per-exercise reps/weight.
     * Handles both create (new workout) and edit (update existing) modes.
     */
    public void onWorkoutSaved(String workoutName, List<Integer> repsList, List<Double> weightList) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, R.string.create_workout_error, Toast.LENGTH_SHORT).show();
            return;
        }

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        for (int i = 0; i < selectedExercises.size(); i++) {
            Exercise exercise = selectedExercises.get(i);
            WorkoutExercise we = new WorkoutExercise();
            we.setExerciseId(exercise.getExerciseId());
            we.setReps(repsList.get(i));
            we.setSets(exercise.getDefaultSets());
            we.setOrderIndex(i + 1);
            if (weightList != null && i < weightList.size()) {
                we.setTargetWeight(weightList.get(i));
            }
            workoutExercises.add(we);
        }

        if (isEditMode) {
            // Update existing workout
            Workout workout = new Workout();
            workout.setWorkoutId(editWorkoutId);
            workout.setUserId(userId);
            workout.setName(workoutName);
            workout.setDefault(false);
            workout.setOrderIndex(0);

            workoutRepository.updateWorkout(workout, workoutExercises, new ConnectionManager.DatabaseCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateWorkoutActivity.this,
                                R.string.edit_workout_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateWorkoutActivity.this,
                                R.string.edit_workout_error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // Create new workout
            Workout workout = new Workout();
            workout.setUserId(userId);
            workout.setName(workoutName);
            workout.setDefault(false);
            workout.setOrderIndex(0);

            workoutRepository.createWorkout(workout, workoutExercises, new ConnectionManager.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long workoutId) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateWorkoutActivity.this,
                                R.string.create_workout_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateWorkoutActivity.this,
                                R.string.create_workout_error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    // ─── Getters for fragments ───

    public List<String> getSelectedMuscleGroups() {
        return selectedMuscleGroups;
    }

    public List<Exercise> getSelectedExercises() {
        return selectedExercises;
    }

    public int getSelectedMode() {
        return selectedMode;
    }

    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public String getEditWorkoutName() {
        return editWorkoutName;
    }

    public List<Integer> getEditRepsList() {
        return editRepsList;
    }

    public List<Double> getEditWeightList() {
        return editWeightList;
    }
}
