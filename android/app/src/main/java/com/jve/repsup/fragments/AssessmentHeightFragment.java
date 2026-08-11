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
 * Step 5: Height input with ruler slider and cm/ft toggle.
 */
public class AssessmentHeightFragment extends Fragment {

    private TextView tvHeightValue;
    private TextView tvHeightUnit;
    private SeekBar sbHeight;
    private Button btnCm;
    private Button btnFt;
    private boolean isCm = true;

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

        tvHeightValue = view.findViewById(R.id.tvHeightValue);
        tvHeightUnit = view.findViewById(R.id.tvHeightUnit);
        sbHeight = view.findViewById(R.id.sbHeight);
        btnCm = view.findViewById(R.id.btnCm);
        btnFt = view.findViewById(R.id.btnFt);

        // Restore saved values
        isCm = getAssessmentActivity().getAssessmentData().isHeightInCm();
        double savedHeight = getAssessmentActivity().getAssessmentData().getHeight();
        updateUnitToggle();
        updateDisplay(savedHeight);
        sbHeight.setProgress((int) savedHeight - 50);

        btnCm.setOnClickListener(v -> {
            if (!isCm) {
                double currentFt = getAssessmentActivity().getAssessmentData().getHeight();
                double cmValue = currentFt * 30.48;
                isCm = true;
                getAssessmentActivity().getAssessmentData().setHeightInCm(true);
                getAssessmentActivity().getAssessmentData().setHeight(Math.round(cmValue));
                sbHeight.setProgress((int) Math.round(cmValue) - 50);
                updateUnitToggle();
                updateDisplay(Math.round(cmValue));
            }
        });

        btnFt.setOnClickListener(v -> {
            if (isCm) {
                double currentCm = getAssessmentActivity().getAssessmentData().getHeight();
                double ftValue = currentCm / 30.48;
                isCm = false;
                getAssessmentActivity().getAssessmentData().setHeightInCm(false);
                getAssessmentActivity().getAssessmentData().setHeight(Math.round(ftValue * 10.0) / 10.0);
                sbHeight.setProgress((int) Math.round(ftValue * 10.0) - 50);
                updateUnitToggle();
                updateDisplay(Math.round(ftValue * 10.0) / 10.0);
            }
        });

        sbHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double height = progress + 50;
                updateDisplay(height);
                getAssessmentActivity().getAssessmentData().setHeight(height);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateDisplay(double height) {
        if (isCm) {
            tvHeightValue.setText(String.valueOf((int) height));
        } else {
            tvHeightValue.setText(String.format("%.1f", height));
        }
        tvHeightUnit.setText(isCm ? getString(R.string.assessment_unit_cm) : getString(R.string.assessment_unit_ft));
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
