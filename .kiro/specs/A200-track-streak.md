# Spec: A200 — Track Streak

## Overview
A background process that evaluates the user's workout completion at the end 
of each day. If the user completed their scheduled workout or it was a rest day, 
the streak increments. If they missed a scheduled workout, the streak resets to 0.

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into their account
- The local time reaches 12:00 AM (midnight)

## Post-Conditions
- The user's curr_streak in the users table is either incremented by 1 or reset to 0
- If streak reaches a 2-week milestone, the multiplier in the streaks table is updated
- If streak is broken, the multiplier resets

## Logic
This is a time-triggered system process (not a UI screen):

1. At midnight, check if the user had a scheduled workout for that day
2. If YES and workout is_comp = true → increment curr_streak by 1
3. If YES and workout is_comp = false → reset curr_streak to 0, reset multiplier
4. If NO scheduled workout (rest day) → increment curr_streak by 1
5. If curr_streak reaches 14 (2 weeks) → activate XP multiplier (1.5x)
6. Every additional 14 days → increase multiplier further

## Streak Milestone Rules
- 14 days: multiplier = 1.5x
- 28 days: multiplier = 2.0x
- 42 days: multiplier = 2.5x
- Pattern: every 14 days adds 0.5x
- Missing a scheduled workout resets multiplier to 1.0x

## Files to Create
- `android/app/src/main/java/com/jve/repsup/utils/StreakManager.java`

## Database Operations
1. SELECT sched_date, is_comp FROM scheduled_workouts WHERE user_id = ? AND sched_date = CURDATE()
2. UPDATE users SET curr_streak = curr_streak + 1 WHERE user_id = ?
   OR UPDATE users SET curr_streak = 0 WHERE user_id = ?
3. UPDATE streaks SET multiplier = ? WHERE user_id = ?
4. INSERT/UPDATE streaks (user_id, week_st_date, num_w_comp, met_goal, multiplier)

## Navigation
- No dedicated screen — this logic runs in the background
- Streak value is displayed on the Home Page (B100)
- Milestone pop-ups trigger A400 (Share Streak Milestone)

## Reference Designs
Streak display appears on the home page header (fire icon + number).
