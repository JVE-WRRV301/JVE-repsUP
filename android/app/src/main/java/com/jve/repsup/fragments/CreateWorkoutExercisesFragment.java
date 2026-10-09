package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.activities.AdminCreateWorkoutActivity;
import com.jve.repsup.adapters.AdminExercisePickAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SnackbarUtils;

import java.util.List;

/**
 * Exercise selection screen for a single muscle group.
 *
 * Tapping an exercise opens a dialog to set that exercise's sets/reps (pre-filled
 * with its defaults); confirming adds it to the running workout selection with
 * those per-exercise values. A top bar shows "X / 8 added" and a persistent
 * "Review / Create" button opens the review page.
 */
public class CreateWorkoutExercisesFragment extends Fragment {

    private static final String ARG_MUSCLE_GROUP = "muscle_group";

    private AdminExercisePickAdapter adapter;
    private WorkoutRepository workoutRepository;
    private TextView tvAddedCount;

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

        AdminCreateWorkoutActivity activity = (AdminCreateWorkoutActivity) requireActivity();

        TextView tvTitle = view.findViewById(R.id.tvExercisesTitle);
        tvTitle.setText(getString(R.string.admin_select_exercises));

        tvAddedCount = view.findViewById(R.id.tvAddedCount);
        updateAddedCount();

        RecyclerView recyclerView = view.findViewById(R.id.rvExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Tapping an exercise opens the sets/reps dialog to add it to the workout.
        adapter = new AdminExercisePickAdapter(exercise -> promptSetsReps(exercise));
        recyclerView.setAdapter(adapter);

        Button btnReview = view.findViewById(R.id.btnReview);
        btnReview.setOnClickListener(v -> {
            if (activity.getSelectedCount() == 0) {
                SnackbarUtils.show(requireView(), R.string.admin_add_at_least_one);
                return;
            }
            activity.openReview();
        });

        loadExercises(muscleGroup);
    }

    /**
     * Shows a dialog pre-filled with the exercise's default sets/reps, then adds
     * the exercise with the admin's chosen values to the running selection.
     */
    private void promptSetsReps(Exercise exercise) {
        AdminCreateWorkoutActivity activity = (AdminCreateWorkoutActivity) requireActivity();

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_sets_reps, null);
        EditText etSets = dialogView.findViewById(R.id.etSets);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        tvDialogTitle.setText(exercise.getName());
        int defaultSets = exercise.getDefaultSets() > 0 ? exercise.getDefaultSets() : 3;
        int defaultReps = exercise.getDefaultReps() > 0 ? exercise.getDefaultReps() : 10;
        etSets.setText(String.valueOf(defaultSets));
        etReps.setText(String.valueOf(defaultReps));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.admin_add_exercise, (d, which) -> {
                    int sets = parseOrDefault(etSets.getText().toString(), defaultSets);
                    int reps = parseOrDefault(etReps.getText().toString(), defaultReps);
                    boolean added = activity.addExerciseWithSetsReps(exercise, sets, reps);
                    if (!added) {
                        SnackbarUtils.show(requireView(), R.string.create_workout_max_exercises_reached);
                    } else {
                        updateAddedCount();
                        SnackbarUtils.show(requireView(), R.string.admin_exercise_added_toast);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        // Let the dialog draw edge-to-edge so IME insets are dispatched, then
        // pad the content with the keyboard inset so the fields stay visible.
        if (dialog.getWindow() != null) {
            InsetsHelper.enableEdgeToEdge(dialog.getWindow());
        }
        InsetsHelper.applyImeBottomPadding(dialogView.findViewById(R.id.dialogSetsRepsRoot));

        dialog.show();
    }

    private int parseOrDefault(String text, int fallback) {
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
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
                            DialogUtils.showError(requireContext(), R.string.admin_load_exercises_error));
                }
            }
        });
    }

    private void updateAddedCount() {
        int added = ((AdminCreateWorkoutActivity) requireActivity()).getSelectedCount();
        tvAddedCount.setText(getString(R.string.admin_exercises_added, added,
                AdminCreateWorkoutActivity.MAX_EXERCISES));
    }
}
