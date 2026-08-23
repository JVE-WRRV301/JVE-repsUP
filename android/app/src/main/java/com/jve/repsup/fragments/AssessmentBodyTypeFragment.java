package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 6: Body type selection in a 2x3 grid with gender-aware images.
 * Body types: Skinny, Average, Toned, Muscular, Chubby, Overweight.
 */
public class AssessmentBodyTypeFragment extends Fragment {

    private ImageView[] bodyTypeViews;
    private String[] bodyTypeNames = {"Skinny", "Average", "Toned", "Muscular", "Chubby", "Overweight"};

    // Male image resources
    private int[] maleImages = {
            R.drawable.ic_bodytype_male_skinny,
            R.drawable.ic_bodytype_male_average,
            R.drawable.ic_bodytype_male_toned,
            R.drawable.ic_bodytype_male_muscular,
            R.drawable.ic_bodytype_male_chubby,
            R.drawable.ic_bodytype_male_overweight
    };

    // Female image resources
    private int[] femaleImages = {
            R.drawable.ic_bodytype_female_skinny,
            R.drawable.ic_bodytype_female_average,
            R.drawable.ic_bodytype_female_toned,
            R.drawable.ic_bodytype_female_muscular,
            R.drawable.ic_bodytype_female_chubby,
            R.drawable.ic_bodytype_female_overweight
    };

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

        bodyTypeViews = new ImageView[]{
                view.findViewById(R.id.ivBodyType0),
                view.findViewById(R.id.ivBodyType1),
                view.findViewById(R.id.ivBodyType2),
                view.findViewById(R.id.ivBodyType3),
                view.findViewById(R.id.ivBodyType4),
                view.findViewById(R.id.ivBodyType5)
        };

        // Determine gender for image selection
        String gender = getAssessmentActivity().getAssessmentData().getGender();
        boolean isFemale = "Female".equals(gender);
        int[] images = isFemale ? femaleImages : maleImages;

        // Set images and click listeners
        for (int i = 0; i < bodyTypeViews.length; i++) {
            bodyTypeViews[i].setImageResource(images[i]);
            final int index = i;
            bodyTypeViews[i].setOnClickListener(v -> selectBodyType(index));
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
        getAssessmentActivity().notifyDataChanged();
    }

    private void highlightSelected(int selectedIndex) {
        for (int i = 0; i < bodyTypeViews.length; i++) {
            if (i == selectedIndex) {
                bodyTypeViews[i].setForeground(getResources().getDrawable(R.drawable.bg_bodytype_selected, null));
            } else {
                bodyTypeViews[i].setForeground(getResources().getDrawable(R.drawable.bg_bodytype_unselected, null));
            }
        }
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
