package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;
import com.jve.repsup.utils.AgePickerView;

/**
 * Step 3: Age selection via vertical number scroll picker.
 * Numbers are displayed vertically with the selected age centered, larger, and highlighted.
 */
public class AssessmentAgeFragment extends Fragment {

    private AgePickerView agePickerView;

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

        agePickerView = view.findViewById(R.id.agePickerView);

        // Restore saved age
        int savedAge = getAssessmentActivity().getAssessmentData().getAge();
        agePickerView.setSelectedAge(savedAge);

        agePickerView.setOnAgeSelectedListener(age ->
                getAssessmentActivity().getAssessmentData().setAge(age));
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
