# Spec: C400 — Create Default Exercise

## Overview
An admin adds a new exercise to the exercise database. They specify the muscle 
group, exercise name, demonstration animation/image, and difficulty level.

## Owner
Emma Martin

## Pre-Conditions
- Admin is logged in as an administrator

## Post-Conditions
- A new exercise is added to the exercises table in the database
- The exercise is available for inclusion in workouts

## Screen
Activity: AdminCreateExerciseActivity.java (admin-only)

**Muscle Group Selection:**
- "ADD NEW EXERCISE" heading in Orbitron
- Full grid of muscle group images (all groups):
  - Abdominals, Abductors, Adductors
  - Biceps, Calves, Quads
  - Forearms, Glutes, Hamstrings
  - Triceps, Chest, Lats
- Tapping a muscle group → opens the exercise creation form for that group

**Exercise Creation Form:**
- "NEW [MUSCLE GROUP] EXERCISE" heading (e.g. "NEW GLUTES EXERCISE")
- Exercise Name input field
- "Add Demonstration" section:
  - Camera/upload icon placeholder
  - Upload or capture demonstration image/animation
- "Select Difficulty" dropdown: Easy / Medium / Hard
- "Save Exercise" button (light blue)

## UI Requirements
- Dark grey background (#393944)
- Light blue (#B2D6FD) for Save Exercise button
- Muscle group grid with anatomy images (same style as library)
- Orbitron for headings
- Inter for form fields

## Validation Rules
- Muscle group: must be selected
- Exercise name: required, non-empty, unique within muscle group
- Difficulty: required, must select one
- Demonstration: optional (can be added later)

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/AdminCreateExerciseActivity.java`
- `android/app/src/main/res/layout/activity_admin_create_exercise.xml`

## Database Operations
1. INSERT INTO exercises (ex_name, muscle_group, default_sets, default_reps, diff, base_xp) VALUES (?, ?, 3, 12, ?, ?)
   - base_xp calculated from difficulty: Easy=50, Medium=75, Hard=100

## Navigation
- Entry point: "Create" button under "CREATE EXERCISE" on Admin Landing (C300)
- Muscle group selection → exercise creation form
- Exit point: "Save Exercise" → returns to Admin Landing with exercise added to database

## Reference Designs
See designs/ folder — admin add exercise flow with muscle group grid and creation form.
