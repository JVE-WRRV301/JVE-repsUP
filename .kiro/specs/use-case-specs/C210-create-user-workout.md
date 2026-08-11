# Spec: C210 — Create User Workout

## Overview
The user creates a new workout by either generating one based on difficulty 
and muscle groups, or manually selecting exercises from the library. They then 
configure sets, reps, and weight before naming and saving the workout.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User is on the workout page (C200)

## Post-Conditions
- A new workout is saved to the user's workout library
- Workout exercises are stored with configured sets/reps/weight

## Screen
Activity: CreateWorkoutActivity.java

**Method Selection (first screen):**
- Tab toggle: "Generate Workout" | "Create Workout"
- Difficulty selector (for generate): Easy, Medium, Hard
- Muscle group selection grid (for both methods)

**Exercise Selection (Create Workout flow):**
- "MUSCLE GROUPS" heading
- Grid of muscle group images (same as library)
- Tapping a group shows exercises within it
- User selects exercises (checkmark appears)
- Back button returns to muscle group grid to select from other groups
- "Next" button when done selecting → workout configuration screen
- Maximum 8 exercises enforced

**Exercise Selection (Generate Workout flow):**
- User selects difficulty and muscle groups
- System auto-selects exercises matching criteria
- User can review and modify the generated selection

**Workout Configuration Screen:**
- "YOUR WORKOUT" heading in Orbitron
- Workout name input field at top
- List of selected exercises, each showing:
  - Exercise name
  - Set rows: Set number | Reps input | Weight input
  - Edit icon per exercise to modify sets
  - Add set (+) button
- "Save Workout" button at bottom

## UI Requirements
- Dark grey background (#393944)
- Light blue (#B2D6FD) for selected tab and buttons
- Muscle group grid with anatomy images
- Exercise list with difficulty colour coding
- Orbitron for headings
- Inter for form fields

## Validation Rules
- Must select at least 1 exercise
- Maximum 8 exercises per workout
- Workout name: required, non-empty
- Sets: minimum 1 per exercise
- Reps: must be > 0
- Weight: must be >= 0

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/CreateWorkoutActivity.java`
- `android/app/src/main/res/layout/activity_create_workout.xml`
- `android/app/src/main/res/layout/step_select_exercises.xml`
- `android/app/src/main/res/layout/step_configure_workout.xml`
- `android/app/src/main/java/com/jve/repsup/adapters/ExerciseSelectAdapter.java`

## Database Operations
1. SELECT * FROM exercises WHERE muscle_group = ? ORDER BY diff, ex_name
2. INSERT INTO workouts (user_id, w_name, is_default, w_order_index) VALUES (?, ?, false, ?)
3. INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index, updated_at) VALUES (?, ?, ?, ?, ?, NOW()) — for each selected exercise

## Navigation
- Entry point: plus (+) button on Workout Page (C200)
- Exit point: "Save Workout" → returns to Workout Page with new workout in saved list

## Reference Designs
See designs/ folder — create workout flow with muscle group selection, exercise list, and configuration.
