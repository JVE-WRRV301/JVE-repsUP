package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.AdminCreateWorkoutActivity;
import com.jve.repsup.adapters.MuscleGroupAdapter;
import com.jve.repsup.utils.SnackbarUtils;

/**
 * First step of the admin create-workout flow: a grid of the 10 muscle groups.
 * (There is no category layer.) Shows the running "X / 8 added" count at the top
 * and a persistent "Review / Create" button at the bottom.
 */
public class CreateWorkoutMuscleGroupFragment extends Fragment {

    private TextView tvAddedCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_muscle_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdminCreateWorkoutActivity activity = (AdminCreateWorkoutActivity) requireActivity();

        TextView tvTitle = view.findViewById(R.id.tvMuscleGroupTitle);
        tvTitle.setText(getString(R.string.admin_select_muscle_group));

        tvAddedCount = view.findViewById(R.id.tvAddedCount);
        updateAddedCount();

        RecyclerView recyclerView = view.findViewById(R.id.rvMuscleGroups);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        MuscleGroupAdapter adapter = new MuscleGroupAdapter(
                AdminCreateWorkoutActivity.MUSCLE_GROUPS,
                muscleGroup -> activity.onMuscleGroupSelected(muscleGroup));
        recyclerView.setAdapter(adapter);

        Button btnReview = view.findViewById(R.id.btnReview);
        btnReview.setOnClickListener(v -> {
            if (activity.getSelectedCount() == 0) {
                SnackbarUtils.show(requireView(), R.string.admin_add_at_least_one);
                return;
            }
            activity.openReview();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAddedCount();
    }

    private void updateAddedCount() {
        int added = ((AdminCreateWorkoutActivity) requireActivity()).getSelectedCount();
        tvAddedCount.setText(getString(R.string.admin_exercises_added, added,
                AdminCreateWorkoutActivity.MAX_EXERCISES));
    }
}
