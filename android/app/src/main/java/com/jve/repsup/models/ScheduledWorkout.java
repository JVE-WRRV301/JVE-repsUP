package com.jve.repsup.models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a workout scheduled on a specific date.
 * Maps to the `Scheduled_Workout` table.
 */
public class ScheduledWorkout {

    private int scheduledWorkoutId;
    private int userId;
    private int workoutId;
    private Date scheduledDate;
    private boolean isCompleted;
    private Timestamp completedDate;
    private int earnedXp;
    private int completedSets;
    private int completedReps;
    // Shared by every occurrence created from one repeat; 0 when the workout
    // does not repeat (the column is NULL).
    private int seriesId;

    // Transient: workout details (populated by joins)
    private Workout workout;

    public ScheduledWorkout() {}

    public ScheduledWorkout(int scheduledWorkoutId, int userId, int workoutId, Date scheduledDate,
                            boolean isCompleted, Timestamp completedDate, int earnedXp,
                            int completedSets, int completedReps) {
        this.scheduledWorkoutId = scheduledWorkoutId;
        this.userId = userId;
        this.workoutId = workoutId;
        this.scheduledDate = scheduledDate;
        this.isCompleted = isCompleted;
        this.completedDate = completedDate;
        this.earnedXp = earnedXp;
        this.completedSets = completedSets;
        this.completedReps = completedReps;
    }

    // Getters and Setters

    public int getScheduledWorkoutId() { return scheduledWorkoutId; }
    public void setScheduledWorkoutId(int scheduledWorkoutId) { this.scheduledWorkoutId = scheduledWorkoutId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getWorkoutId() { return workoutId; }
    public void setWorkoutId(int workoutId) { this.workoutId = workoutId; }

    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Timestamp getCompletedDate() { return completedDate; }
    public void setCompletedDate(Timestamp completedDate) { this.completedDate = completedDate; }

    public int getEarnedXp() { return earnedXp; }
    public void setEarnedXp(int earnedXp) { this.earnedXp = earnedXp; }

    public int getCompletedSets() { return completedSets; }
    public void setCompletedSets(int completedSets) { this.completedSets = completedSets; }

    public int getCompletedReps() { return completedReps; }
    public void setCompletedReps(int completedReps) { this.completedReps = completedReps; }

    public int getSeriesId() { return seriesId; }
    public void setSeriesId(int seriesId) { this.seriesId = seriesId; }

    public Workout getWorkout() { return workout; }
    public void setWorkout(Workout workout) { this.workout = workout; }
}
