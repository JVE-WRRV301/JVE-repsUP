package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.adapters.ExerciseSelectionAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.repositories.WorkoutRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for selecting exercises in the Create Workout flow.
 * Loads exercises based on selected muscle groups and enforces max 8 limit.
 */
public class UserExerciseSelectionFragment extends Fragment {

    public static final int MAX_EXERCISES = 8;

    private ExerciseSelectionAdapter adapter;
    private TextView tvSelectionCount;
    private WorkoutRepository workoutRepository;

    public UserExerciseSelectionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_exercise_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workoutRepository = new WorkoutRepository();

        RecyclerView rvExercises = view.findViewById(R.id.rvExercises);
        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        Button btnConfirm = view.findViewById(R.id.btnConfirmExercises);

        updateCountText(0);

        adapter = new ExerciseSelectionAdapter(MAX_EXERCISES, count -> {
            updateCountText(count);
            if (count >= MAX_EXERCISES) {
                Toast.makeText(getContext(),
                        R.string.create_workout_max_exercises_reached, Toast.LENGTH_SHORT).show();
            }
        });

        rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExercises.setAdapter(adapter);

        // Load exercises for the selected muscle groups
        loadExercises();

        btnConfirm.setOnClickListener(v -> {
            List<Exercise> selected = adapter.getSelectedExercises();
            if (selected.isEmpty()) {
                Toast.makeText(getContext(),
                        R.string.create_workout_select_at_least_one, Toast.LENGTH_SHORT).show();
                return;
            }

            CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
            if (activity != null) {
                activity.onExercisesSelected(selected);
            }
        });
    }

    private void updateCountText(int count) {
        if (tvSelectionCount != null) {
            tvSelectionCount.setText(getString(R.string.create_workout_exercises_selected, count, MAX_EXERCISES));
        }
    }

    private void loadExercises() {
        CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
        if (activity == null) return;

        List<String> muscleGroups = activity.getSelectedMuscleGroups();
        if (muscleGroups == null || muscleGroups.isEmpty()) return;

        List<Exercise> allExercises = new ArrayList<>();
        final int[] loadedCount = {0};
        final int totalGroups = muscleGroups.size();

        for (String group : muscleGroups) {
            workoutRepository.getExercisesByMuscleGroup(group, new ConnectionManager.DatabaseCallback<List<Exercise>>() {
                @Override
                public void onSuccess(List<Exercise> result) {
                    synchronized (allExercises) {
                        if (result != null) {
                            allExercises.addAll(result);
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] >= totalGroups) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> adapter.setExercises(allExercises));
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    synchronized (allExercises) {
                        loadedCount[0]++;
                        if (loadedCount[0] >= totalGroups) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                if (allExercises.isEmpty()) {
                                    DialogUtils.showError(getContext(), R.string.create_workout_load_error);
                                } else {
                                    adapter.setExercises(allExercises);
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
