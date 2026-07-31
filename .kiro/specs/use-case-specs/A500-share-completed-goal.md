# Spec: A500 — Share Completed Goal

## Overview
After completing a goal (B222), a congratulations pop-up appears. The user 
can share their completed goal to external platforms using Android's share intent.

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into the system
- Use case B222 (Complete Goal) has been completed

## Post-Conditions
- The system exports the completed goal to the user's chosen platform
- After sharing, the system returns to the schedule page

## Goal Completion Dialog (shared with B222)
- "CONGRATULATIONS!" heading in Orbitron
- "You have completed your goal." subtext
- Goal completion animation
- Goal title (e.g. "Bench press my body weight")
- "+[X]XP" earned amount
- Share button (bottom left) → triggers share intent
- Close button (X, bottom right) → returns to schedule page

## Basic Flow
1. B222 triggers the goal completion pop-up
2. User clicks the share button
3. System captures the goal completion card as a shareable image
4. Android share sheet opens with platform options
5. User shares to chosen platform
6. System returns to the schedule page

## Files to Create
- Share logic handled within goal completion dialog
- Uses shared `ShareHelper.java` utility

## Database Operations
- SELECT g.description, g.priority FROM goals g WHERE g.goal_id = ?
- (Share itself requires no DB writes)

## Navigation
- Entry point: share button on goal completion pop-up (from B222)
- Exit point: Android share sheet → returns to schedule page (B200)

## Reference Designs
See designs/ folder — goal completion pop-up with share options.
