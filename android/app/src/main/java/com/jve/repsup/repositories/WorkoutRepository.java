package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository handling Workout, Workout_Exercise, and Exercise table CRUD operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class WorkoutRepository {

    /**
     * Inserts a new Exercise record into the database.
     *
     * @param exercise the Exercise object containing form data
     * @param callback receives the generated exercise ID or error
     */
    public void createExercise(Exercise exercise, ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO Exercise (exercise_name, muscle_group, difficulty, default_sets, default_reps, base_xp) VALUES (?, ?, ?, ?, ?, ?)";

        Object[] params = new Object[]{
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getDifficulty(),
                exercise.getDefaultSets(),
                exercise.getDefaultReps(),
                exercise.getBaseXp()
        };

        ConnectionManager.executeInsert(sql, params, callback);
    }

    /**
     * Retrieves all exercises for a given muscle group.
     *
     * @param muscleGroup the muscle group to filter by
     * @param callback    receives the list of exercises or error
     */
    public void getExercisesByMuscleGroup(String muscleGroup, ConnectionManager.DatabaseCallback<List<Exercise>> callback) {
        String sql = "SELECT exercise_id, exercise_name, muscle_group, default_sets, default_reps, difficulty, base_xp " +
                "FROM Exercise WHERE muscle_group = ? ORDER BY exercise_name";

        ConnectionManager.queryList(sql, new Object[]{muscleGroup}, rs -> {
            Exercise exercise = new Exercise();
            exercise.setExerciseId(rs.getInt("exercise_id"));
            exercise.setName(rs.getString("exercise_name"));
            exercise.setMuscleGroup(rs.getString("muscle_group"));
            exercise.setDefaultSets(rs.getInt("default_sets"));
            exercise.setDefaultReps(rs.getInt("default_reps"));
            exercise.setDifficulty(rs.getString("difficulty"));
            exercise.setBaseXp(rs.getInt("base_xp"));
            return exercise;
        }, callback);
    }

    /**
     * Creates a new workout with associated workout exercises in a single transaction.
     * Inserts the Workout record with is_default=TRUE and user_id=NULL,
     * then inserts all Workout_Exercise records.
     *
     * @param workout   the workout to create
     * @param exercises the list of workout exercises to associate
     * @param callback  receives the generated workout_id or error
     */
    public void createWorkout(Workout workout, List<WorkoutExercise> exercises, ConnectionManager.DatabaseCallback<Long> callback) {
        ConnectionManager.executeAsync(connection -> {
            connection.setAutoCommit(false);
            try {
                // Insert the Workout record
                String workoutSql = "INSERT INTO Workout (user_id, workout_name, is_default, workout_order_index) " +
                        "VALUES (?, ?, ?, ?)";
                long workoutId;
                try (PreparedStatement stmt = connection.prepareStatement(workoutSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setObject(1, workout.getUserId()); // null for default workouts
                    stmt.setString(2, workout.getName());
                    stmt.setBoolean(3, workout.isDefault());
                    stmt.setInt(4, workout.getOrderIndex());
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            workoutId = keys.getLong(1);
                        } else {
                            throw new java.sql.SQLException("Failed to get generated workout_id");
                        }
                    }
                }

                // Insert Workout_Exercise records
                String exerciseSql = "INSERT INTO Workout_Exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) " +
                        "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(exerciseSql)) {
                    for (int i = 0; i < exercises.size(); i++) {
                        WorkoutExercise we = exercises.get(i);
                        stmt.setLong(1, workoutId);
                        stmt.setInt(2, we.getExerciseId());
                        stmt.setInt(3, we.getSets());
                        stmt.setInt(4, we.getReps());
                        stmt.setInt(5, i + 1);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                connection.commit();
                return workoutId;
            } catch (Exception e) {
                connection.rollback();
                throw e instanceof java.sql.SQLException
                        ? (java.sql.SQLException) e
                        : new java.sql.SQLException("Transaction failed", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }, callback);
    }

    /**
     * Fetches all default workouts (is_default=TRUE) with their associated exercises joined.
     * Each Workout object will have its exercises list populated with Exercise details.
     *
     * @param callback receives the list of default workouts with exercises or error
     */
    public void getDefaultWorkouts(ConnectionManager.DatabaseCallback<List<Workout>> callback) {
        String sql = "SELECT w.workout_id, w.user_id, w.workout_name, w.is_default, w.workout_order_index, " +
                "we.exercise_id, we.num_sets, we.num_reps, we.workout_exercise_order_index, " +
                "e.exercise_name, e.muscle_group, e.default_sets, e.default_reps, e.difficulty, e.base_xp " +
                "FROM Workout w " +
                "LEFT JOIN Workout_Exercise we ON w.workout_id = we.workout_id " +
                "LEFT JOIN Exercise e ON we.exercise_id = e.exercise_id " +
                "WHERE w.is_default = TRUE " +
                "ORDER BY w.workout_name, we.workout_exercise_order_index";

        ConnectionManager.executeAsync(connection -> {
            Map<Integer, Workout> workoutMap = new LinkedHashMap<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int workoutId = rs.getInt("workout_id");

                        Workout workout = workoutMap.get(workoutId);
                        if (workout == null) {
                            workout = new Workout();
                            workout.setWorkoutId(workoutId);
                            workout.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
                            workout.setName(rs.getString("workout_name"));
                            workout.setDefault(rs.getBoolean("is_default"));
                            workout.setOrderIndex(rs.getInt("workout_order_index"));
                            workout.setExercises(new ArrayList<>());
                            workoutMap.put(workoutId, workout);
                        }

                        // Add exercise if the join produced one
                        int exerciseId = rs.getInt("exercise_id");
                        if (!rs.wasNull()) {
                            Exercise exercise = new Exercise();
                            exercise.setExerciseId(exerciseId);
                            exercise.setName(rs.getString("exercise_name"));
                            exercise.setMuscleGroup(rs.getString("muscle_group"));
                            exercise.setDefaultSets(rs.getInt("default_sets"));
                            exercise.setDefaultReps(rs.getInt("default_reps"));
                            exercise.setDifficulty(rs.getString("difficulty"));
                            exercise.setBaseXp(rs.getInt("base_xp"));

                            WorkoutExercise we = new WorkoutExercise();
                            we.setWorkoutId(workoutId);
                            we.setExerciseId(exerciseId);
                            we.setSets(rs.getInt("num_sets"));
                            we.setReps(rs.getInt("num_reps"));
                            we.setOrderIndex(rs.getInt("workout_exercise_order_index"));
                            we.setExercise(exercise);

                            workout.getExercises().add(we);
                        }
                    }
                }
            }
            return new ArrayList<>(workoutMap.values());
        }, callback);
    }

    /**
     * Deletes a workout and all its associated Workout_Exercise records.
     * Removes Workout_Exercise records first (FK constraint), then the Workout record.
     *
     * @param workoutId the ID of the workout to delete
     * @param callback  receives the number of affected rows or error
     */
    public void deleteWorkout(int workoutId, ConnectionManager.DatabaseCallback<Integer> callback) {
        ConnectionManager.executeAsync(connection -> {
            connection.setAutoCommit(false);
            try {
                // Delete associated Workout_Exercise records first
                String deleteExercisesSql = "DELETE FROM Workout_Exercise WHERE workout_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteExercisesSql)) {
                    stmt.setInt(1, workoutId);
                    stmt.executeUpdate();
                }

                // Delete the Workout record
                String deleteWorkoutSql = "DELETE FROM Workout WHERE workout_id = ?";
                int affectedRows;
                try (PreparedStatement stmt = connection.prepareStatement(deleteWorkoutSql)) {
                    stmt.setInt(1, workoutId);
                    affectedRows = stmt.executeUpdate();
                }

                connection.commit();
                return affectedRows;
            } catch (Exception e) {
                connection.rollback();
                throw e instanceof java.sql.SQLException
                        ? (java.sql.SQLException) e
                        : new java.sql.SQLException("Delete transaction failed", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }, callback);
    }
}
