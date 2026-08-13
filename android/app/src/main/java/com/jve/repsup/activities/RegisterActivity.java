package com.jve.repsup.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Registration screen with redesigned UI: background image at top, rounded form panel below.
 * Features per-field validation, real-time debounced username availability checking,
 * email domain/extension validation, and password length enforcement.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final long USERNAME_DEBOUNCE_MS = 500;

    private EditText etName;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private TextView tvNameError;
    private TextView tvUsernameStatus;
    private TextView tvEmailError;
    private TextView tvPasswordError;
    private TextView tvConfirmPasswordError;

    private Button btnGetStarted;
    private ImageButton btnBack;
    private TextView tvHaveAccount;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    private final Handler usernameHandler = new Handler(Looper.getMainLooper());
    private Runnable usernameCheckRunnable;
    private boolean isUsernameTaken = false;

    private static final Set<String> VALID_EMAIL_PROVIDERS = new HashSet<>(Arrays.asList(
            "gmail", "outlook", "hotmail", "yahoo", "icloud", "protonmail",
            "aol", "zoho", "mail", "yandex", "live", "msn"
    ));

    private static final Set<String> VALID_EMAIL_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".com", ".co.za", ".co.uk", ".org", ".net", ".edu",
            ".gov", ".io", ".me", ".info", ".biz", ".us",
            ".ca", ".au", ".de", ".fr", ".in", ".za"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen immersive like the welcome page
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_register);

        userRepository = new UserRepository();
        sessionManager = new SessionManager(this);

        initViews();
        setupBackButton();
        setupLoginLink();
        setupTextWatchers();

        btnGetStarted.setOnClickListener(v -> attemptRegistration());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tvNameError = findViewById(R.id.tvNameError);
        tvUsernameStatus = findViewById(R.id.tvUsernameStatus);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnBack = findViewById(R.id.btnBack);
        tvHaveAccount = findViewById(R.id.tvHaveAccount);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupLoginLink() {
        String fullText = getString(R.string.register_have_account);
        String loginWord = getString(R.string.register_login_word);
        int loginStart = fullText.indexOf(loginWord);

        if (loginStart == -1) {
            tvHaveAccount.setOnClickListener(v ->
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
            );
            return;
        }

        SpannableString spannable = new SpannableString(fullText);
        int loginEnd = loginStart + loginWord.length();
        int blueColor = ContextCompat.getColor(this, R.color.light_blue);

        ClickableSpan loginSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(blueColor);
                ds.setUnderlineText(false);
            }
        };

        spannable.setSpan(loginSpan, loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvHaveAccount.setText(spannable);
        tvHaveAccount.setMovementMethod(LinkMovementMethod.getInstance());
        tvHaveAccount.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupTextWatchers() {
        // Clear name error on typing
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                hideError(tvNameError);
            }
        });

        // Debounced username availability check
        etUsername.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Remove any pending check
                if (usernameCheckRunnable != null) {
                    usernameHandler.removeCallbacks(usernameCheckRunnable);
                }

                String username = s.toString().trim();
                if (username.isEmpty()) {
                    tvUsernameStatus.setVisibility(View.GONE);
                    isUsernameTaken = false;
                    return;
                }

                // Show checking state
                tvUsernameStatus.setText(getString(R.string.error_username_checking));
                tvUsernameStatus.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.muted));
                tvUsernameStatus.setVisibility(View.VISIBLE);

                // Debounce the check
                usernameCheckRunnable = () -> checkUsernameAvailability(username);
                usernameHandler.postDelayed(usernameCheckRunnable, USERNAME_DEBOUNCE_MS);
            }
        });

        // Clear email error on typing
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                hideError(tvEmailError);
            }
        });

        // Clear password error on typing
        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                hideError(tvPasswordError);
            }
        });

        // Clear confirm password error on typing
        etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                hideError(tvConfirmPasswordError);
            }
        });
    }

    private void checkUsernameAvailability(String username) {
        userRepository.isUsernameTaken(username, new ConnectionManager.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isTaken) {
                runOnUiThread(() -> {
                    // Verify the field still has the same value (user may have kept typing)
                    String currentText = etUsername.getText() != null
                            ? etUsername.getText().toString().trim() : "";
                    if (!currentText.equals(username)) {
                        return;
                    }

                    isUsernameTaken = isTaken;
                    if (isTaken) {
                        tvUsernameStatus.setText(getString(R.string.error_username_taken));
                        tvUsernameStatus.setTextColor(
                                ContextCompat.getColor(RegisterActivity.this, R.color.red));
                    } else {
                        tvUsernameStatus.setText(getString(R.string.error_username_available));
                        tvUsernameStatus.setTextColor(
                                ContextCompat.getColor(RegisterActivity.this, R.color.green));
                    }
                    tvUsernameStatus.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> tvUsernameStatus.setVisibility(View.GONE));
            }
        });
    }

    private void attemptRegistration() {
        // Clear all errors
        hideError(tvNameError);
        hideError(tvEmailError);
        hideError(tvPasswordError);
        hideError(tvConfirmPasswordError);

        String name = getFieldText(etName);
        String username = getFieldText(etUsername);
        String email = getFieldText(etEmail);
        String password = getFieldText(etPassword);
        String confirmPassword = getFieldText(etConfirmPassword);

        boolean hasError = false;

        // Validate all fields required
        if (name.isEmpty()) {
            showError(tvNameError, getString(R.string.error_fields_required));
            hasError = true;
        }
        if (username.isEmpty()) {
            showUsernameError(getString(R.string.error_fields_required));
            hasError = true;
        }
        if (email.isEmpty()) {
            showError(tvEmailError, getString(R.string.error_fields_required));
            hasError = true;
        }
        if (password.isEmpty()) {
            showError(tvPasswordError, getString(R.string.error_fields_required));
            hasError = true;
        }
        if (confirmPassword.isEmpty()) {
            showError(tvConfirmPasswordError, getString(R.string.error_fields_required));
            hasError = true;
        }

        if (hasError) return;

        // Validate email format
        if (!isValidEmail(email)) {
            showError(tvEmailError, getString(R.string.error_invalid_email));
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            showError(tvPasswordError, getString(R.string.error_password_too_short));
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showError(tvConfirmPasswordError, getString(R.string.error_password_mismatch));
            return;
        }

        // Check if username was marked as taken by real-time check
        if (isUsernameTaken) {
            showUsernameError(getString(R.string.error_username_taken));
            return;
        }

        // Disable button while processing
        btnGetStarted.setEnabled(false);

        // Final username uniqueness check before registering
        userRepository.isUsernameTaken(username, new ConnectionManager.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isTaken) {
                runOnUiThread(() -> {
                    if (isTaken) {
                        showUsernameError(getString(R.string.error_username_taken));
                        btnGetStarted.setEnabled(true);
                    } else {
                        performRegistration(name, username, email, password);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(tvNameError, getString(R.string.error_registration_failed));
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
                    // Store session with name
                    sessionManager.createSession(userId.intValue(), username, name);

                    // Navigate to FitnessAssessmentActivity with name extra
                    Intent intent = new Intent(RegisterActivity.this, FitnessAssessmentActivity.class);
                    intent.putExtra("user_name", name);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    showError(tvNameError, getString(R.string.error_registration_failed));
                    btnGetStarted.setEnabled(true);
                });
            }
        });
    }

    /**
     * Validates email against recognized providers and extensions.
     * Must contain exactly one @, a recognized domain provider, and a valid extension.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;

        String lower = email.toLowerCase();

        // Must contain exactly one @
        int atIndex = lower.indexOf('@');
        if (atIndex == -1 || atIndex != lower.lastIndexOf('@')) return false;

        // Split into local and domain parts
        String localPart = lower.substring(0, atIndex);
        String domainPart = lower.substring(atIndex + 1);

        if (localPart.isEmpty() || domainPart.isEmpty()) return false;

        // Check for valid extension
        boolean hasValidExtension = false;
        String matchedExtension = null;
        for (String ext : VALID_EMAIL_EXTENSIONS) {
            if (domainPart.endsWith(ext)) {
                // Pick the longest matching extension
                if (matchedExtension == null || ext.length() > matchedExtension.length()) {
                    matchedExtension = ext;
                    hasValidExtension = true;
                }
            }
        }
        if (!hasValidExtension) return false;

        // Extract provider: domain part minus the extension
        String providerPart = domainPart.substring(0, domainPart.length() - matchedExtension.length());

        // Remove trailing dot if present (e.g., "gmail." from "gmail.com" when extension is ".com")
        // Actually the extension includes the dot, so providerPart should be just "gmail"
        // But for compound extensions like ".co.za", domain "gmail.co.za" → providerPart = "gmail"
        // Let's validate the provider
        if (providerPart.isEmpty()) return false;

        // Remove trailing dot that remains from compound domains like "hotmail.co.uk"
        // With extension ".co.uk", domain "hotmail.co.uk" → providerPart = "hotmail"
        // But "someuser@gmail.com" → extension ".com" → providerPart = "gmail"
        // That's correct already.

        return VALID_EMAIL_PROVIDERS.contains(providerPart);
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideError(TextView errorView) {
        errorView.setVisibility(View.GONE);
    }

    private void showUsernameError(String message) {
        tvUsernameStatus.setText(message);
        tvUsernameStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
        tvUsernameStatus.setVisibility(View.VISIBLE);
    }

    private String getFieldText(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    /**
     * Simplified TextWatcher that only requires afterTextChanged to be implemented.
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
