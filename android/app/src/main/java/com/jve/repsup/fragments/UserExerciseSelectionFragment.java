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

import com.jve.repsup.R;
import com.jve.repsup.activities.CreateWorkoutActivity;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SnackbarUtils;
import com.jve.repsup.utils.WorkoutValidator;
import com.jve.repsup.views.ExerciseBrowserView;

import java.util.List;

/**
 * Exercise picker for the Create Workout flow: search box, a tab per muscle
 * group and a list to tick from. What is ticked is kept while switching groups
 * or searching, and the running "x / max selected" count covers every group.
 * When opened from the details screen to add more exercises, the exercises
 * already in the workout start ticked.
 */
public class UserExerciseSelectionFragment extends Fragment {

    private static final String ARG_ADDING = "adding";

    private TextView tvSelectionCount;

    public UserExerciseSelectionFragment() {
        // Required empty public constructor
    }

    /** @param adding true when adding to a workout that already has exercises */
    public static UserExerciseSelectionFragment newInstance(boolean adding) {
        UserExerciseSelectionFragment fragment = new UserExerciseSelectionFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ADDING, adding);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_exercise_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CreateWorkoutActivity activity = (CreateWorkoutActivity) getActivity();
        if (activity == null) return;

        // Keep the Continue button above the keyboard while searching.
        InsetsHelper.applyImeBottomPadding(view);

        tvSelectionCount = view.findViewById(R.id.tvSelectionCount);
        Button btnConfirm = view.findViewById(R.id.btnConfirmExercises);
        ExerciseBrowserView browser = view.findViewById(R.id.exerciseBrowser);

        // Exercises already in the workout start ticked (also when going Back to here).
        browser.configure(true, WorkoutValidator.MAX_EXERCISES, activity.getSelectedExercises());
        browser.setSelectionListener(new ExerciseBrowserView.SelectionListener() {
            @Override
            public void onSelectionChanged(int count) {
                updateCountText(count);
            }

            @Override
            public void onLimitReached() {
                SnackbarUtils.show(requireView(), R.string.create_workout_max_exercises_reached);
            }
        });
        updateCountText(browser.getSelectedCount());
        browser.loadIfNeeded();

        boolean adding = getArguments() != null && getArguments().getBoolean(ARG_ADDING, false);
        btnConfirm.setOnClickListener(v -> {
            List<Exercise> selected = browser.getSelectedExercises();
            if (selected.isEmpty()) {
                SnackbarUtils.show(v, R.string.create_workout_select_at_least_one);
                return;
            }
            activity.onExercisesSelected(selected, adding);
        });
    }

    private void updateCountText(int count) {
        if (tvSelectionCount != null) {
            tvSelectionCount.setText(getString(R.string.create_workout_exercises_selected,
                    count, WorkoutValidator.MAX_EXERCISES));
        }
    }
}
