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
 * Step 3: Age selection via SeekBar.
 */
public class AssessmentAgeFragment extends Fragment {

    private TextView tvAgeValue;
    private SeekBar sbAge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_age, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAgeValue = view.findViewById(R.id.tvAgeValue);
        sbAge = view.findViewById(R.id.sbAge);

        // Restore saved age
        int savedAge = getAssessmentActivity().getAssessmentData().getAge();
        tvAgeValue.setText(String.valueOf(savedAge));
        sbAge.setProgress(savedAge - 14); // min is 14

        sbAge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int age = progress + 14;
                tvAgeValue.setText(String.valueOf(age));
                getAssessmentActivity().getAssessmentData().setAge(age);
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
