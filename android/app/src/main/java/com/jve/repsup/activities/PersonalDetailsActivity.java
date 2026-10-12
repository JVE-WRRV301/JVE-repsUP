package com.jve.repsup.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.models.User;
import com.jve.repsup.repositories.UserRepository;
import com.jve.repsup.utils.DialogUtils;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.SnackbarUtils;

/**
 * PersonalDetailsActivity lets the user view and edit their name, email, and username.
 * Reached from SettingsActivity's "Personal Details" row.
 *
 * Implements the Personal Details sub-screen described in spec C113.
 */
public class PersonalDetailsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private UserRepository userRepository;

    private EditText etName;
    private EditText etEmail;
    private EditText etUsername;
    private Button btnSave;

    private String originalUsername;
    private String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        // Shrink the scroll view above the keyboard so the focused field scrolls
        // into view. The root ScrollView keeps fitsSystemWindows for the
        // status/nav bars, so the IME margin is driven from its parent to avoid
        // cancelling that.
        InsetsHelper.applyImeBottomMarginViaParent(findViewById(R.id.scrollPersonalDetails));

        sessionManager = new SessionManager(this);
        userRepository = new UserRepository();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> onSave());

        loadCurrentDetails();
    }

    /**
     * Loads the current user's details from the database into the fields.
     */
    private void loadCurrentDetails() {
        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        userRepository.getUserById(userId, new DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                runOnUiThread(() -> {
                    etName.setText(user.getFirstName());
                    etEmail.setText(user.getEmail());
                    etUsername.setText(user.getUsername());
                    originalUsername = user.getUsername();
                    originalEmail = user.getEmail();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showError(PersonalDetailsActivity.this,
                        R.string.personal_details_load_error));
            }
        });
    }

    /**
     * Validates input and saves the updated details. Username and email uniqueness are
     * only checked when that value has actually changed.
     */
    private void onSave() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(username)) {
            SnackbarUtils.show(this, R.string.personal_details_empty_error);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SnackbarUtils.show(this, R.string.personal_details_email_error);
            return;
        }

        int userId = sessionManager.getUserId();
        if (userId == -1) return;

        // If username unchanged, skip its uniqueness check and go on to the email check.
        if (username.equalsIgnoreCase(originalUsername)) {
            checkEmailThenPersist(userId, name, email, username);
            return;
        }

        userRepository.isUsernameTaken(username, new DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean taken) {
                if (Boolean.TRUE.equals(taken)) {
                    runOnUiThread(() -> SnackbarUtils.show(PersonalDetailsActivity.this,
                            R.string.personal_details_username_taken));
                } else {
                    checkEmailThenPersist(userId, name, email, username);
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showError(PersonalDetailsActivity.this,
                        R.string.personal_details_save_error));
            }
        });
    }

    /**
     * Saves straight away when the email is unchanged; otherwise first checks that no
     * other account uses it, so the user gets a specific message instead of a
     * generic save error.
     */
    private void checkEmailThenPersist(int userId, String name, String email, String username) {
        if (email.equalsIgnoreCase(originalEmail)) {
            persistDetails(userId, name, email, username);
            return;
        }

        userRepository.isEmailTaken(email, new DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean taken) {
                if (Boolean.TRUE.equals(taken)) {
                    runOnUiThread(() -> SnackbarUtils.show(PersonalDetailsActivity.this,
                            R.string.personal_details_email_taken));
                } else {
                    persistDetails(userId, name, email, username);
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> DialogUtils.showError(PersonalDetailsActivity.this,
                        R.string.personal_details_save_error));
            }
        });
    }

    private void persistDetails(int userId, String name, String email, String username) {
        userRepository.updatePersonalDetails(userId, name, email, username,
                new DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer rows) {
                        runOnUiThread(() -> {
                            SnackbarUtils.show(PersonalDetailsActivity.this, R.string.personal_details_saved);
                            // Keep the session username in sync with the change.
                            sessionManager.createSession(userId, username, name);
                            findViewById(android.R.id.content).postDelayed(() -> finish(), 1500);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> DialogUtils.showError(PersonalDetailsActivity.this,
                                R.string.personal_details_save_error));
                    }
                });
    }
}
