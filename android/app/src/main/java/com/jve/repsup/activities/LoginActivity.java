package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jve.repsup.MainActivity;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.AdminRepository;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SessionManager;

/**
 * LoginActivity displays a dark top section with a gym image and "Welcome back!" text,
 * and a white bottom card with username/password fields and a Login button.
 * Authenticates against the User table via UserRepository on a background thread.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvError;
    private TextView tvSignUp;

    private UserRepository userRepository;
    private AdminRepository adminRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user already has an active session
        sessionManager = new SessionManager(this);
        if (sessionManager.hasActiveSession()) {
            navigateToMain();
            return;
        }

        // Draw edge-to-edge and keep the form clear of the system bars and the
        // keyboard on Android 15+.
        InsetsHelper.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_login);

        InsetsHelper.applySystemBarsAndImeBottomPadding(findViewById(R.id.scrollLogin));

        userRepository = new UserRepository();
        adminRepository = new AdminRepository();

        initViews();
        setupListeners();
    }

    /**
     * Initializes all view references from the layout.
     */
    private void initViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    /**
     * Sets up click listeners for the Login button and Sign Up link.
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Validates input fields and attempts authentication via UserRepository.
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
            tilUsername.setError(getString(R.string.login_username_hint));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.login_password_hint));
            return;
        }

        // Disable button while authenticating
        btnLogin.setEnabled(false);

        userRepository.authenticate(username, password, new ConnectionManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    runOnUiThread(() -> {
                        // User authentication successful — store session and navigate
                        sessionManager.createSession(user.getUserId(), user.getUsername(), user.getFirstName());
                        navigateToMain();
                    });
                } else {
                    // User not found — try admin credentials
                    attemptAdminLogin(username, password);
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.login_connection_error));
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
     * Navigates to MainActivity and finishes LoginActivity so the user can't go back.
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Attempts admin authentication as a fallback when regular user login fails.
     * If admin credentials match, navigates to AdminMainActivity.
     * If not, shows a generic login error.
     */
    private void attemptAdminLogin(String username, String password) {
        adminRepository.authenticate(username, password, new ConnectionManager.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isAdmin) {
                runOnUiThread(() -> {
                    if (isAdmin != null && isAdmin) {
                        // Admin authentication successful
                        sessionManager.setAdmin(true);
                        navigateToAdminMain();
                    } else {
                        // Both user and admin auth failed
                        showError(getString(R.string.login_error));
                        btnLogin.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.login_connection_error));
                    btnLogin.setEnabled(true);
                });
            }
        });
    }

    /**
     * Navigates to AdminMainActivity and clears the back stack.
     */
    private void navigateToAdminMain() {
        Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
