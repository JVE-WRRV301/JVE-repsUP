# Spec: C111 — Unlock Cosmetic

## Overview
When the user taps a locked cosmetic item in the locker, a purchase dialog 
appears. If the user has enough coins, they can buy and unlock the item. 
Otherwise, an insufficient funds error is shown.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User has the locker open on the avatar page
- User taps on a locked cosmetic item

## Post-Conditions
- If purchase succeeds: cosmetic's is_unlocked is set to true, user's coins reduced
- If purchase fails: error message displayed, no changes made

## Purchase Dialog
- Cosmetic item image (large preview)
- Coin icon + price (e.g. "50")
- "Cancel" button (dark) | "Buy" button (light blue)

## Error State
- If insufficient coins: "Not enough coins!" message displayed
- Dialog remains open, user can dismiss

## Basic Flow
1. User taps a locked cosmetic item in the locker grid
2. System displays the purchase confirmation dialog with item image and price
3. System checks user's coin balance against item cost
4. If coins >= cost:
   - Deduct coins from user's balance
   - Set cosmetic's is_unlocked = true in user_cosmetics
   - Log the transaction in coin_transactions
   - Item becomes equippable in the locker
5. If coins < cost:
   - Display "Not enough coins!" error
   - No changes made

## Files to Create
- `android/app/src/main/java/com/jve/repsup/dialogs/PurchaseCosmeticDialog.java`
- `android/app/src/main/res/layout/dialog_purchase_cosmetic.xml`

## Database Operations
1. SELECT u.coins FROM users u WHERE u.user_id = ?
2. SELECT ci.coin_cost FROM cosmetic_items ci WHERE ci.cos_id = ?
3. UPDATE users SET coins = coins - ? WHERE user_id = ?
4. UPDATE user_cosmetics SET is_unlocked = true WHERE user_id = ? AND cos_id = ?
   OR INSERT INTO user_cosmetics (user_id, cos_id, is_unlocked, is_equipped) VALUES (?, ?, true, false)
5. INSERT INTO coin_transactions (user_id, amt, cos_id) VALUES (?, -?, ?)

## Navigation
- Entry point: tapping a locked item in the locker grid (C110)
- Exit point: "Buy" → item unlocked, dialog closes, locker refreshes
- Exit point: "Cancel" → dialog closes, no changes

## Reference Designs
See designs/ folder — purchase dialog with item preview and buy/cancel buttons.
