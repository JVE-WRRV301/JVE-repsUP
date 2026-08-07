package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 4: Weight input with ruler slider and kg/lbs toggle.
 */
public class AssessmentWeightFragment extends Fragment {

    private TextView tvWeightValue;
    private TextView tvWeightUnit;
    private SeekBar sbWeight;
    private Button btnKg;
    private Button btnLbs;
    private boolean isKg = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_weight, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWeightValue = view.findViewById(R.id.tvWeightValue);
        tvWeightUnit = view.findViewById(R.id.tvWeightUnit);
        sbWeight = view.findViewById(R.id.sbWeight);
        btnKg = view.findViewById(R.id.btnKg);
        btnLbs = view.findViewById(R.id.btnLbs);

        // Restore saved values
        isKg = getAssessmentActivity().getAssessmentData().isWeightInKg();
        double savedWeight = getAssessmentActivity().getAssessmentData().getWeight();
        updateUnitToggle();
        updateDisplay(savedWeight);
        sbWeight.setProgress((int) savedWeight - 20);

        btnKg.setOnClickListener(v -> {
            if (!isKg) {
                double currentLbs = getAssessmentActivity().getAssessmentData().getWeight();
                double kgValue = currentLbs * 0.453592;
                isKg = true;
                getAssessmentActivity().getAssessmentData().setWeightInKg(true);
                getAssessmentActivity().getAssessmentData().setWeight(Math.round(kgValue));
                sbWeight.setProgress((int) Math.round(kgValue) - 20);
                updateUnitToggle();
                updateDisplay(Math.round(kgValue));
            }
        });

        btnLbs.setOnClickListener(v -> {
            if (isKg) {
                double currentKg = getAssessmentActivity().getAssessmentData().getWeight();
                double lbsValue = currentKg * 2.20462;
                isKg = false;
                getAssessmentActivity().getAssessmentData().setWeightInKg(false);
                getAssessmentActivity().getAssessmentData().setWeight(Math.round(lbsValue));
                sbWeight.setProgress((int) Math.round(lbsValue) - 20);
                updateUnitToggle();
                updateDisplay(Math.round(lbsValue));
            }
        });

        sbWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double weight = progress + 20;
                updateDisplay(weight);
                getAssessmentActivity().getAssessmentData().setWeight(weight);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateDisplay(double weight) {
        tvWeightValue.setText(String.valueOf((int) weight));
        tvWeightUnit.setText(isKg ? getString(R.string.assessment_unit_kg) : getString(R.string.assessment_unit_lbs));
    }

    private void updateUnitToggle() {
        if (isKg) {
            btnKg.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            btnKg.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            btnLbs.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
            btnLbs.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            btnLbs.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            btnLbs.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            btnKg.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
            btnKg.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
