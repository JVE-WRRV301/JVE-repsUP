package com.jve.repsup.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;
import com.jve.repsup.utils.RulerSliderView;

/**
 * Step 4: Weight input with ruler slider, kg/lbs toggle, and manual EditText input.
 * Features a blue underline under the value and a custom ruler with numbered tick marks.
 */
public class AssessmentWeightFragment extends Fragment {

    private EditText etWeightValue;
    private TextView tvWeightUnit;
    private RulerSliderView rulerSlider;
    private Button btnKg;
    private Button btnLbs;
    private boolean isKg = true;
    private boolean isUpdatingFromSlider = false;
    private boolean isUpdatingFromEditText = false;

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

        etWeightValue = view.findViewById(R.id.etWeightValue);
        tvWeightUnit = view.findViewById(R.id.tvWeightUnit);
        rulerSlider = view.findViewById(R.id.rulerSlider);
        btnKg = view.findViewById(R.id.btnKg);
        btnLbs = view.findViewById(R.id.btnLbs);

        // Restore saved values
        isKg = getAssessmentActivity().getAssessmentData().isWeightInKg();
        double savedWeight = getAssessmentActivity().getAssessmentData().getWeight();

        setupRulerForMode();
        updateUnitToggle();
        updateDisplay((int) savedWeight);
        rulerSlider.setValue((int) savedWeight);

        btnKg.setOnClickListener(v -> {
            if (!isKg) {
                double currentLbs = getAssessmentActivity().getAssessmentData().getWeight();
                int kgValue = (int) Math.round(currentLbs * 0.453592);
                isKg = true;
                getAssessmentActivity().getAssessmentData().setWeightInKg(true);
                getAssessmentActivity().getAssessmentData().setWeight(kgValue);
                setupRulerForMode();
                isUpdatingFromSlider = true;
                rulerSlider.setValue(kgValue);
                updateDisplay(kgValue);
                isUpdatingFromSlider = false;
                updateUnitToggle();
            }
        });

        btnLbs.setOnClickListener(v -> {
            if (isKg) {
                double currentKg = getAssessmentActivity().getAssessmentData().getWeight();
                int lbsValue = (int) Math.round(currentKg * 2.20462);
                isKg = false;
                getAssessmentActivity().getAssessmentData().setWeightInKg(false);
                getAssessmentActivity().getAssessmentData().setWeight(lbsValue);
                setupRulerForMode();
                isUpdatingFromSlider = true;
                rulerSlider.setValue(lbsValue);
                updateDisplay(lbsValue);
                isUpdatingFromSlider = false;
                updateUnitToggle();
            }
        });

        rulerSlider.setOnValueChangedListener(value -> {
            if (isUpdatingFromEditText) return;
            isUpdatingFromSlider = true;
            updateDisplay(value);
            getAssessmentActivity().getAssessmentData().setWeight(value);
            isUpdatingFromSlider = false;
        });

        etWeightValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromSlider) return;
                isUpdatingFromEditText = true;
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    try {
                        int weight = Integer.parseInt(text);
                        int max = isKg ? 300 : 660;
                        if (weight > max) weight = max;
                        rulerSlider.setValue(weight);
                        getAssessmentActivity().getAssessmentData().setWeight(weight);
                    } catch (NumberFormatException ignored) {}
                }
                isUpdatingFromEditText = false;
            }
        });
    }

    private void setupRulerForMode() {
        if (isKg) {
            rulerSlider.setRange(20, 300);
            rulerSlider.setMajorTickInterval(10);
        } else {
            rulerSlider.setRange(44, 660);
            rulerSlider.setMajorTickInterval(10);
        }
    }

    private void updateDisplay(int weight) {
        if (!isUpdatingFromEditText) {
            isUpdatingFromSlider = true;
            etWeightValue.setText(String.valueOf(weight));
            etWeightValue.setSelection(etWeightValue.getText().length());
            isUpdatingFromSlider = false;
        }
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
