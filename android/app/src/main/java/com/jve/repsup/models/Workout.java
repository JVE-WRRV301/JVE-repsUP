package com.jve.repsup.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a workout (user-created or default).
 * Maps to the `workouts` table.
 */
public class Workout {

    private int workoutId;
    private Integer userId;  // null for default workouts
    private String name;
    private boolean isDefault;
    private int orderIndex;
    private Timestamp createdAt;

    // Transient: list of exercises in this workout (populated by repository)
    private List<WorkoutExercise> exercises = new ArrayList<>();

    public Workout() {}

    public Workout(int workoutId, Integer userId, String name, boolean isDefault,
                   int orderIndex, Timestamp createdAt) {
        this.workoutId = workoutId;
        this.userId = userId;
        this.name = name;
        this.isDefault = isDefault;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getWorkoutId() { return workoutId; }
    public void setWorkoutId(int workoutId) { this.workoutId = workoutId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<WorkoutExercise> getExercises() { return exercises; }
    public void setExercises(List<WorkoutExercise> exercises) { this.exercises = exercises; }
}
