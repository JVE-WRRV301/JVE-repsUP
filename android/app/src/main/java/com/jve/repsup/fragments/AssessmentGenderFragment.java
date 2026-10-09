package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.AssessmentData;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 2: Gender selection with large circular buttons (Male / Female).
 */
public class AssessmentGenderFragment extends Fragment {

    private LinearLayout llMale;
    private LinearLayout llFemale;
    private FrameLayout flMaleCircle;
    private FrameLayout flFemaleCircle;
    private TextView tvMaleLabel;
    private TextView tvFemaleLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_gender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llMale = view.findViewById(R.id.llMale);
        llFemale = view.findViewById(R.id.llFemale);
        flMaleCircle = view.findViewById(R.id.flMaleCircle);
        flFemaleCircle = view.findViewById(R.id.flFemaleCircle);
        tvMaleLabel = view.findViewById(R.id.tvMaleLabel);
        tvFemaleLabel = view.findViewById(R.id.tvFemaleLabel);

        // Restore selection
        String savedGender = getAssessmentActivity().getAssessmentData().getGender();
        if (savedGender != null) {
            updateSelection(savedGender.equals("Male"));
        }

        llMale.setOnClickListener(v -> {
            selectGender("Male");
            updateSelection(true);
        });

        llFemale.setOnClickListener(v -> {
            selectGender("Female");
            updateSelection(false);
        });
    }

    /**
     * Stores the chosen gender. When the gender actually changes, the previously
     * chosen body type is cleared: the body types differ by gender (male has
     * "built", female has "toned"), so a stale selection would no longer be valid.
     * The body-type page rebuilds itself for the current gender in onResume.
     */
    private void selectGender(String gender) {
        AssessmentData data = getAssessmentActivity().getAssessmentData();
        String previous = data.getGender();
        if (previous == null || !previous.equals(gender)) {
            data.setBodyType(null);
        }
        data.setGender(gender);
    }

    private void updateSelection(boolean isMale) {
        if (isMale) {
            flMaleCircle.setBackgroundResource(R.drawable.bg_gender_circle_selected);
            tvMaleLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_blue));
            flFemaleCircle.setBackgroundResource(R.drawable.bg_gender_circle_unselected);
            tvFemaleLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            flFemaleCircle.setBackgroundResource(R.drawable.bg_gender_circle_selected);
            tvFemaleLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_blue));
            flMaleCircle.setBackgroundResource(R.drawable.bg_gender_circle_unselected);
            tvMaleLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
        getAssessmentActivity().notifyDataChanged();
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
