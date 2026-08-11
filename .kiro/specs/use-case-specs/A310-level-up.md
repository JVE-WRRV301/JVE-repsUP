# Spec: A310 — Level Up

## Overview
When a user's XP exceeds the threshold for their current level, they level up.
The system increases their level, resets their XP bar, awards coins (A311), 
and checks if the new level triggers a rank up (A320).

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into their account
- User has completed one or more workouts
- Use case A300 has been invoked and XP threshold is reached

## Post-Conditions
- The user's level is incremented by 1
- The user's XP is reset (excess carries over to next level)
- Use case A311 (Earn Coins) is invoked
- If the new level is a rank-up level, A320 is invoked
- A level-up dialog is displayed to the user

## Level Up Logic
1. Check if user's current XP >= XP required for next level
2. Increment user's level by 1
3. Subtract the level threshold from current XP (carry remainder)
4. Increase the XP threshold for the next level
5. Invoke A311 to award coins
6. Check if new level matches a rank milestone → invoke A320

## Level-Up Dialog
- Animated level icon
- "LEVEL [X]" text in Orbitron font
- "+[X] COINS" in yellow (#FAD942)
- Dismiss button to close

## Files to Create
- `android/app/src/main/java/com/jve/repsup/dialogs/LevelUpDialog.java`
- `android/app/src/main/res/layout/dialog_level_up.xml`

## Database Operations
1. UPDATE users SET level = level + 1, xp = xp - ? WHERE user_id = ?
2. SELECT level FROM users WHERE user_id = ?
3. SELECT min_lvl_req FROM ranks WHERE rank_id = (SELECT rank_id FROM users WHERE user_id = ?) + 1

## Navigation
- Entry point: called programmatically by A300 when XP threshold is met
- Exit point: invokes A311 (Earn Coins), optionally A320 (Rank Up)
- Dialog displayed over current screen

## Reference Designs
See designs/ folder — level up pop-up with spinning level icon and coin reward.
