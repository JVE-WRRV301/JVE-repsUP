package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 8: Workout schedule — how many days per week.
 */
public class AssessmentScheduleFragment extends Fragment {

    private TextView tvScheduleValue;
    private SeekBar sbSchedule;

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

        tvScheduleValue = view.findViewById(R.id.tvScheduleValue);
        sbSchedule = view.findViewById(R.id.sbSchedule);

        // Restore saved value
        int savedDays = getAssessmentActivity().getAssessmentData().getWorkoutsPerWeek();
        tvScheduleValue.setText(String.valueOf(savedDays));
        sbSchedule.setProgress(savedDays - 1); // min is 1

        sbSchedule.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int days = progress + 1;
                tvScheduleValue.setText(String.valueOf(days));
                getAssessmentActivity().getAssessmentData().setWorkoutsPerWeek(days);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
