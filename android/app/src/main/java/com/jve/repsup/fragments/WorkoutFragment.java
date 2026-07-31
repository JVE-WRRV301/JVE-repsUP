package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;

/**
 * Placeholder WorkoutFragment displaying saved workouts and exercise library.
 * Full implementation in later tasks.
 */
public class WorkoutFragment extends Fragment {

    public WorkoutFragment() {
        // Required empty public constructor
    }

    public static WorkoutFragment newInstance() {
        return new WorkoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }
}
