package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.adapters.ExerciseDetailAdapter;
import com.jve.repsup.models.Exercise;

import java.util.List;

/**
 * Fragment for setting workout name and per-exercise reps/weight.
 * Final step before saving the workout.
 * Supports edit mode: pre-populates existing workout data.
 */
public class UserWorkoutDetailsFragment extends Fragment {

    private TextInputEditText etWorkoutName;
    private ExerciseDetailAdapter adapter;

    public UserWorkoutDetailsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_workout_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etWorkoutName = view.findViewById(R.id.etWorkoutName);
        RecyclerView rvExerciseDetails = view.findViewById(R.id.rvExerciseDetails);
        Button btnSave = view.findViewById(R.id.btnSaveWorkout);

        CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
        if (activity == null) return;

        List<Exercise> exercises = activity.getSelectedExercises();

        // Check if we are in edit mode and pre-populate data
        if (activity.isEditMode()) {
            etWorkoutName.setText(activity.getEditWorkoutName());
            adapter = new ExerciseDetailAdapter(exercises,
                    activity.getEditRepsList(), activity.getEditWeightList());
            btnSave.setText(R.string.edit_workout_save_button);
        } else {
            adapter = new ExerciseDetailAdapter(exercises);
        }

        rvExerciseDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExerciseDetails.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            String workoutName = etWorkoutName.getText() != null ?
                    etWorkoutName.getText().toString().trim() : "";

            if (workoutName.isEmpty()) {
                Toast.makeText(getContext(),
                        R.string.create_workout_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            List<Integer> repsList = adapter.getRepsList();
            List<Double> weightList = adapter.getWeightList();

            activity.onWorkoutSaved(workoutName, repsList, weightList);
        });
    }
}
