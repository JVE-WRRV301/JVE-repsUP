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

/**
 * Step 7: Workout level selection (Never, Beginner, Intermediate, Advanced).
 */
public class AssessmentWorkoutLevelFragment extends Fragment {

    private Button btnNever, btnBeginner, btnIntermediate, btnAdvanced;
    private Button[] allButtons;
    private String[] levelValues = {"never", "beginner", "intermediate", "advanced"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_workout_level, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnNever = view.findViewById(R.id.btnNever);
        btnBeginner = view.findViewById(R.id.btnBeginner);
        btnIntermediate = view.findViewById(R.id.btnIntermediate);
        btnAdvanced = view.findViewById(R.id.btnAdvanced);

        allButtons = new Button[]{btnNever, btnBeginner, btnIntermediate, btnAdvanced};

        for (int i = 0; i < allButtons.length; i++) {
            final int index = i;
            allButtons[i].setOnClickListener(v -> selectLevel(index));
        }

        // Restore saved selection
        String savedLevel = getAssessmentActivity().getAssessmentData().getWorkoutLevel();
        if (savedLevel != null) {
            for (int i = 0; i < levelValues.length; i++) {
                if (levelValues[i].equals(savedLevel)) {
                    highlightSelected(i);
                    break;
                }
            }
        }
    }

    private void selectLevel(int index) {
        getAssessmentActivity().getAssessmentData().setWorkoutLevel(levelValues[index]);
        highlightSelected(index);
        getAssessmentActivity().notifyDataChanged();
    }

    private void highlightSelected(int selectedIndex) {
        for (int i = 0; i < allButtons.length; i++) {
            if (i == selectedIndex) {
                allButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
                allButtons[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            } else {
                allButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
                allButtons[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
        }
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
