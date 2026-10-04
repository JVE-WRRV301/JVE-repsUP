package com.jve.repsup.models;

/**
 * Represents a user's avatar configuration.
 * Maps to the `Avatar` table.
 */
public class Avatar {

    private int avatarId;
    private int userId;
    private String skinTone;
    private String hairColour;
    private String bodyShape;
    private String facialDetails;

    public Avatar() {}

    public Avatar(int avatarId, int userId, String skinTone, String hairColour,
                  String bodyShape, String facialDetails) {
        this.avatarId = avatarId;
        this.userId = userId;
        this.skinTone = skinTone;
        this.hairColour = hairColour;
        this.bodyShape = bodyShape;
        this.facialDetails = facialDetails;
    }

    // Getters and Setters

    public int getAvatarId() { return avatarId; }
    public void setAvatarId(int avatarId) { this.avatarId = avatarId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSkinTone() { return skinTone; }
    public void setSkinTone(String skinTone) { this.skinTone = skinTone; }

    public String getHairColour() { return hairColour; }
    public void setHairColour(String hairColour) { this.hairColour = hairColour; }

    public String getBodyShape() { return bodyShape; }
    public void setBodyShape(String bodyShape) { this.bodyShape = bodyShape; }

    public String getFacialDetails() { return facialDetails; }
    public void setFacialDetails(String facialDetails) { this.facialDetails = facialDetails; }
}
