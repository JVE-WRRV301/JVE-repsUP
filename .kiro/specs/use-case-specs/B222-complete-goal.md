# Spec: B222 — Complete Goal

## Overview
The user marks a goal as complete from the goals list. A congratulations 
pop-up appears showing the goal title and XP earned. The user can share 
the achievement (A500) or dismiss.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User has at least one active goal
- User is viewing their goals on the schedule page

## Post-Conditions
- The goal's is_comp field is set to true in the goals table
- Use case A300 (Earn XP) is called with goal XP reward
- User can invoke A500 (Share Completed Goal)

## Goal Completion Dialog
- "CONGRATULATIONS!" heading in Orbitron
- "You have completed your goal." subtext
- Goal completion animation (rotating icon)
- Goal title (e.g. "Bench press my body weight")
- "+[X]XP" earned based on priority
- Share button (bottom left) → A500
- Close button (X, bottom right) → returns to goals list

## Basic Flow
1. User taps the completion checkbox on a goal card
2. System confirms the user wants to mark the goal as complete
3. System updates the goal record (is_comp = true)
4. System displays the congratulations pop-up
5. System calls A300 (Earn XP) with goal priority-based XP
6. User clicks share → A500, or close → returns to goals tab

## XP Rewards by Priority
- Top Priority: 1000 XP
- Medium Priority: 750 XP
- Low Priority: 500 XP

## Files to Create
- `android/app/src/main/java/com/jve/repsup/dialogs/GoalCompleteDialog.java`
- `android/app/src/main/res/layout/dialog_goal_complete.xml`

## Database Operations
1. UPDATE goals SET is_comp = true WHERE goal_id = ?
2. (A300 handles XP insertion)

## Navigation
- Entry point: completion checkbox on goal card in Goals tab (B220)
- Exit point: share → A500, close → returns to Schedule Page goals tab

## Reference Designs
See designs/ folder — goal completion pop-up with share and close buttons.
