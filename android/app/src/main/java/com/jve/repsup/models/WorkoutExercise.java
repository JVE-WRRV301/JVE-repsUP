package com.jve.repsup.models;

import java.sql.Timestamp;

/**
 * Represents the link between a workout and an exercise with sets/reps configuration.
 * Maps to the `Workout_Exercise` table.
 */
public class WorkoutExercise {

    private int workoutId;
    private int exerciseId;
    private int sets;
    private int reps;
    private int orderIndex;
    private double targetWeight;
    private Timestamp updatedAt;

    // Transient: exercise details (populated by joins)
    private Exercise exercise;

    public WorkoutExercise() {}

    public WorkoutExercise(int workoutId, int exerciseId, int sets, int reps,
                           int orderIndex, Timestamp updatedAt, double targetWeight) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.reps = reps;
        this.orderIndex = orderIndex;
        this.updatedAt = updatedAt;
        this.targetWeight = targetWeight;
    }

    // Getters and Setters

    public int getWorkoutId() { return workoutId; }
    public void setWorkoutId(int workoutId) { this.workoutId = workoutId; }

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }
}
