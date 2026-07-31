package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.R;
import com.jve.repsup.utils.SessionManager;

/**
 * AdminMainActivity serves as the admin dashboard after successful login.
 * Provides navigation options for managing workouts, exercises, and logging out.
 */
public class AdminMainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        sessionManager = new SessionManager(this);

        initViews();
    }

    /**
     * Initializes view references and sets up click listeners for management options.
     */
    private void initViews() {
        Button btnCreateWorkout = findViewById(R.id.btnCreateWorkout);
        Button btnCreateExercise = findViewById(R.id.btnCreateExercise);
        Button btnManageWorkouts = findViewById(R.id.btnManageWorkouts);
        Button btnLogout = findViewById(R.id.btnAdminLogout);

        btnCreateWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminCreateWorkoutActivity.class);
            startActivity(intent);
        });

        btnCreateExercise.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminCreateExerciseActivity.class);
            startActivity(intent);
        });

        btnManageWorkouts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminManageWorkoutsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Clears admin session and navigates back to WelcomeActivity.
     */
    private void logout() {
        sessionManager.setAdmin(false);
        sessionManager.clearSession();
        Intent intent = new Intent(AdminMainActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
