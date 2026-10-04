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
 * Step 6: Body type selection, three options stacked one per row, gender-aware.
 *
 * The three body types differ by gender (the app only ships art for these):
 *   Male:   average, built, chubby
 *   Female: average, toned, chubby
 *
 * The stored value is the lowercase key (e.g. "average", "built") so it matches
 * the body_shape used everywhere else - the base body drawables
 * (base_{gender}_{bodytype}_{tone}) and the per-body-type clothing folders.
 */
public class AssessmentBodyTypeFragment extends Fragment {

    private ImageView[] bodyTypeViews;

    // Lowercase body-type keys per gender (index-aligned with the 3 image slots).
    private static final String[] MALE_BODY_TYPES = {"average", "built", "chubby"};
    private static final String[] FEMALE_BODY_TYPES = {"average", "toned", "chubby"};

    // The keys actually in use for the current gender (set in onViewCreated).
    private String[] bodyTypeKeys = MALE_BODY_TYPES;

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
                view.findViewById(R.id.ivBodyType2)
        };

        // Gender drives both which body types are offered and which art is shown.
        String gender = getAssessmentActivity().getAssessmentData().getGender();
        boolean isFemale = "Female".equalsIgnoreCase(gender);
        String g = isFemale ? "female" : "male";
        bodyTypeKeys = isFemale ? FEMALE_BODY_TYPES : MALE_BODY_TYPES;

        // Preview each option with the full-body avatar_{gender}_{bodytype} image.
        for (int i = 0; i < bodyTypeViews.length; i++) {
            int resId = getResources().getIdentifier(
                    "avatar_" + g + "_" + bodyTypeKeys[i], "drawable", requireContext().getPackageName());
            if (resId != 0) {
                bodyTypeViews[i].setImageResource(resId);
            }
            final int index = i;
            bodyTypeViews[i].setOnClickListener(v -> selectBodyType(index));
        }

        // Restore a previously chosen body type (stored as the lowercase key).
        String savedBodyType = getAssessmentActivity().getAssessmentData().getBodyType();
        if (savedBodyType != null) {
            for (int i = 0; i < bodyTypeKeys.length; i++) {
                if (bodyTypeKeys[i].equalsIgnoreCase(savedBodyType)) {
                    highlightSelected(i);
                    break;
                }
            }
        }
    }

    private void selectBodyType(int index) {
        getAssessmentActivity().getAssessmentData().setBodyType(bodyTypeKeys[index]);
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
