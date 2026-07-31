# Spec: C300 — Create Default Workout

## Overview
An admin creates a new default (pre-made) workout that users can receive 
when they select "Generate Workout." The admin selects a category, chooses 
exercises, sets the difficulty, and saves it to the system.

## Owner
Emma Martin

## Pre-Conditions
- Admin is logged into the system as an administrator

## Post-Conditions
- A new default workout is created and stored in the database
- The workout is available for users who generate workouts matching its category/difficulty

## Screen
Activity: AdminCreateWorkoutActivity.java (admin-only)

**Admin Landing Page:**
- "REPS UP" header with admin name
- "MANAGE WORKOUTS" heading with plus (+) button
- Search bar
- Collapsible category list:
  - Default Abdominal Workouts
  - Default Full Body Workouts
  - Default Lower Body Workouts
  - Default Upper Body Workouts
  - Each expandable by difficulty (Easy, Medium)
  - Shows existing workouts within each category

**Create Workout Screen (step 1):**
- "CREATE WORKOUT" heading
- Category options: Create Abdominal / Full Body / Lower Body / Upper Body Workout
- "CREATE EXERCISE" section with "Create" button (→ C400)

**Exercise Selection (step 2):**
- Muscle group grid filtered to selected category
- Select exercises from relevant muscle groups
- "Next" button when done

**Workout Configuration (step 3):**
- "DEFAULT WORKOUT" heading
- Workout name input
- Difficulty dropdown (Easy / Medium / Hard)
- List of selected exercises with:
  - Set rows: Set number | Reps input | Weight input
- "Save Workout" button

## UI Requirements
- Dark grey background (#393944)
- Light blue (#B2D6FD) accent for buttons
- Admin UI follows same design system as user UI
- Orbitron for headings
- Inter for form content

## Validation Rules
- Must select at least 1 exercise (max 8)
- Workout name: required
- Difficulty: required

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/AdminCreateWorkoutActivity.java`
- `android/app/src/main/res/layout/activity_admin_create_workout.xml`
- `android/app/src/main/java/com/jve/repsup/activities/AdminLandingActivity.java`
- `android/app/src/main/res/layout/activity_admin_landing.xml`

## Database Operations
1. SELECT * FROM exercises WHERE muscle_group IN (?) ORDER BY diff, ex_name
2. INSERT INTO workouts (user_id, w_name, is_default, w_order_index) VALUES (NULL, ?, true, ?)
3. INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index, updated_at) VALUES (?, ?, ?, ?, ?, NOW()) — for each exercise

## Navigation
- Entry point: plus (+) button on Admin Landing Page
- Exit point: "Save Workout" → returns to Admin Landing with new workout listed

## Reference Designs
See designs/ folder — admin create workout flow with category selection, exercise grid, and config.
