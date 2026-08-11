# Spec: C200 — View Workout Page

## Overview
The workout page displays the user's saved workouts (Saved tab) and a 
browsable exercise library organized by muscle group (Library tab). Users can 
create new workouts from here.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User is not currently on the workout page

## Post-Conditions
- The system displays the workouts page defaulted to the Saved tab
- User can view the exercise library tab

## Screen
Single Fragment: WorkoutFragment.java (inside MainActivity with bottom navigation)

**Header:**
- Avatar thumbnail with level badge
- Streak count with fire icon
- Coin count with coin icon

**Page Title:**
- "MY WORKOUTS" heading in Orbitron
- Plus button (+) → C210 (Create User Workout)

**Tab Toggle:**
- "Saved" tab (default, selected) | "Library" tab

**Saved Tab Content:**
- List of user's saved workouts as expandable cards:
  - Workout name in bold
  - First 3 exercises with avatar demo thumbnails and set count
  - "and X more" if more than 3 exercises
  - Three-dot menu → C220 (Manage Workout)

**Library Tab Content:**
- "MUSCLE GROUPS" heading
- Grid of muscle group images (3 columns, 2 rows per screen):
  - Abdominals, Abductors, Adductors
  - Biceps, Calves, Quads
  - Forearms, Glutes, Hamstrings
  - Triceps, Chest, Lats
- Tapping a muscle group → list of exercises in that group:
  - Exercise name
  - Difficulty label (Easy/Medium/Hard)
  - Exercise demo thumbnail

## UI Requirements
- Dark grey background (#393944)
- Light blue (#B2D6FD) for selected Saved tab
- Workout cards with dark_grey_variant background
- Muscle group grid with anatomy images
- Orbitron for headings
- Inter for exercise details

## Files to Create
- `android/app/src/main/java/com/jve/repsup/fragments/WorkoutFragment.java`
- `android/app/src/main/res/layout/fragment_workout.xml`
- `android/app/src/main/java/com/jve/repsup/adapters/SavedWorkoutAdapter.java`
- `android/app/src/main/res/layout/item_saved_workout.xml`
- `android/app/src/main/java/com/jve/repsup/adapters/MuscleGroupAdapter.java`
- `android/app/src/main/res/layout/item_muscle_group.xml`
- `android/app/src/main/java/com/jve/repsup/adapters/ExerciseLibraryAdapter.java`
- `android/app/src/main/res/layout/item_exercise_library.xml`

## Database Operations
**Saved Tab:**
1. SELECT w.* FROM workouts w WHERE w.user_id = ? AND w.is_default = false ORDER BY w.w_order_index
2. SELECT we.*, e.ex_name, e.muscle_group FROM workout_exercises we JOIN exercises e ON we.ex_id = e.ex_id WHERE we.w_id = ? ORDER BY we.w_ex_order_index

**Library Tab:**
1. SELECT DISTINCT muscle_group FROM exercises
2. SELECT * FROM exercises WHERE muscle_group = ? ORDER BY diff, ex_name

## Navigation
- Entry point: bottom nav "Workout" tab
- Plus button → C210 (Create User Workout)
- Three-dot menu on workout card → C220 (Manage Workout)
- Muscle group tap → exercise list for that group

## Reference Designs
See designs/ folder — workout page with saved workouts list and muscle group library grid.
