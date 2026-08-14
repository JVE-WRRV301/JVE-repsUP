package com.jve.repsup.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 4: Weight input with ruler slider, kg/lbs toggle, and manual EditText input.
 */
public class AssessmentWeightFragment extends Fragment {

    private EditText etWeightValue;
    private TextView tvWeightUnit;
    private SeekBar sbWeight;
    private Button btnKg;
    private Button btnLbs;
    private boolean isKg = true;
    private boolean isUpdatingFromSeekbar = false;
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
        sbWeight = view.findViewById(R.id.sbWeight);
        btnKg = view.findViewById(R.id.btnKg);
        btnLbs = view.findViewById(R.id.btnLbs);

        // Restore saved values
        isKg = getAssessmentActivity().getAssessmentData().isWeightInKg();
        double savedWeight = getAssessmentActivity().getAssessmentData().getWeight();
        updateUnitToggle();
        updateDisplay((int) savedWeight);
        sbWeight.setProgress((int) savedWeight);

        btnKg.setOnClickListener(v -> {
            if (!isKg) {
                double currentLbs = getAssessmentActivity().getAssessmentData().getWeight();
                int kgValue = (int) Math.round(currentLbs * 0.453592);
                isKg = true;
                getAssessmentActivity().getAssessmentData().setWeightInKg(true);
                getAssessmentActivity().getAssessmentData().setWeight(kgValue);
                isUpdatingFromSeekbar = true;
                sbWeight.setProgress(kgValue);
                updateDisplay(kgValue);
                isUpdatingFromSeekbar = false;
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
                isUpdatingFromSeekbar = true;
                sbWeight.setProgress(Math.min(lbsValue, 500));
                updateDisplay(lbsValue);
                isUpdatingFromSeekbar = false;
                updateUnitToggle();
            }
        });

        sbWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingFromEditText) return;
                isUpdatingFromSeekbar = true;
                updateDisplay(progress);
                getAssessmentActivity().getAssessmentData().setWeight(progress);
                isUpdatingFromSeekbar = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        etWeightValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromSeekbar) return;
                isUpdatingFromEditText = true;
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    try {
                        int weight = Integer.parseInt(text);
                        if (weight > 500) weight = 500;
                        sbWeight.setProgress(weight);
                        getAssessmentActivity().getAssessmentData().setWeight(weight);
                    } catch (NumberFormatException ignored) {}
                }
                isUpdatingFromEditText = false;
            }
        });
    }

    private void updateDisplay(int weight) {
        if (!isUpdatingFromEditText) {
            isUpdatingFromSeekbar = true;
            etWeightValue.setText(String.valueOf(weight));
            etWeightValue.setSelection(etWeightValue.getText().length());
            isUpdatingFromSeekbar = false;
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
