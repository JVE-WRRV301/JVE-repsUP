package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.AdminCreateWorkoutActivity;
import com.jve.repsup.adapters.AdminExerciseSetsRepsAdapter;
import com.jve.repsup.utils.SnackbarUtils;

/**
 * Review step of the admin create/edit flow. Lists every added exercise with
 * editable sets/reps and a remove control, a workout name field, a "Back / Add
 * more" action (returns to the muscle group list), and a "Finish" action that
 * validates and saves the workout.
 */
public class AdminWorkoutReviewFragment extends Fragment {

    private EditText etWorkoutName;
    private AdminExerciseSetsRepsAdapter adapter;
    private Button btnFinish;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_workout_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdminCreateWorkoutActivity activity = (AdminCreateWorkoutActivity) requireActivity();

        etWorkoutName = view.findViewById(R.id.etWorkoutName);
        if (activity.isEditMode() && activity.getEditWorkoutName() != null) {
            etWorkoutName.setText(activity.getEditWorkoutName());
        }

        RecyclerView rv = view.findViewById(R.id.rvReviewExercises);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // The adapter operates directly on the activity's live selection list, so
        // removeAt mutates that single source of truth — no separate activity call.
        adapter = new AdminExerciseSetsRepsAdapter(activity.getSelectedExercises(),
                position -> adapter.removeAt(position));
        rv.setAdapter(adapter);

        Button btnAddMore = view.findViewById(R.id.btnAddMore);
        btnAddMore.setOnClickListener(v -> activity.addMoreExercises());

        btnFinish = view.findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(v -> {
            String name = etWorkoutName.getText() != null
                    ? etWorkoutName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                SnackbarUtils.show(requireView(), R.string.admin_workout_name_required);
                return;
            }
            if (activity.getSelectedCount() == 0) {
                SnackbarUtils.show(requireView(), R.string.admin_add_at_least_one);
                return;
            }
            btnFinish.setEnabled(false);
            activity.saveWorkout(name);
        });
    }

    /** Re-enables Finish after a failed save so the admin can try again. */
    public void setSaving(boolean saving) {
        if (btnFinish != null) btnFinish.setEnabled(!saving);
    }
}
