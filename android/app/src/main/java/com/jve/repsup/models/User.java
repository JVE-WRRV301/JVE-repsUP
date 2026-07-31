package com.jve.repsup.models;

import java.sql.Timestamp;

/**
 * Represents a user in the Reps Up app.
 * Maps to the `users` table.
 */
public class User {

    private int userId;
    private String name;
    private String email;
    private String username;
    private String password;
    private int xp;
    private int coins;
    private int level;
    private Integer rankId;
    private int currentStreak;
    private Integer avatarId;
    private String measurementSystem;
    private String language;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String name, String email, String username, String password,
                int xp, int coins, int level, Integer rankId, int currentStreak,
                Integer avatarId, String measurementSystem, String language, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.xp = xp;
        this.coins = coins;
        this.level = level;
        this.rankId = rankId;
        this.currentStreak = currentStreak;
        this.avatarId = avatarId;
        this.measurementSystem = measurementSystem;
        this.language = language;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public Integer getRankId() { return rankId; }
    public void setRankId(Integer rankId) { this.rankId = rankId; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public Integer getAvatarId() { return avatarId; }
    public void setAvatarId(Integer avatarId) { this.avatarId = avatarId; }

    public String getMeasurementSystem() { return measurementSystem; }
    public void setMeasurementSystem(String measurementSystem) { this.measurementSystem = measurementSystem; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
