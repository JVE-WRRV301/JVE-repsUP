package com.jve.repsup;

import com.jve.repsup.utils.CoinPurchaseHelper;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Coin Balance Invariant on Cosmetic Purchase (Property 7).
 * Verifies that {@link CoinPurchaseHelper#calculateNewBalance(int, int)} correctly
 * deducts cost when affordable and leaves balance unchanged otherwise.
 *
 * Uses jqwik to generate arbitrary coin balances and item costs.
 *
 * Validates: Requirements 22.2, 22.3
 */
class CoinBalancePropertyTest {

    // ─── Property 7a: When coins >= cost, new balance = previous - cost ───

    /**
     * Property 7a: For any user with coins >= item cost, new balance equals
     * previous balance minus the item's coin cost.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Property(tries = 200)
    void sufficientCoinsDeductsCost(
            @ForAll("coinBalances") int currentCoins,
            @ForAll("itemCosts") int itemCost
    ) {
        // Only test cases where user can afford the item
        Assume.that(currentCoins >= itemCost);

        int newBalance = CoinPurchaseHelper.calculateNewBalance(currentCoins, itemCost);
        assertEquals(currentCoins - itemCost, newBalance,
                "When coins (" + currentCoins + ") >= cost (" + itemCost +
                        "), new balance must equal previous - cost");
    }

    // ─── Property 7b: When coins < cost, balance remains unchanged ───

    /**
     * Property 7b: For any user with coins < item cost, the balance remains unchanged.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Property(tries = 200)
    void insufficientCoinsLeavesBalanceUnchanged(
            @ForAll("coinBalances") int currentCoins,
            @ForAll("itemCosts") int itemCost
    ) {
        // Only test cases where user cannot afford the item
        Assume.that(currentCoins < itemCost);

        int newBalance = CoinPurchaseHelper.calculateNewBalance(currentCoins, itemCost);
        assertEquals(currentCoins, newBalance,
                "When coins (" + currentCoins + ") < cost (" + itemCost +
                        "), balance must remain unchanged");
    }

    // ─── Property 7c: Balance is always non-negative ───

    /**
     * Property 7c: For any valid inputs (coins >= 0, cost >= 0),
     * the resulting balance is always non-negative.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Property(tries = 200)
    void balanceIsAlwaysNonNegative(
            @ForAll("coinBalances") int currentCoins,
            @ForAll("itemCosts") int itemCost
    ) {
        int newBalance = CoinPurchaseHelper.calculateNewBalance(currentCoins, itemCost);
        assertTrue(newBalance >= 0,
                "Balance must never be negative. Got " + newBalance +
                        " for coins=" + currentCoins + ", cost=" + itemCost);
    }

    // ─── Property 7d: Exact boundary — coins == cost yields 0 ───

    /**
     * Property 7d: When coins exactly equal cost, new balance is exactly 0.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Property(tries = 200)
    void exactBoundaryYieldsZeroBalance(
            @ForAll("coinBalances") int amount
    ) {
        // coins == cost scenario
        int newBalance = CoinPurchaseHelper.calculateNewBalance(amount, amount);
        assertEquals(0, newBalance,
                "When coins (" + amount + ") == cost (" + amount +
                        "), new balance must be exactly 0");
    }

    // ─── Edge Cases ───

    /**
     * Edge case: 0 coins and 0 cost — purchase succeeds, balance stays 0.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Example
    void zeroCoinsZeroCostSucceeds() {
        int newBalance = CoinPurchaseHelper.calculateNewBalance(0, 0);
        assertEquals(0, newBalance, "0 coins with 0 cost should yield 0 balance");
        assertTrue(CoinPurchaseHelper.canAfford(0, 0),
                "User with 0 coins should afford a 0-cost item");
    }

    /**
     * Edge case: 0 coins with positive cost — purchase blocked.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Example
    void zeroCoinsPositiveCostBlocked() {
        int newBalance = CoinPurchaseHelper.calculateNewBalance(0, 100);
        assertEquals(0, newBalance, "0 coins with cost=100 should keep balance at 0");
        assertFalse(CoinPurchaseHelper.canAfford(0, 100),
                "User with 0 coins cannot afford a 100-cost item");
    }

    /**
     * Edge case: Large values — ensure no overflow for typical game ranges.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Example
    void largeValuesHandledCorrectly() {
        int coins = 1_000_000;
        int cost = 999_999;
        int newBalance = CoinPurchaseHelper.calculateNewBalance(coins, cost);
        assertEquals(1, newBalance, "1000000 - 999999 should be 1");
        assertTrue(CoinPurchaseHelper.canAfford(coins, cost));
    }

    /**
     * Edge case: canAfford consistency — canAfford matches calculateNewBalance behavior.
     *
     * Validates: Requirements 22.2, 22.3
     */
    @Property(tries = 200)
    void canAffordConsistentWithCalculation(
            @ForAll("coinBalances") int currentCoins,
            @ForAll("itemCosts") int itemCost
    ) {
        boolean affordable = CoinPurchaseHelper.canAfford(currentCoins, itemCost);
        int newBalance = CoinPurchaseHelper.calculateNewBalance(currentCoins, itemCost);

        if (affordable) {
            assertEquals(currentCoins - itemCost, newBalance,
                    "canAfford=true means balance should be deducted");
        } else {
            assertEquals(currentCoins, newBalance,
                    "canAfford=false means balance should be unchanged");
        }
    }

    // ─── Custom Providers ───

    @Provide
    Arbitrary<Integer> coinBalances() {
        return Arbitraries.integers().between(0, 1_000_000);
    }

    @Provide
    Arbitrary<Integer> itemCosts() {
        return Arbitraries.integers().between(0, 1_000_000);
    }
}
