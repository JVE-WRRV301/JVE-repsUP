package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jve.repsup.R;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.models.User;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.SnackbarUtils;

/**
 * SettingsActivity provides user profile management and app preferences.
 * Sections: User (Personal Details, Account) and Application (Measurement System).
 * Supports measurement system toggle and logout.
 *
 * Validates: Requirements 23.1, 23.2, 23.3, 23.4
 */
public class SettingsActivity extends AppCompatActivity {

    // Stored lowercase to match the database default ('metric'/'imperial').
    private static final String METRIC = "metric";
    private static final String IMPERIAL = "imperial";

    private SessionManager sessionManager;
    private UserRepository userRepository;

    private TextView btnMetric;
    private TextView btnImperial;

    private String currentMeasurementSystem = METRIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);
        userRepository = new UserRepository();

        initViews();
        loadUserSettings();
    }

    private void initViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // USER section rows
        LinearLayout llPersonalDetails = findViewById(R.id.llPersonalDetails);
        llPersonalDetails.setOnClickListener(v ->
                startActivity(new Intent(this, PersonalDetailsActivity.class)));

        LinearLayout llAccount = findViewById(R.id.llAccount);
        llAccount.setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class)));

        // Measurement system toggle
        btnMetric = findViewById(R.id.btnMetric);
        btnImperial = findViewById(R.id.btnImperial);

        btnMetric.setOnClickListener(v -> onMeasurementSystemSelected(METRIC));
        btnImperial.setOnClickListener(v -> onMeasurementSystemSelected(IMPERIAL));

        // Logout button
        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    /**
     * Loads the user's current measurement system from the database.
     */
    private void loadUserSettings() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.getUserById(userId, new DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    // Compare ignoring case: the stored value may be any casing,
                    // but IMPERIAL is only selected on an explicit imperial match.
                    if (user != null && IMPERIAL.equalsIgnoreCase(user.getMeasurementSystem())) {
                        currentMeasurementSystem = IMPERIAL;
                    } else {
                        currentMeasurementSystem = METRIC;
                    }
                    updateMeasurementToggleUI();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    currentMeasurementSystem = METRIC;
                    updateMeasurementToggleUI();
                });
            }
        });
    }

    /**
     * Handles measurement system selection toggle.
     */
    private void onMeasurementSystemSelected(String system) {
        if (system.equals(currentMeasurementSystem)) return;

        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.updateMeasurementSystem(userId, system, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer rowsAffected) {
                runOnUiThread(() -> {
                    currentMeasurementSystem = system;
                    updateMeasurementToggleUI();
                    SnackbarUtils.show(SettingsActivity.this, R.string.settings_measurement_updated);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showError(SettingsActivity.this,
                        R.string.settings_measurement_error));
            }
        });
    }

    /**
     * Updates the measurement toggle UI to highlight the selected option.
     */
    private void updateMeasurementToggleUI() {
        // Tint the rounded toggle backgrounds instead of calling setBackgroundColor,
        // which would replace the drawable with a flat (square) colour.
        // The toggle sits on a grey card, so the "on card" variant is used.
        boolean metric = METRIC.equals(currentMeasurementSystem);
        SelectableButtonStyler.applySelectedOnCard(btnMetric, metric);
        SelectableButtonStyler.applySelectedOnCard(btnImperial, !metric);
    }

    /**
     * Displays a confirmation dialog before logging out.
     */
    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_logout_confirm_title)
                .setMessage(R.string.settings_logout_confirm_message)
                .setPositiveButton(R.string.settings_logout, (dialog, which) -> performLogout())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Clears session and navigates to WelcomeActivity, clearing the entire task stack.
     */
    private void performLogout() {
        sessionManager.clearSession();

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
