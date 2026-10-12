package com.jve.repsup.models;

import java.io.Serializable;

/**
 * Represents the result of completing an exercise during a workout.
 * This is a transient model used for XP calculation and is NOT mapped to a database table.
 */
public class ExerciseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int exerciseId;
    private int baseXp;
    private int setsCompleted;
    private int totalReps;
    private double totalWeight;
    // The heaviest weight used in a counted set (this is what gets logged).
    private double maxWeight;

    public ExerciseResult() {}

    public ExerciseResult(int exerciseId, int baseXp, int setsCompleted,
                          int totalReps, double totalWeight) {
        this(exerciseId, baseXp, setsCompleted, totalReps, totalWeight, 0);
    }

    public ExerciseResult(int exerciseId, int baseXp, int setsCompleted,
                          int totalReps, double totalWeight, double maxWeight) {
        this.exerciseId = exerciseId;
        this.baseXp = baseXp;
        this.setsCompleted = setsCompleted;
        this.totalReps = totalReps;
        this.totalWeight = totalWeight;
        this.maxWeight = maxWeight;
    }

    // Getters and Setters

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public int getBaseXp() { return baseXp; }
    public void setBaseXp(int baseXp) { this.baseXp = baseXp; }

    public int getSetsCompleted() { return setsCompleted; }
    public void setSetsCompleted(int setsCompleted) { this.setsCompleted = setsCompleted; }

    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int totalReps) { this.totalReps = totalReps; }

    public double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(double totalWeight) { this.totalWeight = totalWeight; }

    public double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(double maxWeight) { this.maxWeight = maxWeight; }
}
