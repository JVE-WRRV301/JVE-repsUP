package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Goal;

import java.util.List;

/**
 * Repository handling Goal table CRUD operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class GoalRepository {

    // Goal XP rules: a goal must be at least this old before it can be completed,
    // and a user can complete at most this many goals in one day.
    public static final int MIN_GOAL_AGE_HOURS = 24;
    public static final int MAX_GOALS_PER_DAY = 3;

    // Results of checkCanComplete
    public static final int COMPLETE_OK = 0;
    public static final int COMPLETE_TOO_NEW = 1;
    public static final int COMPLETE_DAILY_LIMIT = 2;

    /**
     * Retrieves all goals for a user, ordered by deadline ascending.
     */
    public void getUserGoals(int userId, ConnectionManager.DatabaseCallback<List<Goal>> callback) {
        String sql = "SELECT goal_id, user_id, goal_title, goal_description, deadline, goal_priority, " +
                "is_completed, completed_at FROM goal WHERE user_id = ? ORDER BY deadline ASC";

        ConnectionManager.queryList(sql, new Object[]{userId}, rs -> {
            Goal goal = new Goal();
            goal.setGoalId(rs.getInt("goal_id"));
            goal.setUserId(rs.getInt("user_id"));
            goal.setTitle(rs.getString("goal_title"));
            goal.setDescription(rs.getString("goal_description"));
            goal.setDeadline(rs.getDate("deadline"));
            goal.setPriority(rs.getString("goal_priority"));
            goal.setCompleted(rs.getBoolean("is_completed"));
            goal.setCompletedAt(rs.getTimestamp("completed_at"));
            return goal;
        }, callback);
    }

    /**
     * Retrieves the nearest uncompleted goal by deadline for a user.
     * Returns null if no uncompleted goals exist.
     */
    public void getNextGoal(int userId, ConnectionManager.DatabaseCallback<Goal> callback) {
        String sql = "SELECT goal_id, user_id, goal_title, goal_description, deadline, goal_priority, " +
                "is_completed, completed_at FROM goal " +
                "WHERE user_id = ? AND is_completed = 0 ORDER BY deadline ASC LIMIT 1";

        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
            Goal goal = new Goal();
            goal.setGoalId(rs.getInt("goal_id"));
            goal.setUserId(rs.getInt("user_id"));
            goal.setTitle(rs.getString("goal_title"));
            goal.setDescription(rs.getString("goal_description"));
            goal.setDeadline(rs.getDate("deadline"));
            goal.setPriority(rs.getString("goal_priority"));
            goal.setCompleted(rs.getBoolean("is_completed"));
            goal.setCompletedAt(rs.getTimestamp("completed_at"));
            return goal;
        }, callback);
    }

    /**
     * Creates a new goal record. Returns the generated goal_id.
     */
    public void createGoal(Goal goal, ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO goal (user_id, goal_title, goal_description, deadline, goal_priority, is_completed) " +
                "VALUES (?, ?, ?, ?, ?, 0)";

        ConnectionManager.executeInsert(sql, new Object[]{
                goal.getUserId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getDeadline(),
                goal.getPriority()
        }, callback);
    }

    /**
     * Updates an existing goal by goal_id.
     * Returns the number of affected rows.
     */
    public void updateGoal(Goal goal, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE goal SET goal_title = ?, goal_description = ?, deadline = ?, goal_priority = ? " +
                "WHERE goal_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{
                goal.getTitle(),
                goal.getDescription(),
                goal.getDeadline(),
                goal.getPriority(),
                goal.getGoalId()
        }, callback);
    }

    /**
     * Checks the goal XP rules before a goal is completed. Returns COMPLETE_OK,
     * COMPLETE_TOO_NEW (goal is under 24 hours old) or COMPLETE_DAILY_LIMIT (the
     * user already completed 3 goals today). Both checks use the database clock,
     * the same one that stamps created_at and completed_at.
     */
    public void checkCanComplete(int userId, int goalId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM goal WHERE goal_id = ? AND user_id = ? " +
                "AND created_at <= DATE_SUB(NOW(), INTERVAL " + MIN_GOAL_AGE_HOURS + " HOUR)) AS old_enough, " +
                "(SELECT COUNT(*) FROM goal WHERE user_id = ? AND is_completed = 1 " +
                "AND completed_at >= CURDATE()) AS done_today";

        ConnectionManager.querySingle(sql, new Object[]{goalId, userId, userId}, rs -> {
            if (rs.getInt("old_enough") == 0) {
                return COMPLETE_TOO_NEW;
            }
            if (rs.getInt("done_today") >= MAX_GOALS_PER_DAY) {
                return COMPLETE_DAILY_LIMIT;
            }
            return COMPLETE_OK;
        }, callback);
    }

    /**
     * Marks a goal as completed by setting is_completed=1 and completed_at=NOW().
     * Only changes a goal that belongs to the user, is not already completed and
     * is at least 24 hours old, so a double tap can't award the XP twice.
     * Returns the number of affected rows (0 if the goal could not be completed).
     */
    public void completeGoal(int userId, int goalId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE goal SET is_completed = 1, completed_at = NOW() " +
                "WHERE goal_id = ? AND user_id = ? AND is_completed = 0 " +
                "AND created_at <= DATE_SUB(NOW(), INTERVAL " + MIN_GOAL_AGE_HOURS + " HOUR)";

        ConnectionManager.executeUpdate(sql, new Object[]{goalId, userId}, callback);
    }

    /**
     * Deletes a goal by goal_id.
     * Returns the number of affected rows.
     */
    public void deleteGoal(int goalId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "DELETE FROM goal WHERE goal_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{goalId}, callback);
    }
}
