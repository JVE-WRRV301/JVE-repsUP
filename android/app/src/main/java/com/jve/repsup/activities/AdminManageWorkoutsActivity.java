package com.jve.repsup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jve.repsup.R;
import com.jve.repsup.adapters.ManageWorkoutsAdapter;
import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Workout;
import com.jve.repsup.repositories.WorkoutRepository;

import java.util.List;

/**
 * Activity for admins to manage default workouts.
 * Displays workouts in collapsible category/difficulty sections with search filtering.
 * Provides Edit and Delete actions via a 3-dot popup menu on each workout card.
 */
public class AdminManageWorkoutsActivity extends AppCompatActivity
        implements ManageWorkoutsAdapter.WorkoutActionListener {

    public static final String EXTRA_EDIT_WORKOUT_ID = "extra_edit_workout_id";

    private EditText etSearchWorkouts;
    private RecyclerView rvWorkouts;
    private TextView tvEmptyState;

    private ManageWorkoutsAdapter adapter;
    private WorkoutRepository workoutRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_workouts);

        workoutRepository = new WorkoutRepository();

        initViews();
        setupSearch();
        loadWorkouts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload workouts when returning from edit
        loadWorkouts();
    }

    /**
     * Initializes view references and sets up the RecyclerView.
     */
    private void initViews() {
        etSearchWorkouts = findViewById(R.id.etSearchWorkouts);
        rvWorkouts = findViewById(R.id.rvWorkouts);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        adapter = new ManageWorkoutsAdapter(this);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));
        rvWorkouts.setAdapter(adapter);
    }

    /**
     * Sets up the TextWatcher on the search EditText for real-time filtering.
     */
    private void setupSearch() {
        etSearchWorkouts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Fetches all default workouts from the database and populates the adapter.
     */
    private void loadWorkouts() {
        workoutRepository.getDefaultWorkouts(new ConnectionManager.DatabaseCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> workouts) {
                runOnUiThread(() -> {
                    adapter.setWorkouts(workouts);
                    updateEmptyState();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminManageWorkoutsActivity.this,
                            getString(R.string.admin_load_workouts_error), Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
            }
        });
    }

    /**
     * Shows or hides the empty state text based on adapter content.
     */
    private void updateEmptyState() {
        if (adapter.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvWorkouts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvWorkouts.setVisibility(View.VISIBLE);
        }
    }

    // --- WorkoutActionListener implementation ---

    @Override
    public void onEditWorkout(Workout workout) {
        Intent intent = new Intent(this, AdminCreateWorkoutActivity.class);
        intent.putExtra(EXTRA_EDIT_WORKOUT_ID, workout.getWorkoutId());
        startActivity(intent);
    }

    @Override
    public void onDeleteWorkout(Workout workout) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete_workout_title)
                .setMessage(R.string.admin_delete_workout_message)
                .setPositiveButton(R.string.admin_action_delete, (dialog, which) -> {
                    workoutRepository.deleteWorkout(workout.getWorkoutId(),
                            new ConnectionManager.DatabaseCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer result) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminManageWorkoutsActivity.this,
                                                getString(R.string.admin_workout_deleted),
                                                Toast.LENGTH_SHORT).show();
                                        loadWorkouts();
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminManageWorkoutsActivity.this,
                                                getString(R.string.admin_workout_delete_error),
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton(R.string.admin_action_cancel, null)
                .show();
    }
}
