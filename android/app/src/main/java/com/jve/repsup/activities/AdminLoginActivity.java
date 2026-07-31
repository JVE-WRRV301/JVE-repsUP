package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.repositories.AdminRepository;
import com.jve.repsup.utils.SessionManager;

/**
 * AdminLoginActivity displays a dark-themed login form for admin authentication.
 * Authenticates against the Admins table via AdminRepository on a background thread.
 */
public class AdminLoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvError;

    private AdminRepository adminRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminRepository = new AdminRepository();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    /**
     * Initializes all view references from the layout.
     */
    private void initViews() {
        tilUsername = findViewById(R.id.tilAdminUsername);
        tilPassword = findViewById(R.id.tilAdminPassword);
        etUsername = findViewById(R.id.etAdminUsername);
        etPassword = findViewById(R.id.etAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);
        tvError = findViewById(R.id.tvAdminError);
    }

    /**
     * Sets up click listeners for the Login button.
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Validates input fields and attempts authentication via AdminRepository.
     */
    private void attemptLogin() {
        // Clear previous errors
        tvError.setVisibility(View.GONE);
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Basic validation
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.admin_username_hint));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.admin_password_hint));
            return;
        }

        // Disable button while authenticating
        btnLogin.setEnabled(false);

        adminRepository.authenticate(username, password, new ConnectionManager.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                runOnUiThread(() -> {
                    if (result != null && result) {
                        // Authentication successful — store admin session and navigate
                        sessionManager.setAdmin(true);
                        navigateToAdminMain();
                    } else {
                        // Authentication failed
                        showError(getString(R.string.admin_login_error));
                        btnLogin.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.admin_connection_error));
                    btnLogin.setEnabled(true);
                });
            }
        });
    }

    /**
     * Displays an error message below the form fields.
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    /**
     * Navigates to AdminMainActivity and finishes this activity.
     */
    private void navigateToAdminMain() {
        Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
