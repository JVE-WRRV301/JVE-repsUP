package com.jve.repsup.models;

/**
 * Represents a user's ownership/equip state for a cosmetic item.
 * Maps to the `User_Cosmetic` table.
 */
public class UserCosmetic {

    private int userCosmeticId;
    private int userId;
    private int cosmeticId;
    private boolean isUnlocked;
    private boolean isEquipped;

    public UserCosmetic() {}

    public UserCosmetic(int userCosmeticId, int userId, int cosmeticId,
                        boolean isUnlocked, boolean isEquipped) {
        this.userCosmeticId = userCosmeticId;
        this.userId = userId;
        this.cosmeticId = cosmeticId;
        this.isUnlocked = isUnlocked;
        this.isEquipped = isEquipped;
    }

    // Getters and Setters

    public int getUserCosmeticId() { return userCosmeticId; }
    public void setUserCosmeticId(int userCosmeticId) { this.userCosmeticId = userCosmeticId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCosmeticId() { return cosmeticId; }
    public void setCosmeticId(int cosmeticId) { this.cosmeticId = cosmeticId; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }
}
