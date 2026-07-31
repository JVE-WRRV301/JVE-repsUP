# Spec: C220 — Manage Workout

## Overview
The user manages their saved workouts through a three-dot menu that provides 
options to share, edit, duplicate, reorder, or delete a workout.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User has saved a workout to their workout library
- User is viewing the Saved tab on the workout page

## Post-Conditions
- The chosen action (share/edit/duplicate/reorder/delete) is completed

## Three-Dot Menu Options
- Share Workout
- Edit Workout
- Duplicate Workout
- Reorder Workouts
- Delete Workout

## Basic Flows

**Flow 1 — Share Workout:**
1. User clicks "Share Workout"
2. User chooses their preferred sharing platform
3. System exports the workout into a shareable format (workout name + exercise list)
4. Android share intent opens

**Flow 2 — Edit Workout:**
1. User clicks "Edit Workout"
2. System opens the workout configuration screen (similar to create) with pre-filled data
3. "YOUR WORKOUT" heading with workout name pre-filled
4. All exercises shown with current sets/reps/weight
5. User can add/remove exercises, change sets/reps/weight
6. User clicks "Save Workout" to update

**Flow 3 — Duplicate Workout:**
1. User clicks "Duplicate Workout"
2. System copies the workout and all its exercises
3. New workout saved as "Copy of [workout name]"
4. Workout list refreshes with the duplicate added

**Flow 4 — Reorder Workouts:**
1. User clicks "Reorder Workouts"
2. System opens a reorder screen with drag handles
3. "REORDER WORKOUTS" heading with list of all saved workouts
4. User drags to rearrange order
5. User taps tick button to confirm new order

**Flow 5 — Delete Workout:**
1. User clicks "Delete Workout"
2. System shows confirmation dialog: "Are you sure you want to delete this workout?"
3. "Cancel" button | "Delete" button (red)
4. On confirm: workout and its exercises removed from database

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/EditWorkoutActivity.java`
- `android/app/src/main/res/layout/activity_edit_workout.xml`
- `android/app/src/main/java/com/jve/repsup/activities/ReorderWorkoutsActivity.java`
- `android/app/src/main/res/layout/activity_reorder_workouts.xml`
- `android/app/src/main/res/layout/item_reorder_workout.xml`

## Database Operations
**Edit:**
1. UPDATE workouts SET w_name = ? WHERE w_id = ?
2. DELETE FROM workout_exercises WHERE w_id = ? (then re-insert updated list)
3. INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index, updated_at) VALUES ...

**Duplicate:**
1. INSERT INTO workouts (user_id, w_name, is_default, w_order_index) VALUES (?, 'Copy of ...', false, ?)
2. INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index, updated_at) SELECT [new_w_id], ex_id, sets, reps, w_ex_order_index, NOW() FROM workout_exercises WHERE w_id = [original_w_id]

**Reorder:**
1. UPDATE workouts SET w_order_index = ? WHERE w_id = ? — for each workout in new order

**Delete:**
1. DELETE FROM workouts WHERE w_id = ? (CASCADE deletes workout_exercises)

## Navigation
- Entry point: three-dot menu on a workout card in Saved tab (C200)
- Exit point: all actions return to Workout Page (C200) with refreshed data

## Reference Designs
See designs/ folder — edit workout screen, reorder screen, delete confirmation, and share sheet.
