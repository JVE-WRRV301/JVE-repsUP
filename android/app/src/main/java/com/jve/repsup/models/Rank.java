package com.jve.repsup.models;

/**
 * Represents a rank tier in the gamification system.
 * Maps to the `ranks` table.
 */
public class Rank {

    private int rankId;
    private String tier;      // e.g. "Bronze", "Silver", "Gold"
    private String subTier;   // e.g. "I", "II", "III"
    private int minLevelRequired;

    public Rank() {}

    public Rank(int rankId, String tier, String subTier, int minLevelRequired) {
        this.rankId = rankId;
        this.tier = tier;
        this.subTier = subTier;
        this.minLevelRequired = minLevelRequired;
    }

    // Getters and Setters

    public int getRankId() { return rankId; }
    public void setRankId(int rankId) { this.rankId = rankId; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getSubTier() { return subTier; }
    public void setSubTier(String subTier) { this.subTier = subTier; }

    public int getMinLevelRequired() { return minLevelRequired; }
    public void setMinLevelRequired(int minLevelRequired) { this.minLevelRequired = minLevelRequired; }

    /**
     * Returns the full display name, e.g. "Bronze II".
     */
    public String getDisplayName() {
        return tier + " " + subTier;
    }
}
