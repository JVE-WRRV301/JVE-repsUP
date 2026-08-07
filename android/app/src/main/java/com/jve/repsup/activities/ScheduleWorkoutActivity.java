package com.jve.repsup.activities;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.jve.repsup.R;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.models.Workout;
import com.jve.repsup.repositories.ScheduleRepository;
import com.jve.repsup.repositories.WorkoutRepository;
import com.jve.repsup.utils.SessionManager;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedDate = new Date(selected.getTimeInMillis());
                    etDate.setText(dateFormat.format(selectedDate));
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
                if (userWorkouts != null) {
                    workoutList.addAll(userWorkouts);
                }

                // Then load default workouts
                workoutRepository.getDefaultWorkouts(new ConnectionManager.DatabaseCallback<List<Workout>>() {
                    @Override
                    public void onSuccess(List<Workout> defaultWorkouts) {
                        if (defaultWorkouts != null) {
                            workoutList.addAll(defaultWorkouts);
                        }
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
                        if (defaultWorkouts != null) {
                            workoutList.addAll(defaultWorkouts);
                        }
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
        int activeColor = ContextCompat.getColor(this, R.color.light_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.dark_grey_surface);
        int activeTextColor = ContextCompat.getColor(this, R.color.dark_text);
        int inactiveTextColor = ContextCompat.getColor(this, R.color.white);

        btnRepeatNever.setBackgroundTintList(ColorStateList.valueOf(
                "Never".equals(selectedRepeat) ? activeColor : inactiveColor));
        btnRepeatNever.setTextColor(
                "Never".equals(selectedRepeat) ? activeTextColor : inactiveTextColor);

        btnRepeatWeekly.setBackgroundTintList(ColorStateList.valueOf(
                "Weekly".equals(selectedRepeat) ? activeColor : inactiveColor));
        btnRepeatWeekly.setTextColor(
                "Weekly".equals(selectedRepeat) ? activeTextColor : inactiveTextColor);

        btnRepeatBiweekly.setBackgroundTintList(ColorStateList.valueOf(
                "Biweekly".equals(selectedRepeat) ? activeColor : inactiveColor));
        btnRepeatBiweekly.setTextColor(
                "Biweekly".equals(selectedRepeat) ? activeTextColor : inactiveTextColor);

        btnRepeatMonthly.setBackgroundTintList(ColorStateList.valueOf(
                "Monthly".equals(selectedRepeat) ? activeColor : inactiveColor));
        btnRepeatMonthly.setTextColor(
                "Monthly".equals(selectedRepeat) ? activeTextColor : inactiveTextColor);
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
                            Toast.makeText(ScheduleWorkoutActivity.this,
                                    R.string.schedule_workout_success, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
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
