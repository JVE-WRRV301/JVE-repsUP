package com.jve.repsup.utils;

/**
 * Pure utility class for coin purchase logic.
 * Encapsulates balance calculations for cosmetic item purchases.
 */
public class CoinPurchaseHelper {

    private CoinPurchaseHelper() {
        // Utility class — prevent instantiation
    }

    /**
     * Calculates the new coin balance after attempting a purchase.
     * If the user can afford the item (currentCoins >= itemCost), the cost is deducted.
     * Otherwise the balance remains unchanged.
     *
     * @param currentCoins the user's current coin balance (must be >= 0)
     * @param itemCost     the cost of the cosmetic item (must be >= 0)
     * @return the new balance after the purchase attempt
     */
    public static int calculateNewBalance(int currentCoins, int itemCost) {
        if (currentCoins >= itemCost) {
            return currentCoins - itemCost;
        }
        return currentCoins;
    }

    /**
     * Checks whether the user can afford a cosmetic item.
     *
     * @param currentCoins the user's current coin balance
     * @param itemCost     the cost of the cosmetic item
     * @return true if currentCoins >= itemCost
     */
    public static boolean canAfford(int currentCoins, int itemCost) {
        return currentCoins >= itemCost;
    }
}
