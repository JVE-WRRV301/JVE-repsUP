package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;

/**
 * Registration screen with form: Name, Username, Email, Password, Confirm Password.
 * Validates all fields, checks username uniqueness, and inserts a new User record.
 * On success: stores session and navigates to FitnessAssessmentActivity.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private TextView tvError;
    private Button btnGetStarted;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository();
        sessionManager = new SessionManager(this);

        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvError = findViewById(R.id.tvError);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        // Clear previous error
        tvError.setVisibility(View.GONE);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validate all fields required
        if (name.isEmpty() || username.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            showError(getString(R.string.error_fields_required));
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showError(getString(R.string.error_password_mismatch));
            return;
        }

        // Disable button while processing
        btnGetStarted.setEnabled(false);

        // Check username uniqueness
        userRepository.isUsernameTaken(username, new ConnectionManager.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isTaken) {
                runOnUiThread(() -> {
                    if (isTaken) {
                        showError(getString(R.string.error_username_taken));
                        btnGetStarted.setEnabled(true);
                    } else {
                        // Proceed with registration
                        performRegistration(name, username, email, password);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_registration_failed));
                    btnGetStarted.setEnabled(true);
                });
            }
        });
    }

    private void performRegistration(String name, String username, String email, String password) {
        User newUser = new User();
        newUser.setFirstName(name);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);

        userRepository.register(newUser, new ConnectionManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long userId) {
                runOnUiThread(() -> {
                    // Store session
                    sessionManager.createSession(userId.intValue(), username);

                    // Navigate to FitnessAssessmentActivity
                    Intent intent = new Intent(RegisterActivity.this, FitnessAssessmentActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_registration_failed));
                    btnGetStarted.setEnabled(true);
                });
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
