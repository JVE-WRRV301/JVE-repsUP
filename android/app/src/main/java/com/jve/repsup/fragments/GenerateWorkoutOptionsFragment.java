package com.jve.repsup.fragments;

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

import com.google.android.material.chip.Chip;
import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.adapters.SelectableMuscleGroupAdapter;
import com.jve.repsup.utils.SnackbarUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment for the Generate Workout flow.
 * User selects a difficulty and one or more muscle groups, then taps Generate.
 */
public class GenerateWorkoutOptionsFragment extends Fragment {

    private static final List<String> MUSCLE_GROUPS = Arrays.asList(
            "Chest", "Back", "Shoulders", "Biceps", "Triceps",
            "Legs", "Glutes", "Abs", "Calves", "Forearms"
    );

    private Chip chipEasy;
    private Chip chipMedium;
    private Chip chipHard;
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

        chipEasy = view.findViewById(R.id.chipEasy);
        chipMedium = view.findViewById(R.id.chipMedium);
        chipHard = view.findViewById(R.id.chipHard);
        setupSingleSelectDifficulty();

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
     * Enforces single-selection across the three difficulty chips: checking one
     * unchecks the others. (We manage this manually since the chips are laid out
     * in a weighted LinearLayout rather than a ChipGroup.)
     */
    private void setupSingleSelectDifficulty() {
        Chip[] chips = {chipEasy, chipMedium, chipHard};
        for (Chip chip : chips) {
            chip.setOnClickListener(v -> {
                for (Chip other : chips) {
                    other.setChecked(other == v);
                }
            });
        }
    }

    private String getSelectedDifficulty() {
        if (chipEasy.isChecked()) {
            return "Easy";
        } else if (chipMedium.isChecked()) {
            return "Medium";
        } else if (chipHard.isChecked()) {
            return "Hard";
        }
        return null;
    }
}
