package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Goal;

import java.util.List;

/**
 * Repository handling Goal table CRUD operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class GoalRepository {

    /**
     * Retrieves all goals for a user, ordered by deadline ascending.
     */
    public void getUserGoals(int userId, ConnectionManager.DatabaseCallback<List<Goal>> callback) {
        String sql = "SELECT goal_id, user_id, goal_description, deadline, goal_priority, " +
                "is_completed, completed_at FROM goal WHERE user_id = ? ORDER BY deadline ASC";

        ConnectionManager.queryList(sql, new Object[]{userId}, rs -> {
            Goal goal = new Goal();
            goal.setGoalId(rs.getInt("goal_id"));
            goal.setUserId(rs.getInt("user_id"));
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
        String sql = "SELECT goal_id, user_id, goal_description, deadline, goal_priority, " +
                "is_completed, completed_at FROM goal " +
                "WHERE user_id = ? AND is_completed = 0 ORDER BY deadline ASC LIMIT 1";

        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
            Goal goal = new Goal();
            goal.setGoalId(rs.getInt("goal_id"));
            goal.setUserId(rs.getInt("user_id"));
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
        String sql = "INSERT INTO goal (user_id, goal_description, deadline, goal_priority, is_completed) " +
                "VALUES (?, ?, ?, ?, 0)";

        ConnectionManager.executeInsert(sql, new Object[]{
                goal.getUserId(),
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
        String sql = "UPDATE goal SET goal_description = ?, deadline = ?, goal_priority = ? " +
                "WHERE goal_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{
                goal.getDescription(),
                goal.getDeadline(),
                goal.getPriority(),
                goal.getGoalId()
        }, callback);
    }

    /**
     * Marks a goal as completed by setting is_completed=1 and completed_at=NOW().
     * Returns the number of affected rows.
     */
    public void completeGoal(int goalId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE goal SET is_completed = 1, completed_at = NOW() WHERE goal_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{goalId}, callback);
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
