# Spec: B111 — Complete Workout

## Overview
After the user finishes all exercises in their workout, the system marks the 
workout as complete, displays a congratulations summary screen showing 
muscles worked and XP earned, then invokes A300 (Earn XP).

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User has started a workout (B110)
- User has completed all sets of the final exercise

## Post-Conditions
- The scheduled_workouts entry is marked as is_comp = true
- comp_date is set to today
- comp_sets and comp_reps are recorded
- earned_xp is calculated and stored
- Use case A300 (Earn XP) is called

## Completion Screen
- "GREAT WORKOUT!" heading in Orbitron
- "+[X]XP" earned amount
- "Muscles Worked" section with muscle group icons:
  - Lists each muscle group targeted (e.g. Chest, Shoulders, Triceps)
  - Each with a muscle icon
- Avatar image showing the user's character
- "Continue" button → returns to home page

## Basic Flow
1. User checks the last box on their last exercise
2. System displays "Complete Workout" button
3. User presses "Complete Workout"
4. System calculates total XP based on exercises completed
5. System updates the scheduled_workouts record
6. System displays the congratulations screen with workout summary
7. System calls A300 (Earn XP)
8. User clicks "Continue" → returns to home page

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/WorkoutCompleteActivity.java`
- `android/app/src/main/res/layout/activity_workout_complete.xml`

## Database Operations
1. UPDATE scheduled_workouts SET is_comp = true, comp_date = CURDATE(), earned_xp = ?, comp_sets = ?, comp_reps = ? WHERE user_id = ? AND w_id = ? AND sched_date = CURDATE()
2. SELECT DISTINCT e.muscle_group FROM workout_exercises we JOIN exercises e ON we.ex_id = e.ex_id WHERE we.w_id = ?
3. SELECT SUM(e.base_xp) FROM workout_exercises we JOIN exercises e ON we.ex_id = e.ex_id WHERE we.w_id = ?

## Navigation
- Entry point: called from WorkoutExecutionActivity after final exercise
- Exit point: "Continue" button → HomeFragment (B100)

## Reference Designs
See designs/ folder — workout complete screen with muscles worked list and avatar.
