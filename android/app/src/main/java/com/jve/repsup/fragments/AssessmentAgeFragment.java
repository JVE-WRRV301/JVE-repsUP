package com.jve.repsup.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.FitnessAssessmentActivity;

/**
 * Step 3: Age selection via SeekBar with manual EditText input.
 */
public class AssessmentAgeFragment extends Fragment {

    private EditText etAgeValue;
    private SeekBar sbAge;
    private boolean isUpdatingFromSeekbar = false;
    private boolean isUpdatingFromEditText = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_age, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAgeValue = view.findViewById(R.id.etAgeValue);
        sbAge = view.findViewById(R.id.sbAge);

        // Restore saved age
        int savedAge = getAssessmentActivity().getAssessmentData().getAge();
        etAgeValue.setText(String.valueOf(savedAge));
        sbAge.setProgress(savedAge);

        sbAge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingFromEditText) return;
                isUpdatingFromSeekbar = true;
                int age = progress;
                etAgeValue.setText(String.valueOf(age));
                etAgeValue.setSelection(etAgeValue.getText().length());
                getAssessmentActivity().getAssessmentData().setAge(age);
                isUpdatingFromSeekbar = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        etAgeValue.addTextChangedListener(new TextWatcher() {
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
                        int age = Integer.parseInt(text);
                        if (age > 99) age = 99;
                        sbAge.setProgress(age);
                        getAssessmentActivity().getAssessmentData().setAge(age);
                    } catch (NumberFormatException ignored) {}
                }
                isUpdatingFromEditText = false;
            }
        });
    }

    private FitnessAssessmentActivity getAssessmentActivity() {
        return (FitnessAssessmentActivity) requireActivity();
    }
}
