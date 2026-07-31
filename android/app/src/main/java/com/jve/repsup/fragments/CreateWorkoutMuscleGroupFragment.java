package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.AdminCreateWorkoutActivity;
import com.jve.repsup.adapters.MuscleGroupAdapter;

import java.util.List;

/**
 * Step 2: Muscle group grid screen.
 * Shows muscle groups filtered by the selected category.
 */
public class CreateWorkoutMuscleGroupFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";

    public static CreateWorkoutMuscleGroupFragment newInstance(String category) {
        CreateWorkoutMuscleGroupFragment fragment = new CreateWorkoutMuscleGroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_muscle_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String category = getArguments() != null ? getArguments().getString(ARG_CATEGORY) : "";

        TextView tvTitle = view.findViewById(R.id.tvMuscleGroupTitle);
        tvTitle.setText(getString(R.string.admin_select_muscle_group));

        RecyclerView recyclerView = view.findViewById(R.id.rvMuscleGroups);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        List<String> muscleGroups = AdminCreateWorkoutActivity.CATEGORY_MUSCLE_GROUPS.get(category);
        if (muscleGroups != null) {
            MuscleGroupAdapter adapter = new MuscleGroupAdapter(muscleGroups, muscleGroup -> {
                ((AdminCreateWorkoutActivity) requireActivity()).onMuscleGroupSelected(muscleGroup);
            });
            recyclerView.setAdapter(adapter);
        }
    }
}
