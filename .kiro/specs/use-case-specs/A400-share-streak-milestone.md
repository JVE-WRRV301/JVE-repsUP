# Spec: A400 — Share Streak Milestone

## Overview
When a user reaches a streak milestone (every 14 days), a congratulations 
pop-up appears showing the new XP multiplier. The user can share this 
achievement to external platforms.

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into their account
- User has reached a new streak milestone (14, 28, 42... days)

## Post-Conditions
- The system exports the milestone to the user's chosen platform
- XP multiplier is displayed in the pop-up

## Streak Milestone Dialog
- "CONGRATULATIONS!" heading in Orbitron
- "You haven't missed a day in [X] weeks." subtext
- "XP MULTIPLIER ACTIVATED" label
- Fire icon animation
- "[X] DAY STREAK" in large text
- "x[X]XP" multiplier value
- Share button (bottom left) → triggers share intent
- Close button (X, bottom right) → dismisses

## Basic Flow
1. A200 (Track Streak) detects a 14-day milestone
2. System displays the streak milestone dialog
3. User clicks share button
4. System captures the milestone card as a shareable image
5. Android share sheet opens with platform options
6. User shares to chosen platform

## Files to Create
- `android/app/src/main/java/com/jve/repsup/dialogs/StreakDialog.java`
- `android/app/src/main/res/layout/dialog_streak_milestone.xml`

## Database Operations
- SELECT curr_streak FROM users WHERE user_id = ?
- SELECT multiplier FROM streaks WHERE user_id = ?

## Navigation
- Entry point: triggered by A200 when streak hits a 14-day milestone
- Exit point: share button → Android share sheet, close → dismiss dialog

## Reference Designs
See designs/ folder — streak milestone pop-up with fire animation and multiplier.
