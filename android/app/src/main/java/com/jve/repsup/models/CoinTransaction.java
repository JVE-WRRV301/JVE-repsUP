package com.jve.repsup.models;

/**
 * Represents a coin transaction record.
 * Maps to the `Coin_Transaction` table.
 */
public class CoinTransaction {

    private int transactionId;
    private int userId;
    private int amount;
    private Integer cosmeticId;

    public CoinTransaction() {}

    public CoinTransaction(int transactionId, int userId, int amount, Integer cosmeticId) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.cosmeticId = cosmeticId;
    }

    // Getters and Setters

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public Integer getCosmeticId() { return cosmeticId; }
    public void setCosmeticId(Integer cosmeticId) { this.cosmeticId = cosmeticId; }
}
