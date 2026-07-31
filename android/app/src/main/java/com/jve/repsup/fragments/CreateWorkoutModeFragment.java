package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;

/**
 * Fragment presenting the mode toggle: "Create Workout" or "Generate Workout".
 */
public class CreateWorkoutModeFragment extends Fragment {

    public CreateWorkoutModeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCreate = view.findViewById(R.id.btnCreateMode);
        Button btnGenerate = view.findViewById(R.id.btnGenerateMode);

        btnCreate.setOnClickListener(v -> {
            CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
            if (activity != null) {
                activity.onModeSelected(CreateWorkoutActivity.MODE_CREATE);
            }
        });

        btnGenerate.setOnClickListener(v -> {
            CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
            if (activity != null) {
                activity.onModeSelected(CreateWorkoutActivity.MODE_GENERATE);
            }
        });
    }
}
