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
 * Step 2: Gender selection (Male / Female).
 */
public class AssessmentGenderFragment extends Fragment {

    private Button btnMale;
    private Button btnFemale;

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

        btnMale = view.findViewById(R.id.btnMale);
        btnFemale = view.findViewById(R.id.btnFemale);

        // Restore selection
        String savedGender = getAssessmentActivity().getAssessmentData().getGender();
        if (savedGender != null) {
            updateSelection(savedGender.equals("Male"));
        }

        btnMale.setOnClickListener(v -> {
            getAssessmentActivity().getAssessmentData().setGender("Male");
            updateSelection(true);
        });

        btnFemale.setOnClickListener(v -> {
            getAssessmentActivity().getAssessmentData().setGender("Female");
            updateSelection(false);
        });
    }

    private void updateSelection(boolean isMale) {
        if (isMale) {
            btnMale.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            btnMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            btnFemale.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
            btnFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            btnFemale.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            btnFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            btnMale.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
            btnMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
