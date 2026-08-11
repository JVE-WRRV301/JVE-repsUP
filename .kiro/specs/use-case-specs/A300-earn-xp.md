# Spec: A300 — Earn XP

## Overview
When a user completes a workout or achieves a goal, the system awards them 
the appropriate amount of XP. The XP is added to their total and may trigger 
a level up (A310).

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into their account
- User has either completed a workout (B111) or completed a goal (B222)

## Post-Conditions
- The user's xp field in the users table is increased by the earned amount
- An xp_logs entry is created recording the XP event
- If the new XP total exceeds the threshold for the next level, A310 is invoked

## XP Calculation Logic
**Workout XP:**
- Base XP = sum of base_xp for each exercise in the workout
- Apply streak multiplier if active
- Formula: earned_xp = base_xp_total * streak_multiplier

**Goal XP:**
- Fixed XP reward based on goal priority:
  - Top Priority: 1000 XP
  - Medium Priority: 750 XP
  - Low Priority: 500 XP
- Apply streak multiplier if active

## Level Thresholds
- Each level requires progressively more XP
- XP bar resets to 0 after levelling up
- The amount needed increases with each level

## Files to Create
- `android/app/src/main/java/com/jve/repsup/utils/XPCalculator.java`

## Database Operations
1. SELECT base_xp FROM exercises WHERE ex_id IN (SELECT ex_id FROM workout_exercises WHERE w_id = ?)
2. SELECT multiplier FROM streaks WHERE user_id = ?
3. UPDATE users SET xp = xp + ? WHERE user_id = ?
4. INSERT INTO xp_logs (user_id, amt, src_type, src_id, xp_earned, coins_earned, timestamp) VALUES (?, ?, ?, ?, ?, 0, NOW())
5. SELECT xp, level FROM users WHERE user_id = ?

## Navigation
- Entry point: called programmatically by B111 (Complete Workout) or B222 (Complete Goal)
- Exit point: if level threshold met → invoke A310 (Level Up)

## Reference Designs
XP earned appears in workout completion summary and goal completion pop-up.
