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
import com.jve.repsup.activities.AdminCreateWorkoutActivity;
import com.jve.repsup.adapters.ExerciseSelectionAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.repositories.WorkoutRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 3: Exercise selection screen.
 * Shows exercises filtered by muscle group. Admin can select up to 8 exercises.
 * Selected exercises are highlighted in blue.
 */
public class CreateWorkoutExercisesFragment extends Fragment {

    private static final String ARG_MUSCLE_GROUP = "muscle_group";
    private static final int MAX_EXERCISES = 8;

    private ExerciseSelectionAdapter adapter;
    private WorkoutRepository workoutRepository;
    private TextView tvSelectionCount;

    public static CreateWorkoutExercisesFragment newInstance(String muscleGroup) {
        CreateWorkoutExercisesFragment fragment = new CreateWorkoutExercisesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MUSCLE_GROUP, muscleGroup);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workoutRepository = new WorkoutRepository();
        String muscleGroup = getArguments() != null ? getArguments().getString(ARG_MUSCLE_GROUP) : "";

        TextView tvTitle = view.findViewById(R.id.tvExercisesTitle);
        tvTitle.setText(getString(R.string.admin_select_exercises));

        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        updateSelectionCount(0);

        RecyclerView recyclerView = view.findViewById(R.id.rvExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ExerciseSelectionAdapter(MAX_EXERCISES, count -> updateSelectionCount(count));
        recyclerView.setAdapter(adapter);

        Button btnConfirm = view.findViewById(R.id.btnConfirmExercises);
        btnConfirm.setOnClickListener(v -> {
            List<Exercise> selected = adapter.getSelectedExercises();
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.admin_select_at_least_one), Toast.LENGTH_SHORT).show();
                return;
            }
            ((AdminCreateWorkoutActivity) requireActivity()).onExercisesConfirmed(selected);
        });

        loadExercises(muscleGroup);
    }

    private void loadExercises(String muscleGroup) {
        workoutRepository.getExercisesByMuscleGroup(muscleGroup, new ConnectionManager.DatabaseCallback<List<Exercise>>() {
            @Override
            public void onSuccess(List<Exercise> exercises) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.setExercises(exercises));
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), getString(R.string.admin_load_exercises_error), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void updateSelectionCount(int count) {
        tvSelectionCount.setText(getString(R.string.admin_exercises_selected, count, MAX_EXERCISES));
    }
}
