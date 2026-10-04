package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.SnackbarUtils;

/**
 * AccountActivity lets the user change their password or delete their account.
 * Reached from SettingsActivity's "Account" row.
 *
 * Implements the Account sub-screen described in spec C113.
 */
public class AccountActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private SessionManager sessionManager;
    private UserRepository userRepository;

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        sessionManager = new SessionManager(this);
        userRepository = new UserRepository();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> onChangePassword());

        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    /**
     * Validates the password fields and submits the change. The repository verifies
     * the current password in the WHERE clause; a 0-row result means it was wrong.
     */
    private void onChangePassword() {
        String current = etCurrentPassword.getText().toString();
        String next = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(current) || TextUtils.isEmpty(next) || TextUtils.isEmpty(confirm)) {
            SnackbarUtils.show(this, R.string.account_password_empty_error);
            return;
        }
        if (next.length() < MIN_PASSWORD_LENGTH) {
            SnackbarUtils.show(this, R.string.account_password_too_short);
            return;
        }
        if (!next.equals(confirm)) {
            SnackbarUtils.show(this, R.string.account_password_mismatch);
            return;
        }

        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.changePassword(userId, current, next, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer rows) {
                runOnUiThread(() -> {
                    if (rows != null && rows > 0) {
                        SnackbarUtils.show(AccountActivity.this, R.string.account_password_changed);
                        clearPasswordFields();
                    } else {
                        // No row updated -> current password did not match.
                        DialogUtils.showError(AccountActivity.this,
                                R.string.account_password_current_wrong);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showError(AccountActivity.this,
                        R.string.account_password_change_error));
            }
        });
    }

    private void clearPasswordFields() {
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }

    /**
     * Shows an irreversible-action confirmation dialog before deleting the account.
     */
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.account_delete_confirm_title)
                .setMessage(R.string.account_delete_confirm_message)
                .setPositiveButton(R.string.account_delete, (dialog, which) -> performDeleteAccount())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Deletes the account, clears the session, and returns to the welcome screen.
     */
    private void performDeleteAccount() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.deleteAccount(userId, new DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer rows) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this,
                            R.string.account_deleted, Toast.LENGTH_LONG).show();
                    sessionManager.clearSession();
                    Intent intent = new Intent(AccountActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showError(AccountActivity.this,
                        R.string.account_delete_error));
            }
        });
    }
}
