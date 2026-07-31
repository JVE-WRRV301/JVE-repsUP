package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 6: Body type selection in a 2x3 grid.
 */
public class AssessmentBodyTypeFragment extends Fragment {

    private LinearLayout llEctomorph, llMesomorph, llEndomorph, llAthletic, llStocky, llSlim;
    private LinearLayout[] allOptions;
    private String[] bodyTypeNames = {"Ectomorph", "Mesomorph", "Endomorph", "Athletic", "Stocky", "Slim"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_body_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llEctomorph = view.findViewById(R.id.llEctomorph);
        llMesomorph = view.findViewById(R.id.llMesomorph);
        llEndomorph = view.findViewById(R.id.llEndomorph);
        llAthletic = view.findViewById(R.id.llAthletic);
        llStocky = view.findViewById(R.id.llStocky);
        llSlim = view.findViewById(R.id.llSlim);

        allOptions = new LinearLayout[]{llEctomorph, llMesomorph, llEndomorph, llAthletic, llStocky, llSlim};

        // Set click listeners
        for (int i = 0; i < allOptions.length; i++) {
            final int index = i;
            allOptions[i].setOnClickListener(v -> selectBodyType(index));
        }

        // Restore saved selection
        String savedBodyType = getAssessmentActivity().getAssessmentData().getBodyType();
        if (savedBodyType != null) {
            for (int i = 0; i < bodyTypeNames.length; i++) {
                if (bodyTypeNames[i].equals(savedBodyType)) {
                    highlightSelected(i);
                    break;
                }
            }
        }
    }

    private void selectBodyType(int index) {
        getAssessmentActivity().getAssessmentData().setBodyType(bodyTypeNames[index]);
        highlightSelected(index);
    }

    private void highlightSelected(int selectedIndex) {
        for (int i = 0; i < allOptions.length; i++) {
            if (i == selectedIndex) {
                allOptions[i].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue));
            } else {
                allOptions[i].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_grey_surface));
            }
        }
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
