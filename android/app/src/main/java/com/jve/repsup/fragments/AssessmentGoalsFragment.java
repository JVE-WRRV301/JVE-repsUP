package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 9: Fitness goals multi-select.
 */
public class AssessmentGoalsFragment extends Fragment {

    private Button btnLoseWeight, btnStayConsistent, btnBuildMuscle, btnGetFitter, btnImproveEndurance;
    private Button[] allButtons;
    private String[] goalValues = {"Lose weight", "Stay Consistent", "Build muscle", "Get fitter", "Improve endurance"};
    private boolean[] selected = new boolean[5];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLoseWeight = view.findViewById(R.id.btnGoalLoseWeight);
        btnStayConsistent = view.findViewById(R.id.btnGoalStayConsistent);
        btnBuildMuscle = view.findViewById(R.id.btnGoalBuildMuscle);
        btnGetFitter = view.findViewById(R.id.btnGoalGetFitter);
        btnImproveEndurance = view.findViewById(R.id.btnGoalImproveEndurance);

        allButtons = new Button[]{btnLoseWeight, btnStayConsistent, btnBuildMuscle, btnGetFitter, btnImproveEndurance};

        // Restore saved selections
        List<String> savedGoals = getAssessmentActivity().getAssessmentData().getFitnessGoals();
        for (int i = 0; i < goalValues.length; i++) {
            if (savedGoals.contains(goalValues[i])) {
                selected[i] = true;
            }
        }
        updateAllButtons();

        for (int i = 0; i < allButtons.length; i++) {
            final int index = i;
            allButtons[i].setOnClickListener(v -> toggleGoal(index));
        }
    }

    private void toggleGoal(int index) {
        selected[index] = !selected[index];
        updateAllButtons();
        saveGoals();
    }

    private void updateAllButtons() {
        for (int i = 0; i < allButtons.length; i++) {
            if (selected[i]) {
                allButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
                allButtons[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            } else {
                allButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
                allButtons[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
        }
    }

    private void saveGoals() {
        List<String> goals = new ArrayList<>();
        for (int i = 0; i < goalValues.length; i++) {
            if (selected[i]) {
                goals.add(goalValues[i]);
            }
        }
        getAssessmentActivity().getAssessmentData().setFitnessGoals(goals);
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
