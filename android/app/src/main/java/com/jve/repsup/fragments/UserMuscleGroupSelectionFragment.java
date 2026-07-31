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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.adapters.SelectableMuscleGroupAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment for selecting muscle groups in the Create Workout flow.
 * Allows multi-selection of muscle groups for exercise filtering.
 */
public class UserMuscleGroupSelectionFragment extends Fragment {

    private static final List<String> MUSCLE_GROUPS = Arrays.asList(
            "Chest", "Back", "Shoulders", "Biceps", "Triceps",
            "Legs", "Glutes", "Abs", "Calves", "Forearms"
    );

    private SelectableMuscleGroupAdapter adapter;

    public UserMuscleGroupSelectionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_muscle_group_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvMuscleGroups = view.findViewById(R.id.rvMuscleGroups);
        Button btnConfirm = view.findViewById(R.id.btnConfirmMuscleGroups);

        adapter = new SelectableMuscleGroupAdapter(MUSCLE_GROUPS);
        rvMuscleGroups.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvMuscleGroups.setAdapter(adapter);

        btnConfirm.setOnClickListener(v -> {
            List<String> selected = adapter.getSelectedMuscleGroups();
            if (selected.isEmpty()) {
                Toast.makeText(getContext(),
                        R.string.create_workout_select_muscle_group_first, Toast.LENGTH_SHORT).show();
                return;
            }

            CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
            if (activity != null) {
                activity.onMuscleGroupsSelected(selected);
            }
        });
    }
}
