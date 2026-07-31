package com.jve.repsup.models;

import java.sql.Date;

/**
 * Represents a weekly streak record for a user.
 * Maps to the `Streak` table.
 */
public class Streak {

    private int streakId;
    private int userId;
    private Date weekStartDate;
    private int numWorkoutsCompleted;
    private boolean metGoal;
    private double multiplier;

    public Streak() {}

    public Streak(int streakId, int userId, Date weekStartDate,
                  int numWorkoutsCompleted, boolean metGoal, double multiplier) {
        this.streakId = streakId;
        this.userId = userId;
        this.weekStartDate = weekStartDate;
        this.numWorkoutsCompleted = numWorkoutsCompleted;
        this.metGoal = metGoal;
        this.multiplier = multiplier;
    }

    // Getters and Setters

    public int getStreakId() { return streakId; }
    public void setStreakId(int streakId) { this.streakId = streakId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(Date weekStartDate) { this.weekStartDate = weekStartDate; }

    public int getNumWorkoutsCompleted() { return numWorkoutsCompleted; }
    public void setNumWorkoutsCompleted(int numWorkoutsCompleted) { this.numWorkoutsCompleted = numWorkoutsCompleted; }

    public boolean isMetGoal() { return metGoal; }
    public void setMetGoal(boolean metGoal) { this.metGoal = metGoal; }

    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
}
