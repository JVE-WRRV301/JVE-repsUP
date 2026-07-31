# Spec: C500 — Manage Default Workout

## Overview
An admin can edit or delete existing default workouts from the admin landing 
page. Editing allows changes to workout name, difficulty, and exercise 
sets/reps. Deleting removes the workout from the system entirely.

## Owner
Emma Martin

## Pre-Conditions
- Admin is logged into the system
- Default workouts exist in the database

## Post-Conditions
- The updated or deleted workout is reflected in the database

## Screen
Uses Admin Landing Page + edit/delete interactions

**Three-Dot Menu on Default Workout Card:**
- Edit Workout
- Delete Workout

**Edit Default Workout Screen:**
- "DEFAULT WORKOUT" heading in Orbitron
- Workout name input (pre-filled)
- Difficulty dropdown (pre-filled)
- List of exercises in the workout:
  - Exercise name
  - Set rows: Set number | Reps input | Weight input
  - Edit icon per exercise
- "Save Workout" button

**Delete Confirmation Dialog:**
- "Are you sure you want to delete this workout?"
- "Cancel" button | "Delete" button (red)

## Basic Flows

**Flow 1 — Edit:**
1. Admin taps three-dot menu on a default workout card
2. Admin selects "Edit Workout"
3. System opens the edit screen with pre-filled workout data
4. Admin can modify: workout name, difficulty, sets/reps per exercise
5. Admin clicks "Save Workout"
6. System updates the workout in the database

**Flow 2 — Delete:**
1. Admin taps three-dot menu on a default workout card
2. Admin selects "Delete Workout"
3. System shows confirmation dialog
4. Admin clicks "Delete"
5. System removes the workout and its exercises from the database

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/AdminEditWorkoutActivity.java`
- `android/app/src/main/res/layout/activity_admin_edit_workout.xml`

## Database Operations
**Edit:**
1. UPDATE workouts SET w_name = ?, is_default = true WHERE w_id = ?
2. UPDATE workout_exercises SET sets = ?, reps = ?, updated_at = NOW() WHERE w_id = ? AND ex_id = ?

**Delete:**
1. DELETE FROM workouts WHERE w_id = ? (CASCADE deletes workout_exercises and scheduled_workouts referencing it)

## Navigation
- Entry point: three-dot menu on a default workout card on Admin Landing
- Exit point: save/delete → returns to Admin Landing with refreshed list

## Reference Designs
See designs/ folder — admin edit workout screen and delete confirmation dialog.
