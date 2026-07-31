package com.jve.repsup.models;

/**
 * Represents the equipped outfit slots for a user's avatar.
 * Maps to the `Outfit_Slot` table.
 */
public class OutfitSlot {

    private int outfitSlotId;
    private int userId;
    private Integer topId;
    private Integer bottomId;
    private Integer shoesId;
    private Integer accessoriesId;

    public OutfitSlot() {}

    public OutfitSlot(int outfitSlotId, int userId, Integer topId,
                      Integer bottomId, Integer shoesId, Integer accessoriesId) {
        this.outfitSlotId = outfitSlotId;
        this.userId = userId;
        this.topId = topId;
        this.bottomId = bottomId;
        this.shoesId = shoesId;
        this.accessoriesId = accessoriesId;
    }

    // Getters and Setters

    public int getOutfitSlotId() { return outfitSlotId; }
    public void setOutfitSlotId(int outfitSlotId) { this.outfitSlotId = outfitSlotId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getTopId() { return topId; }
    public void setTopId(Integer topId) { this.topId = topId; }

    public Integer getBottomId() { return bottomId; }
    public void setBottomId(Integer bottomId) { this.bottomId = bottomId; }

    public Integer getShoesId() { return shoesId; }
    public void setShoesId(Integer shoesId) { this.shoesId = shoesId; }

    public Integer getAccessoriesId() { return accessoriesId; }
    public void setAccessoriesId(Integer accessoriesId) { this.accessoriesId = accessoriesId; }
}
