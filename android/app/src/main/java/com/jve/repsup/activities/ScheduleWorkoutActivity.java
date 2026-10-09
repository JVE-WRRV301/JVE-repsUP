package com.jve.repsup.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.jve.repsup.R;
import com.jve.repsup.utils.DateFormats;
import com.jve.repsup.utils.SelectableButtonStyler;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.models.Workout;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.SessionManager;
import com.jve.repsup.utils.SnackbarUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activity for scheduling a workout on a specific date with an optional repeat interval.
 * Allows the user to pick a date, select a workout from their saved and default workouts,
 * choose a repeat interval (Never/Weekly/Biweekly/Monthly), and save.
 */
public class ScheduleWorkoutActivity extends AppCompatActivity {

    private TextInputEditText etDate;
    private Spinner spnWorkout;
    private Button btnRepeatNever;
    private Button btnRepeatWeekly;
    private Button btnRepeatBiweekly;
    private Button btnRepeatMonthly;
    private Button btnSave;
    private TextView tvError;

    private ScheduleRepository scheduleRepository;
    private WorkoutRepository workoutRepository;
    private SessionManager sessionManager;

    private Date selectedDate;
    private String selectedRepeat = "Never";
    private List<Workout> workoutList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_workout);

        scheduleRepository = new ScheduleRepository();
        workoutRepository = new WorkoutRepository();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        updateRepeatButtons();
        loadWorkouts();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        spnWorkout = findViewById(R.id.spnWorkout);
        btnRepeatNever = findViewById(R.id.btnRepeatNever);
        btnRepeatWeekly = findViewById(R.id.btnRepeatWeekly);
        btnRepeatBiweekly = findViewById(R.id.btnRepeatBiweekly);
        btnRepeatMonthly = findViewById(R.id.btnRepeatMonthly);
        btnSave = findViewById(R.id.btnSave);
        tvError = findViewById(R.id.tvError);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker());

        btnRepeatNever.setOnClickListener(v -> {
            selectedRepeat = "Never";
            updateRepeatButtons();
        });

        btnRepeatWeekly.setOnClickListener(v -> {
            selectedRepeat = "Weekly";
            updateRepeatButtons();
        });

        btnRepeatBiweekly.setOnClickListener(v -> {
            selectedRepeat = "Biweekly";
            updateRepeatButtons();
        });

        btnRepeatMonthly.setOnClickListener(v -> {
            selectedRepeat = "Monthly";
            updateRepeatButtons();
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
                    etDate.setText(DateFormats.formatLong(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Only allow today or future dates
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    /**
     * Loads user workouts and default workouts into the spinner.
     */
    private void loadWorkouts() {
        int userId = sessionManager.getUserId();

        // Load user workouts first
        workoutRepository.getUserWorkouts(userId, new ConnectionManager.DatabaseCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> userWorkouts) {
                addWorkoutsWithExercises(userWorkouts);

                // Then load default workouts
                workoutRepository.getDefaultWorkouts(new ConnectionManager.DatabaseCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> defaultWorkouts) {
                        addWorkoutsWithExercises(defaultWorkouts);
                        runOnUiThread(() -> populateSpinner());
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> populateSpinner());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Still try to load defaults
                workoutRepository.getDefaultWorkouts(new ConnectionManager.DatabaseCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> defaultWorkouts) {
                        addWorkoutsWithExercises(defaultWorkouts);
                        runOnUiThread(() -> populateSpinner());
                    }

                    @Override
                    public void onError(Exception e2) {
                        runOnUiThread(() -> showError(getString(R.string.schedule_workout_no_workouts)));
                    }
                });
            }
        });
    }

    /**
     * Adds workouts to the spinner list, skipping any that have no exercises.
     * A workout with 0 exercises can't be started (START shows "Failed to load
     * workout"), so it must not appear in the schedule dropdown.
     */
    private void addWorkoutsWithExercises(List<Workout> workouts) {
        if (workouts == null) {
            return;
        }
        for (Workout w : workouts) {
            if (w.getExercises() != null && !w.getExercises().isEmpty()) {
                workoutList.add(w);
            }
        }
    }

    /**
     * Populates the spinner with workout names from the loaded list.
     */
    private void populateSpinner() {
        if (workoutList.isEmpty()) {
            showError(getString(R.string.schedule_workout_no_workouts));
            btnSave.setEnabled(false);
            return;
        }

        List<String> workoutNames = new ArrayList<>();
        for (Workout w : workoutList) {
            workoutNames.add(w.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_workout,
                workoutNames
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_workout);
        spnWorkout.setAdapter(adapter);
    }

    private void updateRepeatButtons() {
        SelectableButtonStyler.applySelected(btnRepeatNever, "Never".equals(selectedRepeat));
        SelectableButtonStyler.applySelected(btnRepeatWeekly, "Weekly".equals(selectedRepeat));
        SelectableButtonStyler.applySelected(btnRepeatBiweekly, "Biweekly".equals(selectedRepeat));
        SelectableButtonStyler.applySelected(btnRepeatMonthly, "Monthly".equals(selectedRepeat));
    }

    /**
     * Validates inputs and schedules the workout via ScheduleRepository.
     */
    private void attemptSave() {
        tvError.setVisibility(View.GONE);

        // Validate date
        if (selectedDate == null) {
            showError(getString(R.string.schedule_workout_error_date));
            return;
        }

        // Validate workout selection
        if (workoutList.isEmpty() || spnWorkout.getSelectedItemPosition() < 0) {
            showError(getString(R.string.schedule_workout_error_workout));
            return;
        }

        // Get selected workout
        Workout selectedWorkout = workoutList.get(spnWorkout.getSelectedItemPosition());

        // Disable save button while processing
        btnSave.setEnabled(false);

        int userId = sessionManager.getUserId();

        // Build ScheduledWorkout model
        ScheduledWorkout scheduled = new ScheduledWorkout();
        scheduled.setUserId(userId);
        scheduled.setWorkoutId(selectedWorkout.getWorkoutId());
        scheduled.setScheduledDate(selectedDate);

        // Call repository to schedule with repeat logic
        scheduleRepository.scheduleWorkout(scheduled, selectedRepeat,
                new ConnectionManager.DatabaseCallback<Long>() {
                    @Override
                    public void onSuccess(Long id) {
                        runOnUiThread(() -> {
                            setResult(RESULT_OK);
                            // Show the confirmation, then close once it has been seen.
                            // A Snackbar can't display on an activity that finishes
                            // immediately, so delay finish() until after it shows.
                            SnackbarUtils.show(ScheduleWorkoutActivity.this,
                                    R.string.schedule_workout_success);
                            btnSave.postDelayed(() -> finish(), 1500);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            showError(getString(R.string.schedule_workout_error_save));
                            btnSave.setEnabled(true);
                        });
                    }
                });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
