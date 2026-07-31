# Spec: A320 — Rank Up

## Overview
When a user reaches a rank-up level, their rank increases. The system displays 
a congratulations pop-up showing the new rank and bonus coins. The user can 
optionally share their achievement (A321).

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into the system
- User has levelled up (A310) and the new level is a rank-up level

## Post-Conditions
- The user's rank_id in the users table is incremented
- A rank-up dialog is displayed
- User can invoke A321 to share

## Rank System
Ranks progress through tiers and sub-tiers:
- Bronze I → Bronze II → Bronze III
- Silver I → Silver II → Silver III
- Gold I → Gold II → Gold III
- Platinum I → Platinum II → Platinum III
- Diamond I → Diamond II → Diamond III

Each rank requires reaching a specific level (stored in ranks.min_lvl_req).

## Rank-Up Dialog
- "CONGRATULATIONS!" heading in Orbitron
- "NEW RANK UNLOCKED" subheading
- Rank medal/badge image (bronze, silver, gold, platinum, diamond)
- Rank name (e.g. "BRONZE II")
- "+100 COINS" in yellow
- Share button (bottom left) → invokes A321
- Close button (X, bottom right) → dismisses dialog

## Files to Create
- `android/app/src/main/java/com/jve/repsup/dialogs/RankUpDialog.java`
- `android/app/src/main/res/layout/dialog_rank_up.xml`

## Database Operations
1. UPDATE users SET rank_id = rank_id + 1 WHERE user_id = ?
2. SELECT r.tier, r.sub_tier FROM ranks r WHERE r.rank_id = (SELECT rank_id FROM users WHERE user_id = ?)

## Navigation
- Entry point: invoked by A310 (Level Up) when level matches rank threshold
- Exit point: share button → A321 (Share Rank Up), close → dismiss dialog

## Reference Designs
See designs/ folder — rank up pop-up with medal badge and share option.
