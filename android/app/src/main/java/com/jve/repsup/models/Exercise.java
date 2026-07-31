package com.jve.repsup.models;

/**
 * Represents an exercise in the library.
 * Maps to the `exercises` table.
 */
public class Exercise {

    private int exerciseId;
    private String name;
    private String muscleGroup;
    private int defaultSets;
    private int defaultReps;
    private String difficulty;
    private int baseXp;

    public Exercise() {}

    public Exercise(int exerciseId, String name, String muscleGroup,
                    int defaultSets, int defaultReps, String difficulty, int baseXp) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.defaultSets = defaultSets;
        this.defaultReps = defaultReps;
        this.difficulty = difficulty;
        this.baseXp = baseXp;
    }

    // Getters and Setters

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public int getDefaultSets() { return defaultSets; }
    public void setDefaultSets(int defaultSets) { this.defaultSets = defaultSets; }

    public int getDefaultReps() { return defaultReps; }
    public void setDefaultReps(int defaultReps) { this.defaultReps = defaultReps; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getBaseXp() { return baseXp; }
    public void setBaseXp(int baseXp) { this.baseXp = baseXp; }
}
