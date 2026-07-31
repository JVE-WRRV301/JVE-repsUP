package com.jve.repsup.models;

import java.sql.Timestamp;

/**
 * Represents an XP earning event log entry.
 * Maps to the `XP_Log` table.
 */
public class XPLog {

    private int logId;
    private int userId;
    private int amount;
    private String sourceType;  // e.g. "workout", "goal"
    private int sourceId;       // the workout_id or goal_id
    private int xpEarned;
    private int coinsEarned;
    private Timestamp timestamp;

    public XPLog() {}

    public XPLog(int logId, int userId, int amount, String sourceType, int sourceId,
                 int xpEarned, int coinsEarned, Timestamp timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.amount = amount;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.xpEarned = xpEarned;
        this.coinsEarned = coinsEarned;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public int getSourceId() { return sourceId; }
    public void setSourceId(int sourceId) { this.sourceId = sourceId; }

    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }

    public int getCoinsEarned() { return coinsEarned; }
    public void setCoinsEarned(int coinsEarned) { this.coinsEarned = coinsEarned; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
