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
                "WHERE sw.user_id = ? AND sw.scheduled_date = ? " +
                "ORDER BY sw.is_completed ASC " +
                "LIMIT 1";

        // Use Java's current date to avoid timezone mismatch with MySQL CURDATE()
        Date today = new Date(System.currentTimeMillis());

        ConnectionManager.executeAsync(connection -> {
            ScheduledWorkout scheduledWorkout = null;

            try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, today);
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

    /**
     * Fetches all scheduled workouts for a user in a given month (1-indexed).
     * Includes workout name for display purposes.
     */
    public void getScheduledWorkoutsForMonth(int userId, int year, int month,
                                              ConnectionManager.DatabaseCallback<List<ScheduledWorkout>> callback) {
        String sql = "SELECT sw.scheduled_workout_id, sw.user_id, sw.workout_id, sw.scheduled_date, " +
                "sw.is_completed, sw.completed_date, sw.earned_xp, sw.completed_sets, sw.completed_reps, " +
                "w.workout_name " +
                "FROM scheduled_workout sw " +
                "JOIN workout w ON sw.workout_id = w.workout_id " +
                "WHERE sw.user_id = ? AND YEAR(sw.scheduled_date) = ? AND MONTH(sw.scheduled_date) = ? " +
                "ORDER BY sw.scheduled_date ASC";

        ConnectionManager.queryList(sql, new Object[]{userId, year, month}, rs -> {
            ScheduledWorkout sw = new ScheduledWorkout();
            sw.setScheduledWorkoutId(rs.getInt("scheduled_workout_id"));
            sw.setUserId(rs.getInt("user_id"));
            sw.setWorkoutId(rs.getInt("workout_id"));
            sw.setScheduledDate(rs.getDate("scheduled_date"));
            sw.setCompleted(rs.getBoolean("is_completed"));
            sw.setCompletedDate(rs.getTimestamp("completed_date"));
            sw.setEarnedXp(rs.getInt("earned_xp"));
            sw.setCompletedSets(rs.getInt("completed_sets"));
            sw.setCompletedReps(rs.getInt("completed_reps"));

            Workout workout = new Workout();
            workout.setWorkoutId(rs.getInt("workout_id"));
            workout.setName(rs.getString("workout_name"));
            sw.setWorkout(workout);

            return sw;
        }, callback);
    }

    /**
     * Fetches all scheduled workouts for a user in a given week (starting from weekStart).
     * Includes exercises for each workout so the weekly plan cards can display them.
     */
    public void getScheduledWorkoutsForWeek(int userId, Date weekStart,
                                             ConnectionManager.DatabaseCallback<List<ScheduledWorkout>> callback) {
        String sql = "SELECT sw.scheduled_workout_id, sw.user_id, sw.workout_id, sw.scheduled_date, " +
                "sw.is_completed, sw.completed_date, sw.earned_xp, sw.completed_sets, sw.completed_reps, " +
                "w.workout_name " +
                "FROM scheduled_workout sw " +
                "JOIN workout w ON sw.workout_id = w.workout_id " +
                "WHERE sw.user_id = ? AND sw.scheduled_date >= ? AND sw.scheduled_date < DATE_ADD(?, INTERVAL 7 DAY) " +
                "ORDER BY sw.scheduled_date ASC";

        String exerciseSql = "SELECT we.exercise_id, we.num_sets, we.num_reps, " +
                "we.workout_exercise_order_index, " +
                "e.exercise_name, e.muscle_group, e.difficulty, e.base_xp " +
                "FROM workout_exercise we " +
                "JOIN exercise e ON we.exercise_id = e.exercise_id " +
                "WHERE we.workout_id = ? " +
                "ORDER BY we.workout_exercise_order_index ASC";

        ConnectionManager.executeAsync(connection -> {
            List<ScheduledWorkout> results = new ArrayList<>();

            try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, weekStart);
                stmt.setDate(3, weekStart);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ScheduledWorkout sw = new ScheduledWorkout();
                        sw.setScheduledWorkoutId(rs.getInt("scheduled_workout_id"));
                        sw.setUserId(rs.getInt("user_id"));
                        sw.setWorkoutId(rs.getInt("workout_id"));
                        sw.setScheduledDate(rs.getDate("scheduled_date"));
                        sw.setCompleted(rs.getBoolean("is_completed"));
                        sw.setCompletedDate(rs.getTimestamp("completed_date"));
                        sw.setEarnedXp(rs.getInt("earned_xp"));
                        sw.setCompletedSets(rs.getInt("completed_sets"));
                        sw.setCompletedReps(rs.getInt("completed_reps"));

                        Workout workout = new Workout();
                        workout.setWorkoutId(rs.getInt("workout_id"));
                        workout.setName(rs.getString("workout_name"));
                        sw.setWorkout(workout);

                        results.add(sw);
                    }
                }
            }

            // Fetch exercises for each scheduled workout
            for (ScheduledWorkout sw : results) {
                List<WorkoutExercise> exercises = new ArrayList<>();
                try (java.sql.PreparedStatement stmt2 = connection.prepareStatement(exerciseSql)) {
                    stmt2.setInt(1, sw.getWorkoutId());
                    try (java.sql.ResultSet rs2 = stmt2.executeQuery()) {
                        while (rs2.next()) {
                            WorkoutExercise we = new WorkoutExercise();
                            we.setWorkoutId(sw.getWorkoutId());
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
                sw.getWorkout().setExercises(exercises);
            }

            return results;
        }, callback);
    }

    /**
     * Schedules a workout on a specific date. If repeatInterval is not "Never",
     * generates future occurrences for up to 12 weeks.
     * Returns the generated scheduled_workout_id of the first insertion.
     */
    public void scheduleWorkout(ScheduledWorkout scheduled, String repeatInterval,
                                 ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO scheduled_workout (user_id, workout_id, scheduled_date, is_completed) " +
                "VALUES (?, ?, ?, 0)";

        ConnectionManager.executeAsync(connection -> {
            long firstId = -1L;

            // Insert the initial scheduled workout
            try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql,
                    java.sql.PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, scheduled.getUserId());
                stmt.setInt(2, scheduled.getWorkoutId());
                stmt.setDate(3, scheduled.getScheduledDate());
                stmt.executeUpdate();

                try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        firstId = keys.getLong(1);
                    }
                }
            }

            // Generate recurring occurrences if repeatInterval is not "Never"
            if (repeatInterval != null && !repeatInterval.equals("Never")) {
                int intervalDays;
                switch (repeatInterval) {
                    case "Weekly":
                        intervalDays = 7;
                        break;
                    case "Biweekly":
                        intervalDays = 14;
                        break;
                    case "Monthly":
                        intervalDays = 30;
                        break;
                    default:
                        intervalDays = 0;
                        break;
                }

                if (intervalDays > 0) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(scheduled.getScheduledDate());

                    // Generate up to 12 future occurrences
                    for (int i = 0; i < 12; i++) {
                        cal.add(java.util.Calendar.DAY_OF_YEAR, intervalDays);
                        Date futureDate = new Date(cal.getTimeInMillis());

                        try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setInt(1, scheduled.getUserId());
                            stmt.setInt(2, scheduled.getWorkoutId());
                            stmt.setDate(3, futureDate);
                            stmt.executeUpdate();
                        }
                    }
                }
            }

            return firstId;
        }, callback);
    }

    /**
     * Counts how many scheduled workouts for today have already been marked as completed
     * (excluding the specified scheduledWorkoutId which was just completed).
     * Used to determine if the streak has already been incremented for today.
     */
    public void countOtherCompletedToday(int userId, int excludeScheduledWorkoutId,
                                          ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "SELECT COUNT(*) AS cnt FROM scheduled_workout " +
                "WHERE user_id = ? AND scheduled_date = CURDATE() AND is_completed = 1 " +
                "AND scheduled_workout_id != ?";

        ConnectionManager.querySingle(sql, new Object[]{userId, excludeScheduledWorkoutId},
                rs -> rs.getInt("cnt"), callback);
    }

    /**
     * Marks a scheduled workout as completed with earned XP, sets, and reps.
     * Returns the number of affected rows.
     */
    public void completeScheduledWorkout(int schedId, int earnedXp, int sets, int reps,
                                          ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE scheduled_workout SET is_completed = 1, completed_date = NOW(), " +
                "earned_xp = ?, completed_sets = ?, completed_reps = ? " +
                "WHERE scheduled_workout_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{earnedXp, sets, reps, schedId}, callback);
    }

    /**
     * Checks yesterday's workout status for streak evaluation.
     * Returns a boolean array: [hadScheduledWorkout, completedYesterday].
     * If no workout was scheduled yesterday, returns [false, false].
     * If a workout was scheduled and completed, returns [true, true].
     * If a workout was scheduled but not completed, returns [true, false].
     */
    public void getYesterdaysWorkoutStatus(int userId, ConnectionManager.DatabaseCallback<boolean[]> callback) {
        String sql = "SELECT is_completed FROM scheduled_workout " +
                "WHERE user_id = ? AND scheduled_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
                "LIMIT 1";

        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
            boolean completed = rs.getBoolean("is_completed");
            return new boolean[]{true, completed};
        }, new ConnectionManager.DatabaseCallback<boolean[]>() {
            @Override
            public void onSuccess(boolean[] result) {
                if (result == null) {
                    // No workout was scheduled yesterday (rest day)
                    callback.onSuccess(new boolean[]{false, false});
                } else {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Fetches all scheduled content for a user on a specific day.
     * Includes workout name and exercises for display.
     */
    public void getScheduledContentForDay(int userId, Date date,
                                           ConnectionManager.DatabaseCallback<List<ScheduledWorkout>> callback) {
        String sql = "SELECT sw.scheduled_workout_id, sw.user_id, sw.workout_id, sw.scheduled_date, " +
                "sw.is_completed, sw.completed_date, sw.earned_xp, sw.completed_sets, sw.completed_reps, " +
                "w.workout_name " +
                "FROM scheduled_workout sw " +
                "JOIN workout w ON sw.workout_id = w.workout_id " +
                "WHERE sw.user_id = ? AND sw.scheduled_date = ? " +
                "ORDER BY sw.scheduled_workout_id ASC";

        String exerciseSql = "SELECT we.exercise_id, we.num_sets, we.num_reps, " +
                "we.workout_exercise_order_index, " +
                "e.exercise_name, e.muscle_group, e.difficulty, e.base_xp " +
                "FROM workout_exercise we " +
                "JOIN exercise e ON we.exercise_id = e.exercise_id " +
                "WHERE we.workout_id = ? " +
                "ORDER BY we.workout_exercise_order_index ASC";

        ConnectionManager.executeAsync(connection -> {
            List<ScheduledWorkout> results = new ArrayList<>();

            try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, date);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ScheduledWorkout sw = new ScheduledWorkout();
                        sw.setScheduledWorkoutId(rs.getInt("scheduled_workout_id"));
                        sw.setUserId(rs.getInt("user_id"));
                        sw.setWorkoutId(rs.getInt("workout_id"));
                        sw.setScheduledDate(rs.getDate("scheduled_date"));
                        sw.setCompleted(rs.getBoolean("is_completed"));
                        sw.setCompletedDate(rs.getTimestamp("completed_date"));
                        sw.setEarnedXp(rs.getInt("earned_xp"));
                        sw.setCompletedSets(rs.getInt("completed_sets"));
                        sw.setCompletedReps(rs.getInt("completed_reps"));

                        Workout workout = new Workout();
                        workout.setWorkoutId(rs.getInt("workout_id"));
                        workout.setName(rs.getString("workout_name"));
                        sw.setWorkout(workout);

                        results.add(sw);
                    }
                }
            }

            // Fetch exercises for each scheduled workout
            for (ScheduledWorkout sw : results) {
                List<WorkoutExercise> exercises = new ArrayList<>();
                try (java.sql.PreparedStatement stmt2 = connection.prepareStatement(exerciseSql)) {
                    stmt2.setInt(1, sw.getWorkoutId());
                    try (java.sql.ResultSet rs2 = stmt2.executeQuery()) {
                        while (rs2.next()) {
                            WorkoutExercise we = new WorkoutExercise();
                            we.setWorkoutId(sw.getWorkoutId());
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
                sw.getWorkout().setExercises(exercises);
            }

            return results;
        }, callback);
    }

    /**
     * Deletes a scheduled workout by its ID.
     */
    public void deleteScheduledWorkout(int scheduledWorkoutId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "DELETE FROM scheduled_workout WHERE scheduled_workout_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{scheduledWorkoutId}, callback);
    }
}
