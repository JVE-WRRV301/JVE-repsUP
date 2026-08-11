package com.jve.repsup.models;

import java.sql.Timestamp;

/**
 * Represents a single set result logged during workout execution.
 * Maps to the `Exercise_Result` table.
 */
public class ExerciseResult {

    private int resultId;
    private int scheduledWorkoutId;
    private int exerciseId;
    private int setNumber;
    private int reps;
    private double weight;
    private Timestamp completedAt;

    public ExerciseResult() {}

    public ExerciseResult(int resultId, int scheduledWorkoutId, int exerciseId,
                          int setNumber, int reps, double weight, Timestamp completedAt) {
        this.resultId = resultId;
        this.scheduledWorkoutId = scheduledWorkoutId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.completedAt = completedAt;
    }

    // Getters and Setters

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getScheduledWorkoutId() { return scheduledWorkoutId; }
    public void setScheduledWorkoutId(int scheduledWorkoutId) { this.scheduledWorkoutId = scheduledWorkoutId; }

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}
