package com.jve.repsup.models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a fitness goal set by the user.
 * Maps to the `Goal` table.
 */
public class Goal {

    private int goalId;
    private int userId;
    private String title;
    private String description;  // notes (may be null/empty)
    private Date deadline;
    private String priority;  // "Top", "Medium", "Low"
    private boolean isCompleted;
    private Timestamp completedAt;

    public Goal() {}

    public Goal(int goalId, int userId, String title, String description, Date deadline,
                String priority, boolean isCompleted, Timestamp completedAt) {
        this.goalId = goalId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }

    // Getters and Setters

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    /**
     * Returns XP reward based on priority level.
     * Matching ignores case and a null priority is treated as Medium, so values
     * saved as "top"/"medium"/"low" (or null) still award the right XP.
     */
    public int getXpReward() {
        String normalised = priority == null ? "medium" : priority.trim().toLowerCase();
        switch (normalised) {
            case "top": return 1000;
            case "medium": return 500;
            case "low": return 250;
            default: return 0;
        }
    }
}
