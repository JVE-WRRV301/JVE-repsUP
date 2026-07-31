package com.jve.repsup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jve.repsup.R;
import com.jve.repsup.activities.AdminCreateWorkoutActivity;

/**
 * Step 1: Category selection screen.
 * Displays four category buttons: Abdominal, Full Body, Lower Body, Upper Body.
 */
public class CreateWorkoutCategoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_workout_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAbdominal = view.findViewById(R.id.btnCategoryAbdominal);
        Button btnFullBody = view.findViewById(R.id.btnCategoryFullBody);
        Button btnLowerBody = view.findViewById(R.id.btnCategoryLowerBody);
        Button btnUpperBody = view.findViewById(R.id.btnCategoryUpperBody);

        View.OnClickListener categoryClickListener = v -> {
            String category = ((Button) v).getText().toString();
            ((AdminCreateWorkoutActivity) requireActivity()).onCategorySelected(category);
        };

        btnAbdominal.setOnClickListener(categoryClickListener);
        btnFullBody.setOnClickListener(categoryClickListener);
        btnLowerBody.setOnClickListener(categoryClickListener);
        btnUpperBody.setOnClickListener(categoryClickListener);
    }
}
