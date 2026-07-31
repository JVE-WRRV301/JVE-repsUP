package com.jve.repsup.models;

/**
 * Represents the fitness assessment profile data for a user.
 * Maps to the `user_profiles` table.
 */
public class UserProfile {

    private int profileId;
    private int userId;
    private String gender;
    private int age;
    private double weight;
    private double height;
    private String bodyType;
    private String workoutLevel;    // "never", "beginner", "intermediate", "advanced"
    private int workoutsPerWeek;
    private String fitnessGoal;     // comma-separated if multi-select

    public UserProfile() {}

    public UserProfile(int profileId, int userId, String gender, int age,
                       double weight, double height, String bodyType,
                       String workoutLevel, int workoutsPerWeek, String fitnessGoal) {
        this.profileId = profileId;
        this.userId = userId;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.bodyType = bodyType;
        this.workoutLevel = workoutLevel;
        this.workoutsPerWeek = workoutsPerWeek;
        this.fitnessGoal = fitnessGoal;
    }

    // Getters and Setters

    public int getProfileId() { return profileId; }
    public void setProfileId(int profileId) { this.profileId = profileId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getWorkoutLevel() { return workoutLevel; }
    public void setWorkoutLevel(String workoutLevel) { this.workoutLevel = workoutLevel; }

    public int getWorkoutsPerWeek() { return workoutsPerWeek; }
    public void setWorkoutsPerWeek(int workoutsPerWeek) { this.workoutsPerWeek = workoutsPerWeek; }

    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }
}
