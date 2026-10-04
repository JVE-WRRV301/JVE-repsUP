package com.jve.repsup.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jve.repsup.R;
import com.jve.repsup.fragments.CreateWorkoutCategoryFragment;
import com.jve.repsup.fragments.CreateWorkoutExercisesFragment;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.fragments.CreateWorkoutMuscleGroupFragment;
import com.jve.repsup.fragments.CreateWorkoutNamingFragment;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.WorkoutRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-step activity for admins to create a default workout.
 * Flow: Category -> Muscle Group -> Exercise Selection -> Naming/Save
 */
public class AdminCreateWorkoutActivity extends AppCompatActivity {

    // Category to muscle group mappings
    public static final Map<String, List<String>> CATEGORY_MUSCLE_GROUPS = new HashMap<>();

    static {
        CATEGORY_MUSCLE_GROUPS.put("Abdominal", Arrays.asList("Abs", "Obliques", "Core"));
        CATEGORY_MUSCLE_GROUPS.put("Full Body", Arrays.asList("Full Body", "Compound"));
        CATEGORY_MUSCLE_GROUPS.put("Lower Body", Arrays.asList("Quads", "Hamstrings", "Glutes", "Calves"));
        CATEGORY_MUSCLE_GROUPS.put("Upper Body", Arrays.asList("Chest", "Back", "Shoulders", "Biceps", "Triceps"));
    }

    private String selectedCategory;
    private String selectedMuscleGroup;
    private List<Exercise> selectedExercises = new ArrayList<>();

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

        // Start with the category selection fragment
        if (savedInstanceState == null) {
            showFragment(new CreateWorkoutCategoryFragment(), false);
        }
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

    // --- Navigation methods called by fragments ---

    public void onCategorySelected(String category) {
        this.selectedCategory = category;
        showFragment(CreateWorkoutMuscleGroupFragment.newInstance(category), true);
    }

    public void onMuscleGroupSelected(String muscleGroup) {
        this.selectedMuscleGroup = muscleGroup;
        showFragment(CreateWorkoutExercisesFragment.newInstance(muscleGroup), true);
    }

    public void onExercisesConfirmed(List<Exercise> exercises) {
        this.selectedExercises = exercises;
        showFragment(new CreateWorkoutNamingFragment(), true);
    }

    public void onWorkoutSaved(String workoutName, int sets, int reps) {
        Workout workout = new Workout();
        workout.setUserId(null); // default workout has no user
        workout.setName(workoutName);
        workout.setDefault(true);
        workout.setOrderIndex(0);

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        for (int i = 0; i < selectedExercises.size(); i++) {
            Exercise exercise = selectedExercises.get(i);
            WorkoutExercise we = new WorkoutExercise();
            we.setExerciseId(exercise.getExerciseId());
            we.setSets(sets);
            we.setReps(reps);
            we.setOrderIndex(i + 1);
            workoutExercises.add(we);
        }

        workoutRepository.createWorkout(workout, workoutExercises, new com.jve.repsup.database.ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long workoutId) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminCreateWorkoutActivity.this,
                            getString(R.string.admin_workout_created), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        DialogUtils.showError(AdminCreateWorkoutActivity.this,
                                getString(R.string.admin_workout_create_error)));
            }
        });
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public List<Exercise> getSelectedExercises() {
        return selectedExercises;
    }
}
