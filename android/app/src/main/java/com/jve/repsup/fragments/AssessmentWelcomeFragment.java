package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 1: Personalized welcome screen introducing the fitness assessment.
 * Displays "WELCOME NAME!" with the user's first name uppercased.
 */
public class AssessmentWelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvWelcomeName = view.findViewById(R.id.tvWelcomeName);

        // Retrieve the user's name from the parent activity
        String name = null;
        if (getActivity() instanceof FitnessAssessmentActivity) {
            name = ((FitnessAssessmentActivity) getActivity()).getUserName();
        }

        // Set personalized welcome text
        if (name != null && !name.isEmpty()) {
            String welcomeText = getString(R.string.assessment_welcome_personalized,
                    name.toUpperCase());
            tvWelcomeName.setText(welcomeText);
        } else {
            // Fallback if name is not available
            String welcomeText = getString(R.string.assessment_welcome_personalized, "");
            // Show "WELCOME!" without trailing space issue
            tvWelcomeName.setText("WELCOME!");
        }
    }
}
