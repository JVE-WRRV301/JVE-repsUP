package com.jve.repsup.models;

<<<<<<< HEAD
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
=======
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

    public ExerciseResult() {}

    public ExerciseResult(int exerciseId, int baseXp, int setsCompleted,
                          int totalReps, double totalWeight) {
        this.exerciseId = exerciseId;
        this.baseXp = baseXp;
        this.setsCompleted = setsCompleted;
        this.totalReps = totalReps;
        this.totalWeight = totalWeight;
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
    }

    // Getters and Setters

<<<<<<< HEAD
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
=======
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
>>>>>>> 581670146b11a91bfebf3cb43d1120e2cf8fcc8f
}
