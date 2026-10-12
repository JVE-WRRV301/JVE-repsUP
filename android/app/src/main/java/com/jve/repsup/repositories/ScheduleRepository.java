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
     * Fetches every workout scheduled for today, each with its workout name and
     * all of its exercises. Unfinished workouts come first, so the first item is
     * the one the Home card should offer next. Returns an empty list when nothing
     * is scheduled for today.
     */
    public void getTodaysWorkouts(int userId, ConnectionManager.DatabaseCallback<List<ScheduledWorkout>> callback) {
        // "Today" comes from the phone, not MySQL CURDATE(), so the day can't
        // disagree with the server's timezone.
        Date today = new Date(System.currentTimeMillis());
        getScheduledContentForDay(userId, today, callback, true);
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
                "sw.series_id, w.workout_name " +
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
                        sw.setSeriesId(rs.getInt("series_id"));

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
     * also creates the next 12 occurrences. Every occurrence of one repeat shares
     * a series_id (the id of the first row) so they can be deleted together later.
     * Weekly and Biweekly step by 7 and 14 days; Monthly keeps the same day of the
     * month (a repeat from the 31st lands on the last day of shorter months).
     * Returns the generated scheduled_workout_id of the first insertion.
     */
    public void scheduleWorkout(ScheduledWorkout scheduled, String repeatInterval,
                                 ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO scheduled_workout (user_id, workout_id, scheduled_date, is_completed, series_id) " +
                "VALUES (?, ?, ?, 0, ?)";

        ConnectionManager.executeAsync(connection -> {
            // Wrap the initial insert plus any recurring occurrences in one
            // transaction so a partial schedule can't be left behind.
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                boolean repeats = repeatInterval != null
                        && (repeatInterval.equals("Weekly")
                        || repeatInterval.equals("Biweekly")
                        || repeatInterval.equals("Monthly"));

                long firstId = -1L;

                // Insert the initial scheduled workout
                try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql,
                        java.sql.PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, scheduled.getUserId());
                    stmt.setInt(2, scheduled.getWorkoutId());
                    stmt.setDate(3, scheduled.getScheduledDate());
                    stmt.setNull(4, java.sql.Types.INTEGER);
                    stmt.executeUpdate();

                    try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            firstId = keys.getLong(1);
                        }
                    }
                }

                if (repeats && firstId > 0) {
                    // The first row's own id names the series.
                    try (java.sql.PreparedStatement stmt = connection.prepareStatement(
                            "UPDATE scheduled_workout SET series_id = ? WHERE scheduled_workout_id = ?")) {
                        stmt.setLong(1, firstId);
                        stmt.setLong(2, firstId);
                        stmt.executeUpdate();
                    }

                    // Work out each date from the ORIGINAL date (not from the
                    // previous occurrence), so a monthly repeat from the 31st
                    // goes 31 Jan, 28 Feb, 31 Mar instead of drifting to the 28th.
                    for (int i = 1; i <= 12; i++) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(scheduled.getScheduledDate());
                        switch (repeatInterval) {
                            case "Weekly":
                                cal.add(java.util.Calendar.DAY_OF_YEAR, 7 * i);
                                break;
                            case "Biweekly":
                                cal.add(java.util.Calendar.DAY_OF_YEAR, 14 * i);
                                break;
                            default:
                                cal.add(java.util.Calendar.MONTH, i);
                                break;
                        }
                        Date futureDate = new Date(cal.getTimeInMillis());

                        try (java.sql.PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setInt(1, scheduled.getUserId());
                            stmt.setInt(2, scheduled.getWorkoutId());
                            stmt.setDate(3, futureDate);
                            stmt.setLong(4, firstId);
                            stmt.executeUpdate();
                        }
                    }
                }

                connection.commit();
                return firstId;
            } catch (java.sql.SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }, callback);
    }

    /**
     * Counts how many scheduled workouts for today have already been marked as completed
     * (excluding the specified scheduledWorkoutId which was just completed).
     * Used to determine if the streak has already been incremented for today.
     */
    public void countOtherCompletedToday(int userId, int excludeScheduledWorkoutId, Date today,
                                          ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "SELECT COUNT(*) AS cnt FROM scheduled_workout " +
                "WHERE user_id = ? AND scheduled_date = ? AND is_completed = 1 " +
                "AND scheduled_workout_id != ?";

        // "Today" comes from the phone, not MySQL CURDATE(), so the day can't
        // disagree with the server's timezone.
        ConnectionManager.querySingle(sql, new Object[]{userId, today, excludeScheduledWorkoutId},
                rs -> rs.getInt("cnt"), callback);
    }

    /**
     * Marks a scheduled workout as completed with earned XP, sets, and reps.
     * The {@code is_completed = 0} guard makes the update idempotent: a repeat
     * call (e.g. after the complete screen is recreated on rotation) affects 0
     * rows, so XP is never awarded twice.
     * Returns the number of affected rows (1 on first completion, 0 thereafter).
     */
    public void completeScheduledWorkout(int schedId, int earnedXp, int sets, int reps,
                                          ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE scheduled_workout SET is_completed = 1, completed_date = NOW(), " +
                "earned_xp = ?, completed_sets = ?, completed_reps = ? " +
                "WHERE scheduled_workout_id = ? AND is_completed = 0";

        ConnectionManager.executeUpdate(sql, new Object[]{earnedXp, sets, reps, schedId}, callback);
    }

    /**
     * Checks yesterday's workout status for streak evaluation.
     * Returns a boolean array: [hadScheduledWorkout, completedYesterday].
     * If no workout was scheduled yesterday, returns [false, false].
     * If a workout was scheduled and completed, returns [true, true].
     * If a workout was scheduled but not completed, returns [true, false].
     */
    public void getYesterdaysWorkoutStatus(int userId, Date yesterday,
                                           ConnectionManager.DatabaseCallback<boolean[]> callback) {
        // "Yesterday" is derived from the phone's date and passed in, rather than
        // MySQL DATE_SUB(CURDATE(), ...), so it can't shift with the server's timezone.
        String sql = "SELECT is_completed FROM scheduled_workout " +
                "WHERE user_id = ? AND scheduled_date = ? " +
                "LIMIT 1";

        ConnectionManager.querySingle(sql, new Object[]{userId, yesterday}, rs -> {
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
        getScheduledContentForDay(userId, date, callback, false);
    }

    /**
     * Same as above; when unfinishedFirst is true, workouts that are not yet
     * completed are listed before completed ones.
     */
    private void getScheduledContentForDay(int userId, Date date,
                                            ConnectionManager.DatabaseCallback<List<ScheduledWorkout>> callback,
                                            boolean unfinishedFirst) {
        String order = unfinishedFirst
                ? "ORDER BY sw.is_completed ASC, sw.scheduled_workout_id ASC"
                : "ORDER BY sw.scheduled_workout_id ASC";
        String sql = "SELECT sw.scheduled_workout_id, sw.user_id, sw.workout_id, sw.scheduled_date, " +
                "sw.is_completed, sw.completed_date, sw.earned_xp, sw.completed_sets, sw.completed_reps, " +
                "w.workout_name " +
                "FROM scheduled_workout sw " +
                "JOIN workout w ON sw.workout_id = w.workout_id " +
                "WHERE sw.user_id = ? AND sw.scheduled_date = ? " +
                order;

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

    /**
     * Deletes this scheduled workout and every later, unfinished occurrence from
     * the same repeat (same series_id). Workouts already completed stay, so the
     * user's history is kept. Returns the number of rows deleted.
     */
    public void deleteScheduledWorkoutSeriesFrom(int userId, int seriesId, Date fromDate,
                                                  ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "DELETE FROM scheduled_workout " +
                "WHERE user_id = ? AND series_id = ? AND scheduled_date >= ? AND is_completed = 0";
        ConnectionManager.executeUpdate(sql, new Object[]{userId, seriesId, fromDate}, callback);
    }

    /**
     * Repoints a scheduled workout at a different workout. Used when a user edits
     * a scheduled default (or another user's) workout from My Plan: instead of
     * mutating the shared workout, a personal copy is saved and this scheduled
     * workout is pointed at that copy. Only the user's own scheduled row is
     * changed (WHERE scheduled_workout_id = ? AND user_id = ?).
     * Returns the number of affected rows.
     */
    public void updateScheduledWorkoutWorkoutId(int scheduledWorkoutId, int userId, int newWorkoutId,
                                                 ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE scheduled_workout SET workout_id = ? " +
                "WHERE scheduled_workout_id = ? AND user_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{newWorkoutId, scheduledWorkoutId, userId}, callback);
    }
}
