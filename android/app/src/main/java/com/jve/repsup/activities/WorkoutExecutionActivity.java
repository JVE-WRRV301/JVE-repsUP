package com.jve.repsup.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jve.repsup.R;

/**
 * Stub activity for workout execution.
 * Full implementation in a later task.
 */
public class WorkoutExecutionActivity extends AppCompatActivity {

    public static final String EXTRA_SCHEDULED_WORKOUT_ID = "scheduled_workout_id";
    public static final String EXTRA_WORKOUT_ID = "workout_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_execution);
    }
}
