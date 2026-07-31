# Spec: B110 — Start Workout

## Overview
The user starts their scheduled workout for the day. The system displays each 
exercise one at a time with checkboxes for each set. The user logs reps and 
weight, checks off completed sets, and progresses through all exercises.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the home page or on the workout page (My Plan tab)
- User has a workout scheduled for today

## Post-Conditions
- The user completes exercises in sequence
- After completing the final exercise, use case B111 is called

## Screen
Activity: WorkoutExecutionActivity.java

**Exercise Screen:**
- Exercise demonstration image/animation at the top (avatar performing exercise)
- Collapse arrow to minimize the demo
- Difficulty label (e.g. "Difficulty: Medium")
- Exercise name in Orbitron (e.g. "BENCH PRESS")
- Table with columns: SET | REPS | KG | ✓
  - Each row is a set with editable reps and weight fields
  - Checkbox to mark set as complete
- "ADD SET +" button to add extra sets
- "Back" button (navigate to previous exercise)
- "Next" button (enabled only when all sets are checked) → next exercise

**My Plan View (alternative entry):**
- Shows full week's workouts in a list
- Each workout card shows exercises and set count
- "START" button on today's workout
- "Completed" label on finished workouts

## UI Requirements
- Dark grey background (#393944)
- Completed sets highlighted with green row background
- Checkbox uses light blue (#B2D6FD) when checked
- "Next" button in light blue, disabled state is greyed out
- Orbitron for exercise names
- Inter for table content

## Validation Rules
- All sets must be checked before "Next" is enabled
- Reps must be > 0
- Weight must be >= 0
- User can edit reps/weight during the workout

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/WorkoutExecutionActivity.java`
- `android/app/src/main/res/layout/activity_workout_execution.xml`
- `android/app/src/main/res/layout/item_exercise_set.xml`

## Database Operations
1. SELECT sw.w_id FROM scheduled_workouts sw WHERE sw.user_id = ? AND sw.sched_date = CURDATE() AND sw.is_comp = false
2. SELECT we.*, e.* FROM workout_exercises we JOIN exercises e ON we.ex_id = e.ex_id WHERE we.w_id = ? ORDER BY we.w_ex_order_index
3. UPDATE workout_exercises SET sets = ?, reps = ? WHERE w_id = ? AND ex_id = ?
4. On completion of each set: track progress locally until workout finishes

## Navigation
- Entry point: "START" button on home page (B100) or My Plan tab
- Exit point: after final exercise completed → B111 (Complete Workout)
- Back button returns to previous exercise (not exit workout)

## Reference Designs
See designs/ folder — workout execution screen with exercise demo, set table, and navigation.
