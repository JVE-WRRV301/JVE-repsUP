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
 * Step 8: Workout schedule — how many days per week (button-based selection 1-7).
 */
public class AssessmentScheduleFragment extends Fragment {

    private Button[] allButtons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allButtons = new Button[]{
                view.findViewById(R.id.btnSchedule1),
                view.findViewById(R.id.btnSchedule2),
                view.findViewById(R.id.btnSchedule3),
                view.findViewById(R.id.btnSchedule4),
                view.findViewById(R.id.btnSchedule5),
                view.findViewById(R.id.btnSchedule6),
                view.findViewById(R.id.btnSchedule7)
        };

        for (int i = 0; i < allButtons.length; i++) {
            final int days = i + 1;
            allButtons[i].setOnClickListener(v -> selectSchedule(days));
        }

        // Restore saved selection
        int savedDays = getAssessmentActivity().getAssessmentData().getWorkoutsPerWeek();
        highlightSelected(savedDays);
    }

    private void selectSchedule(int days) {
        getAssessmentActivity().getAssessmentData().setWorkoutsPerWeek(days);
        highlightSelected(days);
        getAssessmentActivity().notifyDataChanged();
    }

    private void highlightSelected(int selectedDays) {
        for (int i = 0; i < allButtons.length; i++) {
            if (i + 1 == selectedDays) {
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
