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

import com.google.android.material.textfield.TextInputEditText;
import com.jve.repsup.R;
import com.jve.repsup.activities.AdminCreateWorkoutActivity;

/**
 * Step 4: Naming screen.
 * Admin enters the workout name and sets the number of reps/sets per exercise.
 */
public class CreateWorkoutNamingFragment extends Fragment {

    private TextInputEditText etWorkoutName;
    private TextInputEditText etSets;
    private TextInputEditText etReps;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_naming, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etWorkoutName = view.findViewById(R.id.etWorkoutName);
        etSets = view.findViewById(R.id.etSets);
        etReps = view.findViewById(R.id.etReps);

        Button btnSave = view.findViewById(R.id.btnSaveWorkout);
        btnSave.setOnClickListener(v -> saveWorkout());
    }

    private void saveWorkout() {
        String name = etWorkoutName.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();

        if (name.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.admin_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int sets;
        int reps;
        try {
            sets = Integer.parseInt(setsStr);
            reps = Integer.parseInt(repsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.admin_invalid_numbers), Toast.LENGTH_SHORT).show();
            return;
        }

        if (sets <= 0 || reps <= 0) {
            Toast.makeText(requireContext(), getString(R.string.admin_invalid_numbers), Toast.LENGTH_SHORT).show();
            return;
        }

        ((AdminCreateWorkoutActivity) requireActivity()).onWorkoutSaved(name, sets, reps);
    }
}
