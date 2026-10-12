package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.ExerciseResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes exercise_log: what a user actually did (sets, reps, heaviest
 * weight) for each exercise when they completed a workout. The newest row for an
 * exercise is its "last weight".
 */
public class ExerciseLogRepository {

    /**
     * Saves one log row for every exercise the user actually did (at least one
     * set done). Skipped exercises are not logged, so they never replace the last
     * weight. All rows are saved together. Returns the number of rows saved.
     */
    public void logResults(int userId, int scheduledWorkoutId, List<ExerciseResult> results,
                           ConnectionManager.DatabaseCallback<Integer> callback) {
        ConnectionManager.executeAsync(connection -> {
            String sql = "INSERT INTO exercise_log (user_id, exercise_id, scheduled_workout_id, " +
                    "sets_completed, total_reps, weight_kg) VALUES (?, ?, ?, ?, ?, ?)";
            int count = 0;
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (ExerciseResult result : results) {
                    if (result.getSetsCompleted() <= 0) continue;
                    stmt.setInt(1, userId);
                    stmt.setInt(2, result.getExerciseId());
                    if (scheduledWorkoutId > 0) {
                        stmt.setInt(3, scheduledWorkoutId);
                    } else {
                        stmt.setNull(3, Types.INTEGER);
                    }
                    stmt.setInt(4, result.getSetsCompleted());
                    stmt.setInt(5, result.getTotalReps());
                    stmt.setDouble(6, result.getMaxWeight());
                    stmt.addBatch();
                    count++;
                }
                if (count > 0) {
                    stmt.executeBatch();
                }
                connection.commit();
                return count;
            } catch (java.sql.SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }, callback);
    }

    /**
     * The most recent logged weight (kg) for each exercise this user has done,
     * keyed by exercise id. Exercises never logged are not in the map.
     */
    public void getLastWeights(int userId,
                               ConnectionManager.DatabaseCallback<Map<Integer, Double>> callback) {
        String sql = "SELECT l.exercise_id, l.weight_kg FROM exercise_log l " +
                "JOIN (SELECT exercise_id, MAX(log_id) AS newest FROM exercise_log " +
                "WHERE user_id = ? GROUP BY exercise_id) latest ON l.log_id = latest.newest";

        ConnectionManager.queryList(sql, new Object[]{userId},
                rs -> new double[]{rs.getInt("exercise_id"), rs.getDouble("weight_kg")},
                new ConnectionManager.DatabaseCallback<List<double[]>>() {
                    @Override
                    public void onSuccess(List<double[]> rows) {
                        Map<Integer, Double> weights = new HashMap<>();
                        if (rows != null) {
                            for (double[] row : rows) {
                                weights.put((int) row[0], row[1]);
                            }
                        }
                        callback.onSuccess(weights);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * What was logged for the given scheduled workouts: scheduled workout id, then
     * exercise id, to the sets / total reps / weight that were done. A workout with
     * no logs (finished before logging existed) is simply not in the map.
     */
    public void getLogsForScheduledWorkouts(List<Integer> scheduledWorkoutIds,
            ConnectionManager.DatabaseCallback<Map<Integer, Map<Integer, ExerciseResult>>> callback) {
        if (scheduledWorkoutIds == null || scheduledWorkoutIds.isEmpty()) {
            callback.onSuccess(new HashMap<>());
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < scheduledWorkoutIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ", ?");
        }
        String sql = "SELECT scheduled_workout_id, exercise_id, sets_completed, total_reps, weight_kg " +
                "FROM exercise_log WHERE scheduled_workout_id IN (" + placeholders + ") ORDER BY log_id ASC";

        ConnectionManager.executeAsync(connection -> {
            Map<Integer, Map<Integer, ExerciseResult>> logs = new HashMap<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < scheduledWorkoutIds.size(); i++) {
                    stmt.setInt(i + 1, scheduledWorkoutIds.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ExerciseResult result = new ExerciseResult();
                        result.setExerciseId(rs.getInt("exercise_id"));
                        result.setSetsCompleted(rs.getInt("sets_completed"));
                        result.setTotalReps(rs.getInt("total_reps"));
                        result.setMaxWeight(rs.getDouble("weight_kg"));
                        Map<Integer, ExerciseResult> forWorkout = logs.get(rs.getInt("scheduled_workout_id"));
                        if (forWorkout == null) {
                            forWorkout = new HashMap<>();
                            logs.put(rs.getInt("scheduled_workout_id"), forWorkout);
                        }
                        forWorkout.put(result.getExerciseId(), result);
                    }
                }
            }
            return logs;
        }, callback);
    }
}
