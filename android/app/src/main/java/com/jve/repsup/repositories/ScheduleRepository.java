package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.ScheduledWorkout;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository handling Scheduled_Workout table CRUD operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class ScheduleRepository {

    /**
     * Fetches today's scheduled workout for a user, including workout name
     * and the first 3 exercises with their details.
     * Returns null if no workout is scheduled for today.
     */
    public void getTodaysWorkout(int userId, ConnectionManager.DatabaseCallback<ScheduledWorkout> callback) {
        String sql = "SELECT sw.scheduled_workout_id, sw.user_id, sw.workout_id, sw.scheduled_date, " +
                "sw.is_completed, sw.completed_date, sw.earned_xp, sw.completed_sets, sw.completed_reps, " +
                "w.workout_name " +
                "FROM scheduled_workout sw " +
                "JOIN workout w ON sw.workout_id = w.workout_id " +
                "WHERE sw.user_id = ? AND sw.scheduled_date = CURDATE() AND sw.is_completed = 0 " +
                "LIMIT 1";

        ConnectionManager.executeAsync(connection -> {
            ScheduledWorkout scheduledWorkout = null;

            try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        scheduledWorkout = new ScheduledWorkout();
                        scheduledWorkout.setScheduledWorkoutId(rs.getInt("scheduled_workout_id"));
                        scheduledWorkout.setUserId(rs.getInt("user_id"));
                        scheduledWorkout.setWorkoutId(rs.getInt("workout_id"));
                        scheduledWorkout.setScheduledDate(rs.getDate("scheduled_date"));
                        scheduledWorkout.setCompleted(rs.getBoolean("is_completed"));
                        scheduledWorkout.setCompletedDate(rs.getTimestamp("completed_date"));
                        scheduledWorkout.setEarnedXp(rs.getInt("earned_xp"));
                        scheduledWorkout.setCompletedSets(rs.getInt("completed_sets"));
                        scheduledWorkout.setCompletedReps(rs.getInt("completed_reps"));

                        Workout workout = new Workout();
                        workout.setWorkoutId(rs.getInt("workout_id"));
                        workout.setName(rs.getString("workout_name"));
                        scheduledWorkout.setWorkout(workout);
                    }
                }
            }

            // If we found a scheduled workout, fetch the first 3 exercises
            if (scheduledWorkout != null) {
                String exerciseSql = "SELECT we.exercise_id, we.num_sets, we.num_reps, " +
                        "we.workout_exercise_order_index, " +
                        "e.exercise_name, e.muscle_group, e.difficulty, e.base_xp " +
                        "FROM workout_exercise we " +
                        "JOIN exercise e ON we.exercise_id = e.exercise_id " +
                        "WHERE we.workout_id = ? " +
                        "ORDER BY we.workout_exercise_order_index ASC " +
                        "LIMIT 3";

                List<WorkoutExercise> exercises = new ArrayList<>();
                try (java.sql.PreparedStatement stmt2 = connection.prepareStatement(exerciseSql)) {
                    stmt2.setInt(1, scheduledWorkout.getWorkoutId());
                    try (java.sql.ResultSet rs2 = stmt2.executeQuery()) {
                        while (rs2.next()) {
                            WorkoutExercise we = new WorkoutExercise();
                            we.setWorkoutId(scheduledWorkout.getWorkoutId());
                            we.setExerciseId(rs2.getInt("exercise_id"));
                            we.setSets(rs2.getInt("num_sets"));
                            we.setReps(rs2.getInt("num_reps"));
                            we.setOrderIndex(rs2.getInt("workout_exercise_order_index"));

                            Exercise exercise = new Exercise();
                            exercise.setExerciseId(rs2.getInt("exercise_id"));
                            exercise.setName(rs2.getString("exercise_name"));
                            exercise.setMuscleGroup(rs2.getString("muscle_group"));
                            exercise.setDifficulty(rs2.getString("difficulty"));
                            exercise.setBaseXp(rs2.getInt("base_xp"));
                            we.setExercise(exercise);

                            exercises.add(we);
                        }
                    }
                }
                scheduledWorkout.getWorkout().setExercises(exercises);
            }

            return scheduledWorkout;
        }, callback);
    }
}
