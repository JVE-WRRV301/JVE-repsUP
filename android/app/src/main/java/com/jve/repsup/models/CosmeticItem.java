package com.jve.repsup.models;

/**
 * Represents a cosmetic item available for avatar customization.
 * Maps to the `Cosmetic_Item` table.
 */
public class CosmeticItem {

    private int cosmeticId;
    private String name;
    private String type;        // e.g. "top", "bottom", "shoes", "hair", "skin"
    private String unlockType;  // e.g. "default", "coin", "level"
    private int levelRequired;
    private int coinCost;
    private String imageData;   // path or reference to the cosmetic image

    public CosmeticItem() {}

    public CosmeticItem(int cosmeticId, String name, String type, String unlockType,
                        int levelRequired, int coinCost, String imageData) {
        this.cosmeticId = cosmeticId;
        this.name = name;
        this.type = type;
        this.unlockType = unlockType;
        this.levelRequired = levelRequired;
        this.coinCost = coinCost;
        this.imageData = imageData;
    }

    // Getters and Setters

    public int getCosmeticId() { return cosmeticId; }
    public void setCosmeticId(int cosmeticId) { this.cosmeticId = cosmeticId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUnlockType() { return unlockType; }
    public void setUnlockType(String unlockType) { this.unlockType = unlockType; }

    public int getLevelRequired() { return levelRequired; }
    public void setLevelRequired(int levelRequired) { this.levelRequired = levelRequired; }

    public int getCoinCost() { return coinCost; }
    public void setCoinCost(int coinCost) { this.coinCost = coinCost; }

    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }
}
