# Spec: A311 — Earn Coins

## Overview
When a user levels up, they are awarded coins. The number of coins depends 
on their level, with bonus coins awarded on rank-up levels.

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into their account
- User has just levelled up (A310 invoked)

## Post-Conditions
- The user's coins field in the users table is increased
- A coin_transactions entry is logged

## Coin Reward Logic
- Standard level up: 50 coins
- Rank-up level (every 5 levels): 100 coins
- Coins are added to the user's total balance

## Files to Create
- Logic handled within `XPCalculator.java` or a dedicated `CoinManager.java`

## Database Operations
1. UPDATE users SET coins = coins + ? WHERE user_id = ?
2. INSERT INTO coin_transactions (user_id, amt, cos_id) VALUES (?, ?, NULL)

## Navigation
- Entry point: invoked by A310 (Level Up)
- No dedicated screen — coin reward shown in the Level Up dialog
- Coins displayed in the app header (coin icon + count)

## Reference Designs
"+50 COINS" or "+100 COINS" text shown in yellow on the level-up pop-up.
