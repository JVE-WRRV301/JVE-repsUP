package com.jve.repsup.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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
 * Step 5: Height input with ruler slider, cm/ft toggle, and manual EditText input.
 * In cm mode: SeekBar 0-300 (cm), EditText accepts number.
 * In ft mode: SeekBar 0-96 (total inches), EditText accepts format like 5'8, display is feet'inches".
 */
public class AssessmentHeightFragment extends Fragment {

    private EditText etHeightValue;
    private TextView tvHeightUnit;
    private SeekBar sbHeight;
    private Button btnCm;
    private Button btnFt;
    private boolean isCm = true;
    private boolean isUpdatingFromSeekbar = false;
    private boolean isUpdatingFromEditText = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_height, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etHeightValue = view.findViewById(R.id.etHeightValue);
        tvHeightUnit = view.findViewById(R.id.tvHeightUnit);
        sbHeight = view.findViewById(R.id.sbHeight);
        btnCm = view.findViewById(R.id.btnCm);
        btnFt = view.findViewById(R.id.btnFt);

        // Restore saved values
        isCm = getAssessmentActivity().getAssessmentData().isHeightInCm();
        double savedHeight = getAssessmentActivity().getAssessmentData().getHeight();

        setupForMode();
        updateUnitToggle();

        if (isCm) {
            int cm = (int) savedHeight;
            sbHeight.setProgress(cm);
            updateDisplay(cm);
        } else {
            int totalInches = (int) savedHeight;
            sbHeight.setProgress(totalInches);
            updateDisplay(totalInches);
        }

        btnCm.setOnClickListener(v -> {
            if (!isCm) {
                // Convert total inches to cm
                double totalInches = getAssessmentActivity().getAssessmentData().getHeight();
                int cmValue = (int) Math.round(totalInches * 2.54);
                isCm = true;
                getAssessmentActivity().getAssessmentData().setHeightInCm(true);
                getAssessmentActivity().getAssessmentData().setHeight(cmValue);
                setupForMode();
                updateUnitToggle();
                isUpdatingFromSeekbar = true;
                sbHeight.setProgress(cmValue);
                updateDisplay(cmValue);
                isUpdatingFromSeekbar = false;
            }
        });

        btnFt.setOnClickListener(v -> {
            if (isCm) {
                // Convert cm to total inches
                double currentCm = getAssessmentActivity().getAssessmentData().getHeight();
                int totalInches = (int) Math.round(currentCm / 2.54);
                isCm = false;
                getAssessmentActivity().getAssessmentData().setHeightInCm(false);
                getAssessmentActivity().getAssessmentData().setHeight(totalInches);
                setupForMode();
                updateUnitToggle();
                isUpdatingFromSeekbar = true;
                sbHeight.setProgress(totalInches);
                updateDisplay(totalInches);
                isUpdatingFromSeekbar = false;
            }
        });

        sbHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingFromEditText) return;
                isUpdatingFromSeekbar = true;
                updateDisplay(progress);
                getAssessmentActivity().getAssessmentData().setHeight(progress);
                isUpdatingFromSeekbar = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        etHeightValue.addTextChangedListener(new TextWatcher() {
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
                    if (isCm) {
                        try {
                            int cm = Integer.parseInt(text);
                            if (cm > 300) cm = 300;
                            sbHeight.setProgress(cm);
                            getAssessmentActivity().getAssessmentData().setHeight(cm);
                        } catch (NumberFormatException ignored) {}
                    } else {
                        // Parse feet'inches format (e.g., "5'8" or "5'11")
                        int totalInches = parseFeetInches(text);
                        if (totalInches >= 0) {
                            if (totalInches > 96) totalInches = 96;
                            sbHeight.setProgress(totalInches);
                            getAssessmentActivity().getAssessmentData().setHeight(totalInches);
                        }
                    }
                }
                isUpdatingFromEditText = false;
            }
        });
    }

    /**
     * Configures SeekBar range and EditText input type for the current mode.
     */
    private void setupForMode() {
        if (isCm) {
            sbHeight.setMax(300);
            etHeightValue.setInputType(InputType.TYPE_CLASS_NUMBER);
            etHeightValue.setMaxEms(3);
        } else {
            sbHeight.setMax(96);
            // Allow text input for feet'inches format
            etHeightValue.setInputType(InputType.TYPE_CLASS_TEXT);
            etHeightValue.setMaxEms(5);
        }
    }

    /**
     * Updates the EditText display based on the raw value.
     * In cm mode: value = cm. In ft mode: value = total inches.
     */
    private void updateDisplay(int value) {
        if (!isUpdatingFromEditText) {
            isUpdatingFromSeekbar = true;
            if (isCm) {
                etHeightValue.setText(String.valueOf(value));
            } else {
                int feet = value / 12;
                int inches = value % 12;
                etHeightValue.setText(feet + "'" + inches);
            }
            etHeightValue.setSelection(etHeightValue.getText().length());
            isUpdatingFromSeekbar = false;
        }
        tvHeightUnit.setText(isCm ? getString(R.string.assessment_unit_cm) : getString(R.string.assessment_unit_ft));
    }

    /**
     * Parses a feet'inches string (e.g., "5'8") into total inches.
     * Returns -1 if format is invalid.
     */
    private int parseFeetInches(String text) {
        try {
            if (text.contains("'")) {
                String[] parts = text.split("'");
                int feet = Integer.parseInt(parts[0].trim());
                int inches = 0;
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    // Remove trailing " if present
                    String inchStr = parts[1].replace("\"", "").trim();
                    if (!inchStr.isEmpty()) {
                        inches = Integer.parseInt(inchStr);
                    }
                }
                return feet * 12 + inches;
            } else {
                // Just a number — treat as feet
                int feet = Integer.parseInt(text.trim());
                return feet * 12;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updateUnitToggle() {
        if (isCm) {
            btnCm.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            btnCm.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            btnFt.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
            btnFt.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            btnFt.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.light_blue));
            btnFt.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_text));
            btnCm.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.dark_grey_surface));
            btnCm.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
