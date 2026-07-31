package com.jve.repsup.activities;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared data holder for the fitness assessment wizard.
 * Preserves user input across all 9 steps so backward navigation retains values.
 */
public class AssessmentData {

    private String gender;
    private int age = 25;
    private double weight = 70.0;
    private boolean weightInKg = true;
    private double height = 170.0;
    private boolean heightInCm = true;
    private String bodyType;
    private String workoutLevel;
    private int workoutsPerWeek = 3;
    private List<String> fitnessGoals = new ArrayList<>();

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public boolean isWeightInKg() { return weightInKg; }
    public void setWeightInKg(boolean weightInKg) { this.weightInKg = weightInKg; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public boolean isHeightInCm() { return heightInCm; }
    public void setHeightInCm(boolean heightInCm) { this.heightInCm = heightInCm; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getWorkoutLevel() { return workoutLevel; }
    public void setWorkoutLevel(String workoutLevel) { this.workoutLevel = workoutLevel; }

    public int getWorkoutsPerWeek() { return workoutsPerWeek; }
    public void setWorkoutsPerWeek(int workoutsPerWeek) { this.workoutsPerWeek = workoutsPerWeek; }

    public List<String> getFitnessGoals() { return fitnessGoals; }
    public void setFitnessGoals(List<String> fitnessGoals) { this.fitnessGoals = fitnessGoals; }

    /**
     * Returns the weight converted to kg for database storage.
     */
    public double getWeightInKgValue() {
        if (weightInKg) {
            return weight;
        }
        return weight * 0.453592;
    }

    /**
     * Returns the height converted to cm for database storage.
     */
    public double getHeightInCmValue() {
        if (heightInCm) {
            return height;
        }
        return height * 30.48;
    }

    /**
     * Returns fitness goals as a comma-separated string for database storage.
     */
    public String getFitnessGoalsAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fitnessGoals.size(); i++) {
            sb.append(fitnessGoals.get(i));
            if (i < fitnessGoals.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
