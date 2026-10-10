package com.jve.repsup.fragments;

import com.jve.repsup.utils.MuscleGroups;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.adapters.SelectableMuscleGroupAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.SnackbarUtils;

import java.util.List;

/**
 * Fragment for the Generate Workout flow.
 * User selects a difficulty and one or more muscle groups, then taps Generate.
 */
public class GenerateWorkoutOptionsFragment extends Fragment {

    private static final String TAG = "GenerateWorkoutOptionsFragment";

    private static final List<String> MUSCLE_GROUPS = MuscleGroups.ALL;

    private Button btnDifficultyEasy;
    private Button btnDifficultyMedium;
    private Button btnDifficultyHard;
    /** "Easy", "Medium", "Hard", or null when nothing is selected yet. */
    private String selectedDifficulty;
    private SelectableMuscleGroupAdapter muscleGroupAdapter;

    public GenerateWorkoutOptionsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate_workout_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnDifficultyEasy = view.findViewById(R.id.btnDifficultyEasy);
        btnDifficultyMedium = view.findViewById(R.id.btnDifficultyMedium);
        btnDifficultyHard = view.findViewById(R.id.btnDifficultyHard);
        setupSingleSelectDifficulty();
        preselectDifficultyForBeginners();

        RecyclerView rvMuscleGroups = view.findViewById(R.id.rvMuscleGroups);
        Button btnGenerate = view.findViewById(R.id.btnGenerate);

        muscleGroupAdapter = new SelectableMuscleGroupAdapter(MUSCLE_GROUPS);
        rvMuscleGroups.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvMuscleGroups.setAdapter(muscleGroupAdapter);

        btnGenerate.setOnClickListener(v -> {
            // Get selected difficulty
            String difficulty = getSelectedDifficulty();
            if (difficulty == null) {
                SnackbarUtils.show(v, R.string.generate_workout_select_difficulty);
                return;
            }

            // Get selected muscle groups
            List<String> selectedGroups = muscleGroupAdapter.getSelectedMuscleGroups();
            if (selectedGroups.isEmpty()) {
                SnackbarUtils.show(v, R.string.generate_workout_select_muscle_group);
                return;
            }

            CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
            if (activity != null) {
                activity.onGenerateRequested(difficulty, selectedGroups);
            }
        });
    }

    /**
     * Single selection across the three difficulty buttons: tapping one selects
     * it and deselects the others, using the shared selectable-button look.
     */
    private void setupSingleSelectDifficulty() {
        btnDifficultyEasy.setOnClickListener(v -> selectDifficulty("Easy"));
        btnDifficultyMedium.setOnClickListener(v -> selectDifficulty("Medium"));
        btnDifficultyHard.setOnClickListener(v -> selectDifficulty("Hard"));
        updateDifficultyButtons();
    }

    /**
     * People who said in the fitness assessment that they have never worked out,
     * or are beginners, start with Easy selected. They can still change it.
     */
    private void preselectDifficultyForBeginners() {
        int userId = new SessionManager(requireContext()).getUserId();
        if (userId == -1) return;
        new UserRepository().getWorkoutLevel(userId, new ConnectionManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String level) {
                if (level == null || getActivity() == null || !isAdded()) return;
                boolean beginner = "never".equalsIgnoreCase(level) || "beginner".equalsIgnoreCase(level);
                getActivity().runOnUiThread(() -> {
                    // Do not override a choice the user has already made.
                    if (beginner && selectedDifficulty == null) {
                        selectDifficulty("Easy");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "preselectDifficultyForBeginners: database call failed", e);
                // Nothing to do: the user simply picks a difficulty themselves.
            }
        });
    }

    private void selectDifficulty(String difficulty) {
        selectedDifficulty = difficulty;
        updateDifficultyButtons();
    }

    private void updateDifficultyButtons() {
        SelectableButtonStyler.applySelected(btnDifficultyEasy, "Easy".equals(selectedDifficulty));
        SelectableButtonStyler.applySelected(btnDifficultyMedium, "Medium".equals(selectedDifficulty));
        SelectableButtonStyler.applySelected(btnDifficultyHard, "Hard".equals(selectedDifficulty));
    }

    private String getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
