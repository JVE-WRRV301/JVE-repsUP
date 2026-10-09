package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Exercise;
import com.jve.repsup.models.Workout;
import com.jve.repsup.models.WorkoutExercise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository handling Workout, Workout_Exercise, and Exercise table operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class WorkoutRepository {

    /**
     * Retrieves all workouts for a specific user (non-default workouts).
     */
    public void getUserWorkouts(int userId, ConnectionManager.DatabaseCallback<List<Workout>> callback) {
        String sql = "SELECT w.workout_id, w.user_id, w.workout_name, w.is_default, " +
                "w.workout_order_index FROM workout w WHERE w.user_id = ? ORDER BY w.workout_order_index ASC";

        ConnectionManager.executeAsync(connection -> {
            List<Workout> workouts = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Workout workout = mapWorkout(rs);
                        workout.setExercises(getExercisesForWorkout(connection, workout.getWorkoutId()));
                        workouts.add(workout);
                    }
                }
            }
            return workouts;
        }, callback);
    }

    /**
     * Retrieves all default workouts (admin-created).
     */
    public void getDefaultWorkouts(ConnectionManager.DatabaseCallback<List<Workout>> callback) {
        String sql = "SELECT workout_id, user_id, workout_name, is_default, " +
                "workout_order_index FROM workout WHERE is_default = 1 ORDER BY workout_order_index ASC";

        ConnectionManager.executeAsync(connection -> {
            List<Workout> workouts = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Workout workout = mapWorkout(rs);
                        workout.setExercises(getExercisesForWorkout(connection, workout.getWorkoutId()));
                        workouts.add(workout);
                    }
                }
            }
            return workouts;
        }, callback);
    }

    /**
     * Retrieves a workout with its exercises populated.
     */
    public void getWorkoutWithExercises(int workoutId, ConnectionManager.DatabaseCallback<Workout> callback) {
        ConnectionManager.executeAsync(connection -> {
            String sql = "SELECT workout_id, user_id, workout_name, is_default, " +
                    "workout_order_index FROM workout WHERE workout_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, workoutId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Workout workout = mapWorkout(rs);
                        workout.setExercises(getExercisesForWorkout(connection, workoutId));
                        return workout;
                    }
                    return null;
                }
            }
        }, callback);
    }

    /**
     * Creates a new workout with associated exercises.
     * Returns the generated workout_id.
     */
    public void createWorkout(Workout workout, List<WorkoutExercise> exercises, ConnectionManager.DatabaseCallback<Long> callback) {
        ConnectionManager.executeAsync(connection -> {
            // Insert the workout and its exercises in one transaction so a failed
            // exercise insert can't leave a workout with no exercises behind.
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String insertWorkout = "INSERT INTO workout (user_id, workout_name, is_default, workout_order_index) " +
                        "VALUES (?, ?, ?, ?)";
                long workoutId;
                try (PreparedStatement stmt = connection.prepareStatement(insertWorkout, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    if (workout.getUserId() != null) {
                        stmt.setInt(1, workout.getUserId());
                    } else {
                        stmt.setNull(1, java.sql.Types.INTEGER);
                    }
                    stmt.setString(2, workout.getName());
                    stmt.setBoolean(3, workout.isDefault());
                    stmt.setInt(4, workout.getOrderIndex());
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            workoutId = keys.getLong(1);
                        } else {
                            connection.rollback();
                            return -1L;
                        }
                    }
                }

                // Insert exercises
                String insertExercise = "INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index, target_weight) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(insertExercise)) {
                    for (WorkoutExercise we : exercises) {
                        stmt.setLong(1, workoutId);
                        stmt.setInt(2, we.getExerciseId());
                        stmt.setInt(3, we.getSets());
                        stmt.setInt(4, we.getReps());
                        stmt.setInt(5, we.getOrderIndex());
                        stmt.setDouble(6, we.getTargetWeight());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                connection.commit();
                return workoutId;
            } catch (java.sql.SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }, callback);
    }

    /**
     * Updates a workout and its exercises.
     * Returns the number of affected rows for the workout update.
     */
    public void updateWorkout(Workout workout, List<WorkoutExercise> exercises, ConnectionManager.DatabaseCallback<Integer> callback) {
        ConnectionManager.executeAsync(connection -> {
            // Update, delete-exercises and re-insert run in one transaction so a
            // failed re-insert can't leave the workout with its exercises deleted.
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                // Update workout
                String updateSql = "UPDATE workout SET workout_name = ?, is_default = ?, workout_order_index = ? " +
                        "WHERE workout_id = ?";
                int rows;
                try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                    stmt.setString(1, workout.getName());
                    stmt.setBoolean(2, workout.isDefault());
                    stmt.setInt(3, workout.getOrderIndex());
                    stmt.setInt(4, workout.getWorkoutId());
                    rows = stmt.executeUpdate();
                }

                // Delete existing exercises
                String deleteSql = "DELETE FROM workout_exercise WHERE workout_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                    stmt.setInt(1, workout.getWorkoutId());
                    stmt.executeUpdate();
                }

                // Re-insert exercises
                String insertExercise = "INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index, target_weight) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(insertExercise)) {
                    for (WorkoutExercise we : exercises) {
                        stmt.setInt(1, workout.getWorkoutId());
                        stmt.setInt(2, we.getExerciseId());
                        stmt.setInt(3, we.getSets());
                        stmt.setInt(4, we.getReps());
                        stmt.setInt(5, we.getOrderIndex());
                        stmt.setDouble(6, we.getTargetWeight());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                connection.commit();
                return rows;
            } catch (java.sql.SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }, callback);
    }

    /**
     * Duplicates a workout with a "Copy" suffix.
     * Returns the new workout_id.
     */
    public void duplicateWorkout(int workoutId, ConnectionManager.DatabaseCallback<Long> callback) {
        ConnectionManager.executeAsync(connection -> {
            // Insert the copy and its exercises in one transaction so a failed
            // exercise copy can't leave an empty duplicate behind.
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                // Get original workout
                String selectSql = "SELECT user_id, workout_name, is_default, workout_order_index FROM workout WHERE workout_id = ?";
                String workoutName;
                Integer userId = null;
                boolean isDefault;
                int orderIndex;

                try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
                    stmt.setInt(1, workoutId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return -1L;
                        }
                        userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null;
                        workoutName = rs.getString("workout_name") + " Copy";
                        isDefault = rs.getBoolean("is_default");
                        orderIndex = rs.getInt("workout_order_index");
                    }
                }

                // Insert copy
                String insertSql = "INSERT INTO workout (user_id, workout_name, is_default, workout_order_index) VALUES (?, ?, ?, ?)";
                long newId;
                try (PreparedStatement stmt = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    if (userId != null) {
                        stmt.setInt(1, userId);
                    } else {
                        stmt.setNull(1, java.sql.Types.INTEGER);
                    }
                    stmt.setString(2, workoutName);
                    stmt.setBoolean(3, isDefault);
                    stmt.setInt(4, orderIndex);
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            newId = keys.getLong(1);
                        } else {
                            connection.rollback();
                            return -1L;
                        }
                    }
                }

                // Copy exercises
                String copyExercises = "INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index, target_weight) " +
                        "SELECT ?, exercise_id, num_sets, num_reps, workout_exercise_order_index, target_weight FROM workout_exercise WHERE workout_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(copyExercises)) {
                    stmt.setLong(1, newId);
                    stmt.setInt(2, workoutId);
                    stmt.executeUpdate();
                }

                connection.commit();
                return newId;
            } catch (java.sql.SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }, callback);
    }

    /**
     * Deletes a workout and its associated exercises.
     * Returns the number of affected rows.
     */
    public void deleteWorkout(int workoutId, ConnectionManager.DatabaseCallback<Integer> callback) {
        ConnectionManager.executeAsync(connection -> {
            // Delete exercises first (FK constraint)
            String deleteExercises = "DELETE FROM workout_exercise WHERE workout_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteExercises)) {
                stmt.setInt(1, workoutId);
                stmt.executeUpdate();
            }

            // Delete workout
            String deleteWorkout = "DELETE FROM workout WHERE workout_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteWorkout)) {
                stmt.setInt(1, workoutId);
                return stmt.executeUpdate();
            }
        }, callback);
    }

    /**
     * Retrieves exercises filtered by muscle group.
     */
    public void getExercisesByMuscleGroup(String muscleGroup, ConnectionManager.DatabaseCallback<List<Exercise>> callback) {
        String sql = "SELECT exercise_id, exercise_name, muscle_group, default_sets, " +
                "default_reps, difficulty, base_xp FROM exercise WHERE muscle_group = ? " +
                "ORDER BY exercise_name ASC";

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
     * Retrieves all exercises from the library.
     */
    public void getAllExercises(ConnectionManager.DatabaseCallback<List<Exercise>> callback) {
        String sql = "SELECT exercise_id, exercise_name, muscle_group, default_sets, " +
                "default_reps, difficulty, base_xp FROM exercise ORDER BY exercise_name ASC";

        ConnectionManager.queryList(sql, new Object[]{}, rs -> {
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
     * Creates a new exercise in the library.
     * Returns the generated exercise_id.
     */
    public void createExercise(Exercise exercise, ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO exercise (exercise_name, muscle_group, default_sets, default_reps, difficulty, base_xp) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        ConnectionManager.executeInsert(sql, new Object[]{
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getDefaultSets(),
                exercise.getDefaultReps(),
                exercise.getDifficulty(),
                exercise.getBaseXp()
        }, callback);
    }

    /**
     * Searches workouts by name (partial match).
     */
    public void searchWorkoutsByName(String query, ConnectionManager.DatabaseCallback<List<Workout>> callback) {
        String sql = "SELECT workout_id, user_id, workout_name, is_default, " +
                "workout_order_index FROM workout WHERE workout_name LIKE ? ORDER BY workout_name ASC";

        ConnectionManager.queryList(sql, new Object[]{"%" + query + "%"}, rs -> {
            Workout workout = new Workout();
            workout.setWorkoutId(rs.getInt("workout_id"));
            workout.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
            workout.setName(rs.getString("workout_name"));
            workout.setDefault(rs.getBoolean("is_default"));
            workout.setOrderIndex(rs.getInt("workout_order_index"));
            return workout;
        }, callback);
    }

    // ─── Private helpers ───

    /**
     * Maps a ResultSet row to a Workout object.
     */
    private Workout mapWorkout(ResultSet rs) throws java.sql.SQLException {
        Workout workout = new Workout();
        workout.setWorkoutId(rs.getInt("workout_id"));
        workout.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        workout.setName(rs.getString("workout_name"));
        workout.setDefault(rs.getBoolean("is_default"));
        workout.setOrderIndex(rs.getInt("workout_order_index"));
        return workout;
    }

    /**
     * Retrieves all exercises for a workout with exercise details populated.
     */
    private List<WorkoutExercise> getExercisesForWorkout(Connection connection, int workoutId) throws java.sql.SQLException {
        String sql = "SELECT we.workout_id, we.exercise_id, we.num_sets, we.num_reps, " +
                "we.workout_exercise_order_index, we.target_weight, " +
                "e.exercise_name, e.muscle_group, e.default_sets, e.default_reps, e.difficulty, e.base_xp " +
                "FROM workout_exercise we " +
                "JOIN exercise e ON we.exercise_id = e.exercise_id " +
                "WHERE we.workout_id = ? ORDER BY we.workout_exercise_order_index ASC";

        List<WorkoutExercise> exercises = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, workoutId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WorkoutExercise we = new WorkoutExercise();
                    we.setWorkoutId(rs.getInt("workout_id"));
                    we.setExerciseId(rs.getInt("exercise_id"));
                    we.setSets(rs.getInt("num_sets"));
                    we.setReps(rs.getInt("num_reps"));
                    we.setOrderIndex(rs.getInt("workout_exercise_order_index"));
                    we.setTargetWeight(rs.getDouble("target_weight"));

                    Exercise exercise = new Exercise();
                    exercise.setExerciseId(rs.getInt("exercise_id"));
                    exercise.setName(rs.getString("exercise_name"));
                    exercise.setMuscleGroup(rs.getString("muscle_group"));
                    exercise.setDefaultSets(rs.getInt("default_sets"));
                    exercise.setDefaultReps(rs.getInt("default_reps"));
                    exercise.setDifficulty(rs.getString("difficulty"));
                    exercise.setBaseXp(rs.getInt("base_xp"));
                    we.setExercise(exercise);

                    exercises.add(we);
                }
            }
        }
        return exercises;
    }
}
