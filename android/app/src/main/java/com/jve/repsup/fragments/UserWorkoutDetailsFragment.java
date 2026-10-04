package com.jve.repsup.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

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
import com.jve.repsup.utils.SnackbarUtils;

import java.util.List;

/**
 * Fragment for setting workout name and per-exercise reps/weight.
 * Final step before saving the workout.
 * Supports edit mode: pre-populates existing workout data.
 */
public class UserWorkoutDetailsFragment extends Fragment {

    private TextInputEditText etWorkoutName;
    private ExerciseDetailAdapter adapter;
    private RecyclerView rvExerciseDetails;
    private android.widget.TextView tvNoExercises;

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
        rvExerciseDetails = view.findViewById(R.id.rvExerciseDetails);
        tvNoExercises = view.findViewById(R.id.tvNoExercises);
        Button btnSave = view.findViewById(R.id.btnSaveWorkout);
        Button btnAddExercises = view.findViewById(R.id.btnAddExercises);

        CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
        if (activity == null) return;

        List<Exercise> exercises = activity.getSelectedExercises();

        if (activity.isEditMode()) {
            etWorkoutName.setText(activity.getEditWorkoutName());
            btnSave.setText(R.string.edit_workout_save_button);
        }

        adapter = new ExerciseDetailAdapter(exercises,
                activity.getDraftReps(), activity.getDraftWeights(),
                position -> {
                    // removeAt mutates the shared selectedExercises list plus the
                    // adapter's reps/weight, so re-sync the activity draft afterwards.
                    adapter.removeAt(position);
                    activity.saveDraft(adapter.getRepsList(), adapter.getWeightList());
                    updateEmptyState();
                });

        rvExerciseDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExerciseDetails.setAdapter(adapter);
        updateEmptyState();

        btnAddExercises.setOnClickListener(v -> {
            // Save current input so it survives the add-exercises detour.
            activity.saveDraft(adapter.getRepsList(), adapter.getWeightList());
            activity.onAddExercisesRequested();
        });

        btnSave.setOnClickListener(v -> {
            // Dismiss the soft keyboard first so its key-preview bubbles don't
            // overlap the validation toast at the bottom of the screen.
            hideKeyboard(v);

            String workoutName = etWorkoutName.getText() != null ?
                    etWorkoutName.getText().toString().trim() : "";

            if (workoutName.isEmpty()) {
                SnackbarUtils.show(v, R.string.create_workout_name_required);
                return;
            }

            if (activity.getSelectedExercises().isEmpty()) {
                SnackbarUtils.show(v, R.string.create_workout_select_at_least_one);
                return;
            }

            List<Integer> repsList = adapter.getRepsList();
            List<Double> weightList = adapter.getWeightList();

            activity.onWorkoutSaved(workoutName, repsList, weightList);
        });
    }

    /**
     * Hides the soft keyboard, if showing, using the given view's window token.
     */
    private void hideKeyboard(View view) {
        if (getContext() == null || view == null) return;
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Shows the empty-state message and hides the list when there are no exercises.
     */
    private void updateEmptyState() {
        if (adapter == null || tvNoExercises == null || rvExerciseDetails == null) return;
        boolean empty = adapter.getItemCount() == 0;
        tvNoExercises.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvExerciseDetails.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
