package com.jve.repsup.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.jve.repsup.R;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Goal;
import com.jve.repsup.repositories.GoalRepository;
import com.jve.repsup.utils.InsetsHelper;
import com.jve.repsup.utils.SessionManager;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for creating a new goal.
 * Form includes: Title, Date picker, Importance selector (Top/Medium/Low), Notes.
 * Validates Title and Date are required before saving.
 */
public class CreateGoalActivity extends AppCompatActivity {

    private TextInputEditText etGoalTitle;
    private TextInputEditText etGoalDate;
    private TextInputEditText etGoalNotes;
    private Button btnImportanceTop;
    private Button btnImportanceMedium;
    private Button btnImportanceLow;
    private Button btnSave;
    private TextView tvError;

    private GoalRepository goalRepository;
    private SessionManager sessionManager;

    private String selectedPriority = "Medium";
    private Date selectedDate;
    private int editGoalId = -1;
    private boolean isEditMode = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);

        // Keep the form clear of the keyboard when a field is focused.
        InsetsHelper.applyImeBottomPadding(findViewById(R.id.scrollCreateGoal));

        goalRepository = new GoalRepository();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        loadEditData();
        updateImportanceButtons();
    }

    /**
     * Loads goal data from intent extras when editing an existing goal.
     */
    private void loadEditData() {
        if (getIntent().hasExtra("goal_id")) {
            editGoalId = getIntent().getIntExtra("goal_id", -1);
            isEditMode = editGoalId != -1;

            if (isEditMode) {
                // Update title to "Edit Goal"
                TextView tvTitle = findViewById(R.id.tvTitle);
                tvTitle.setText(R.string.edit_goal_title);

                String title = getIntent().getStringExtra("goal_title");
                String priority = getIntent().getStringExtra("goal_priority");
                String notes = getIntent().getStringExtra("goal_notes");
                long deadlineMillis = getIntent().getLongExtra("goal_deadline", -1);

                if (title != null) {
                    etGoalTitle.setText(title);
                }
                if (priority != null) {
                    selectedPriority = priority;
                }
                if (notes != null) {
                    etGoalNotes.setText(notes);
                }
                if (deadlineMillis != -1) {
                    selectedDate = new Date(deadlineMillis);
                    etGoalDate.setText(dateFormat.format(selectedDate));
                }
            }
        }
    }

    private void initViews() {
        etGoalTitle = findViewById(R.id.etGoalTitle);
        etGoalDate = findViewById(R.id.etGoalDate);
        etGoalNotes = findViewById(R.id.etGoalNotes);
        btnImportanceTop = findViewById(R.id.btnImportanceTop);
        btnImportanceMedium = findViewById(R.id.btnImportanceMedium);
        btnImportanceLow = findViewById(R.id.btnImportanceLow);
        btnSave = findViewById(R.id.btnSave);
        tvError = findViewById(R.id.tvError);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        etGoalDate.setOnClickListener(v -> showDatePicker());

        btnImportanceTop.setOnClickListener(v -> {
            selectedPriority = "Top";
            updateImportanceButtons();
        });

        btnImportanceMedium.setOnClickListener(v -> {
            selectedPriority = "Medium";
            updateImportanceButtons();
        });

        btnImportanceLow.setOnClickListener(v -> {
            selectedPriority = "Low";
            updateImportanceButtons();
        });

        btnSave.setOnClickListener(v -> attemptSave());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTimeInMillis(selectedDate.getTime());
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    // Zero the time fields so the saved java.sql.Date is a pure
                    // date; a leftover time-of-day could shift the day across a
                    // server timezone boundary.
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    selectedDate = new Date(selected.getTimeInMillis());
                    etGoalDate.setText(dateFormat.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Only allow future dates
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateImportanceButtons() {
        SelectableButtonStyler.applySelected(btnImportanceTop, "Top".equals(selectedPriority));
        SelectableButtonStyler.applySelected(btnImportanceMedium, "Medium".equals(selectedPriority));
        SelectableButtonStyler.applySelected(btnImportanceLow, "Low".equals(selectedPriority));
    }

    private void attemptSave() {
        tvError.setVisibility(View.GONE);

        String title = etGoalTitle.getText() != null ? etGoalTitle.getText().toString().trim() : "";
        String notes = etGoalNotes.getText() != null ? etGoalNotes.getText().toString().trim() : "";

        // Validate required fields
        if (title.isEmpty()) {
            showError(getString(R.string.error_goal_title_required));
            return;
        }

        if (selectedDate == null) {
            showError(getString(R.string.error_goal_date_required));
            return;
        }

        // Title and notes are stored in separate columns. Notes may be empty, in
        // which case goal_description is stored as NULL.
        // Disable save button while processing
        btnSave.setEnabled(false);

        int userId = sessionManager.getUserId();

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setDescription(notes.isEmpty() ? null : notes);
        goal.setDeadline(selectedDate);
        goal.setPriority(selectedPriority);

        if (isEditMode) {
            goal.setGoalId(editGoalId);
            goalRepository.updateGoal(goal, new ConnectionManager.DatabaseCallback<Integer>() {
                @Override
                public void onSuccess(Integer rows) {
                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        showError(getString(R.string.error_goal_save_failed));
                        btnSave.setEnabled(true);
                    });
                }
            });
        } else {
            goalRepository.createGoal(goal, new ConnectionManager.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long goalId) {
                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        showError(getString(R.string.error_goal_save_failed));
                        btnSave.setEnabled(true);
                    });
                }
            });
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
