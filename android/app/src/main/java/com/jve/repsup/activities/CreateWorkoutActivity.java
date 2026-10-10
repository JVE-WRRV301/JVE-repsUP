package com.jve.repsup.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jve.repsup.R;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.SnackbarUtils;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.fragments.CreateWorkoutModeFragment;
import com.jve.repsup.fragments.GenerateWorkoutOptionsFragment;
import com.jve.repsup.fragments.UserExerciseSelectionFragment;
import com.jve.repsup.fragments.UserMuscleGroupSelectionFragment;
import com.jve.repsup.fragments.UserWorkoutDetailsFragment;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.SessionManager;

import com.jve.repsup.utils.WorkoutValidator;

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
    // When editing from My Plan, the scheduled_workout row being edited. Lets us
    // repoint it at a personal copy when the user edits a default/other workout.
    public static final String EXTRA_SCHEDULED_WORKOUT_ID = "extra_scheduled_workout_id";
    public static final int MODE_CREATE = 0;
    public static final int MODE_GENERATE = 1;
    public static final int MODE_EDIT = 2;
    public static final int MAX_EXERCISES = WorkoutValidator.MAX_EXERCISES;

    private static final List<String> LOWER_BODY_GROUPS =
            java.util.Arrays.asList("Legs", "Glutes", "Calves");

    private int selectedMode = -1;
    private List<String> selectedMuscleGroups = new ArrayList<>();
    private List<Exercise> selectedExercises = new ArrayList<>();
    private String selectedDifficulty;

    // True while the user is picking additional exercises to merge into the current set.
    private boolean addingExercises = false;

    // Draft reps/weight values kept in sync with selectedExercises (by index).
    // These survive navigating to the add-exercises sub-flow and back.
    private List<Integer> draftSets = new ArrayList<>();
    private List<Integer> draftReps = new ArrayList<>();
    private List<Double> draftWeights = new ArrayList<>();

    // True while a save is running, so a second tap cannot create a duplicate.
    private boolean saving = false;

    // Edit mode fields
    private boolean isEditMode = false;
    private int editWorkoutId = -1;
    private String editWorkoutName = "";
    // The scheduled_workout row being edited (from My Plan), or -1 when editing a
    // workout directly from the library.
    private int editScheduledWorkoutId = -1;
    // Ownership of the workout being edited, read when it loads. Used to decide
    // whether a save can update in place or must save a personal copy instead.
    private Integer editWorkoutOwnerId = null;
    private boolean editWorkoutIsDefault = false;

    private WorkoutRepository workoutRepository;
    private ScheduleRepository scheduleRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        workoutRepository = new WorkoutRepository();
        scheduleRepository = new ScheduleRepository();
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
        editScheduledWorkoutId = getIntent().getIntExtra(EXTRA_SCHEDULED_WORKOUT_ID, -1);
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
                        DialogUtils.showError(CreateWorkoutActivity.this, R.string.edit_workout_load_error);
                        finish();
                    });
                    return;
                }

                // Extract exercise data from the workout
                editWorkoutName = workout.getName();
                editWorkoutOwnerId = workout.getUserId();
                editWorkoutIsDefault = workout.isDefault();
                List<WorkoutExercise> workoutExercises = workout.getExercises();
                selectedExercises.clear();
                draftSets.clear();
                draftReps.clear();
                draftWeights.clear();

                if (workoutExercises != null) {
                    for (WorkoutExercise we : workoutExercises) {
                        Exercise exercise = we.getExercise();
                        if (exercise != null) {
                            selectedExercises.add(exercise);
                            draftSets.add(we.getSets());
                            draftReps.add(we.getReps());
                            draftWeights.add(we.getTargetWeight());
                        }
                    }
                }

                runOnUiThread(() -> showFragment(new UserWorkoutDetailsFragment(), false));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    DialogUtils.showError(CreateWorkoutActivity.this, R.string.edit_workout_load_error);
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
     * Called when user confirms exercise selection.
     * In the normal create flow this sets the exercise list and moves to details.
     * When adding exercises to an existing set (e.g. editing a workout), the newly
     * selected exercises are merged into the current list, skipping duplicates and
     * respecting the MAX_EXERCISES limit, then we return to the details screen.
     */
    public void onExercisesSelected(List<Exercise> exercises) {
        if (addingExercises) {
            mergeExercises(exercises);
            addingExercises = false;
            // Pop the muscle-group and exercise-selection screens off the back stack,
            // then show a fresh details screen so it rebuilds with the merged exercises.
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().popBackStack();
            showFragment(new UserWorkoutDetailsFragment(), false);
        } else {
            this.selectedExercises = exercises;
            seedDraftsFromExercises();
            showFragment(new UserWorkoutDetailsFragment(), true);
        }
    }

    /**
     * Initialises draft reps/weight lists from the current exercises' defaults.
     */
    private void seedDraftsFromExercises() {
        draftSets.clear();
        draftReps.clear();
        draftWeights.clear();
        for (Exercise exercise : selectedExercises) {
            draftSets.add(exercise.getDefaultSets());
            draftReps.add(exercise.getDefaultReps());
            draftWeights.add(0.0);
        }
    }

    /**
     * Merges newly selected exercises into the current selection without duplicating
     * exercises already present, capping the total at MAX_EXERCISES.
     */
    private void mergeExercises(List<Exercise> newExercises) {
        for (Exercise exercise : newExercises) {
            if (selectedExercises.size() >= MAX_EXERCISES) {
                SnackbarUtils.show(this, R.string.create_workout_max_exercises_reached);
                break;
            }
            boolean alreadyPresent = false;
            for (Exercise existing : selectedExercises) {
                if (existing.getExerciseId() == exercise.getExerciseId()) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) {
                selectedExercises.add(exercise);
                draftSets.add(exercise.getDefaultSets());
                draftReps.add(exercise.getDefaultReps());
                draftWeights.add(0.0);
            }
        }
    }

    /**
     * Persists the reps/weight the user has currently entered on the details screen so
     * the values are not lost when navigating to add more exercises.
     */
    public void saveDraft(List<Integer> setsList, List<Integer> repsList, List<Double> weightList) {
        this.draftSets = new ArrayList<>(setsList);
        this.draftReps = new ArrayList<>(repsList);
        this.draftWeights = new ArrayList<>(weightList);
    }

    /**
     * Called from the details screen to start the add-exercises sub-flow.
     * Reuses the muscle group -> exercise selection screens.
     */
    public void onAddExercisesRequested() {
        if (selectedExercises.size() >= MAX_EXERCISES) {
            SnackbarUtils.show(this, R.string.create_workout_max_exercises_reached);
            return;
        }
        addingExercises = true;
        showFragment(new UserMuscleGroupSelectionFragment(), true);
    }

    /**
     * Called when user requests workout generation (Generate mode).
     * Loads the exercises for every chosen muscle group, then builds a realistic
     * workout: a sensible number of exercises (see targetExerciseCount) spread
     * evenly across the chosen groups.
     */
    public void onGenerateRequested(String difficulty, List<String> muscleGroups) {
        this.selectedDifficulty = difficulty;
        this.selectedMuscleGroups = muscleGroups;

        // Exercises per muscle group, filled as each query returns.
        final java.util.Map<String, List<Exercise>> byGroup = new java.util.HashMap<>();
        final int[] loadedCount = {0};
        final int totalGroups = muscleGroups.size();

        for (String group : muscleGroups) {
            workoutRepository.getExercisesByMuscleGroup(group, new ConnectionManager.DatabaseCallback<List<Exercise>>() {
                @Override
                public void onSuccess(List<Exercise> result) {
                    synchronized (byGroup) {
                        if (result != null) {
                            // Include every exercise from the group, regardless of
                            // difficulty. The chosen difficulty only decides which are
                            // picked first, so a workout stays populated even when few
                            // match exactly.
                            byGroup.put(group, result);
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] >= totalGroups) {
                            runOnUiThread(() -> onExercisesLoaded(byGroup));
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    synchronized (byGroup) {
                        loadedCount[0]++;
                        if (loadedCount[0] >= totalGroups) {
                            runOnUiThread(() -> onExercisesLoaded(byGroup));
                        }
                    }
                }
            });
        }
    }

    /**
     * How many exercises a generated workout should have. Lower-body-only
     * workouts (Legs, Glutes, Calves) are 4 / 4 / 5 for Easy / Medium / Hard;
     * anything that involves the upper body is 5 / 5 / 6. If more muscle groups
     * are chosen than that, there is one exercise per group so none is left out.
     */
    static int targetExerciseCount(String difficulty, List<String> groups) {
        boolean lowerBodyOnly = !groups.isEmpty();
        for (String group : groups) {
            if (!LOWER_BODY_GROUPS.contains(group)) {
                lowerBodyOnly = false;
                break;
            }
        }
        boolean hard = "Hard".equalsIgnoreCase(difficulty);
        int count = lowerBodyOnly ? (hard ? 5 : 4) : (hard ? 6 : 5);
        return Math.min(MAX_EXERCISES, Math.max(count, groups.size()));
    }

    /**
     * After the exercises are loaded for generate mode, build the workout by
     * taking one exercise from each chosen group in turn until the target count
     * is reached, then open the details/review screen.
     */
    private void onExercisesLoaded(java.util.Map<String, List<Exercise>> byGroup) {
        // Within each group: exercises matching the chosen difficulty first
        // (shuffled), then the rest (shuffled).
        List<List<Exercise>> queues = new ArrayList<>();
        for (String group : selectedMuscleGroups) {
            List<Exercise> groupExercises = byGroup.get(group);
            if (groupExercises == null || groupExercises.isEmpty()) continue;

            List<Exercise> preferred = new ArrayList<>();
            List<Exercise> others = new ArrayList<>();
            for (Exercise exercise : groupExercises) {
                if (selectedDifficulty != null && exercise.getDifficulty() != null
                        && exercise.getDifficulty().equalsIgnoreCase(selectedDifficulty)) {
                    preferred.add(exercise);
                } else {
                    others.add(exercise);
                }
            }
            Collections.shuffle(preferred);
            Collections.shuffle(others);
            List<Exercise> queue = new ArrayList<>(preferred);
            queue.addAll(others);
            queues.add(queue);
        }

        if (queues.isEmpty()) {
            SnackbarUtils.show(this, R.string.generate_workout_no_exercises);
            return;
        }

        int target = targetExerciseCount(selectedDifficulty, selectedMuscleGroups);
        List<Exercise> picked = new ArrayList<>();
        java.util.Set<Integer> usedIds = new java.util.HashSet<>();
        boolean tookAny = true;
        while (picked.size() < target && tookAny) {
            tookAny = false;
            for (List<Exercise> queue : queues) {
                if (picked.size() >= target) break;
                // Next exercise in this group that is not already in the workout.
                for (int k = 0; k < queue.size(); k++) {
                    Exercise candidate = queue.get(k);
                    if (usedIds.add(candidate.getExerciseId())) {
                        picked.add(candidate);
                        queue.remove(k);
                        tookAny = true;
                        break;
                    }
                }
            }
        }

        this.selectedExercises = picked;
        seedDraftsFromExercises();
        showFragment(new UserWorkoutDetailsFragment(), true);
    }

    /**
     * Called when the user saves the workout with name and per-exercise reps/weight.
     * Handles both create (new workout) and edit (update existing) modes.
     */
    public void onWorkoutSaved(String workoutName, List<Integer> setsList,
                               List<Integer> repsList, List<Double> weightList) {
        // Ignore a second tap while the first save is still running.
        if (saving) return;

        int userId = sessionManager.getUserId();
        if (userId == -1) {
            DialogUtils.showError(this, R.string.create_workout_error);
            onSaveFailed(0);
            return;
        }
        saving = true;

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        for (int i = 0; i < selectedExercises.size(); i++) {
            Exercise exercise = selectedExercises.get(i);
            WorkoutExercise we = new WorkoutExercise();
            we.setExerciseId(exercise.getExerciseId());
            we.setReps(repsList.get(i));
            // Fall back to the exercise default if the box was left empty or 0.
            int sets = i < setsList.size() ? setsList.get(i) : 0;
            we.setSets(sets > 0 ? sets : Math.max(1, exercise.getDefaultSets()));
            we.setOrderIndex(i + 1);
            if (weightList != null && i < weightList.size()) {
                we.setTargetWeight(weightList.get(i));
            }
            workoutExercises.add(we);
        }

        if (isEditMode) {
            // If the workout being edited is an admin default, or is not owned by
            // this user, we must NOT change the shared workout. Instead save a
            // personal copy owned by this user and (when editing from My Plan)
            // repoint the scheduled workout at that copy.
            boolean ownedByUser = editWorkoutOwnerId != null && editWorkoutOwnerId == userId;
            if (editWorkoutIsDefault || !ownedByUser) {
                saveAsCopy(userId, workoutName, workoutExercises);
                return;
            }

            // Update the user's own workout in place.
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
                        SnackbarUtils.show(CreateWorkoutActivity.this, R.string.edit_workout_success);
                        findViewById(android.R.id.content).postDelayed(() -> finish(), 1500);
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> onSaveFailed(R.string.edit_workout_error));
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
                        SnackbarUtils.show(CreateWorkoutActivity.this, R.string.create_workout_success);
                        findViewById(android.R.id.content).postDelayed(() -> finish(), 1500);
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> onSaveFailed(R.string.create_workout_error));
                }
            });
        }
    }

    /**
     * Saves the edited workout as a NEW workout owned by the user, leaving the
     * original (a default or another user's workout) untouched. When this edit
     * came from My Plan (a scheduled workout id was supplied), the scheduled
     * workout is then repointed at the new copy so the user's plan shows their
     * edited version while the shared default is unchanged.
     */
    private void saveAsCopy(int userId, String workoutName, List<WorkoutExercise> workoutExercises) {
        Workout copy = new Workout();
        copy.setUserId(userId);
        copy.setName(workoutName);
        copy.setDefault(false);
        copy.setOrderIndex(0);

        workoutRepository.createWorkout(copy, workoutExercises, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long newWorkoutId) {
                if (newWorkoutId == null || newWorkoutId <= 0) {
                    runOnUiThread(() -> onSaveFailed(R.string.edit_workout_error));
                    return;
                }

                // No scheduled row to repoint (edited straight from the library):
                // the personal copy has been created, we're done.
                if (editScheduledWorkoutId == -1) {
                    runOnUiThread(() -> {
                        SnackbarUtils.show(CreateWorkoutActivity.this, R.string.edit_workout_success);
                        findViewById(android.R.id.content).postDelayed(() -> finish(), 1500);
                    });
                    return;
                }

                // Point this user's scheduled workout at the new copy.
                scheduleRepository.updateScheduledWorkoutWorkoutId(editScheduledWorkoutId, userId,
                        newWorkoutId.intValue(), new ConnectionManager.DatabaseCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer rows) {
                                runOnUiThread(() -> {
                                    SnackbarUtils.show(CreateWorkoutActivity.this, R.string.edit_workout_success);
                                    findViewById(android.R.id.content).postDelayed(() -> finish(), 1500);
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> onSaveFailed(R.string.edit_workout_error));
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> onSaveFailed(R.string.edit_workout_error));
            }
        });
    }

    /**
     * A save failed: let the user try again (Save button back on) and, when a
     * message is given, tell them what went wrong.
     */
    private void onSaveFailed(int messageRes) {
        saving = false;
        androidx.fragment.app.Fragment current =
                getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (current instanceof UserWorkoutDetailsFragment) {
            ((UserWorkoutDetailsFragment) current).setSaving(false);
        }
        if (messageRes != 0) {
            DialogUtils.showError(this, messageRes);
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

    public List<Integer> getDraftSets() {
        return draftSets;
    }

    /** How many exercises are already in the workout when adding more (else 0). */
    public int getExistingExerciseCount() {
        return addingExercises ? selectedExercises.size() : 0;
    }

    public List<Integer> getDraftReps() {
        return draftReps;
    }

    public List<Double> getDraftWeights() {
        return draftWeights;
    }
}
