# Bugfix Requirements Document

## Introduction

This document addresses two related bugs in the workout feature of the Reps Up Android app:

1. **Exercise Reorder Drag Not Working** — The reorder dialog shows exercises but drag-and-drop doesn't initiate because the drag handle is never connected to the `ItemTouchHelper` and the default long-press mechanism conflicts with the dialog's touch handling.

2. **Weight Not Pre-filled During Workout Execution** — Users enter target weights when creating/editing a workout, but the weight is discarded because there is no `target_weight` column in the `workout_exercise` table, no weight field in the `WorkoutExercise` model, and the execution screen never receives or pre-fills it.

---

## Bug Analysis

### Current Behavior (Defect)

#### Bug 1: Exercise Reorder Drag Not Working

1.1 WHEN a user opens the reorder dialog and attempts to drag an exercise item by touching the drag handle icon THEN the system does not initiate a drag gesture and the item remains stationary

1.2 WHEN a user opens the reorder dialog and attempts a long-press drag on an exercise item THEN the system either does not respond or the gesture conflicts with the AlertDialog scroll/touch handling, making reordering unreliable or impossible

#### Bug 2: Weight Not Pre-filled During Workout Execution

1.3 WHEN a user creates or edits a workout and enters a target weight for an exercise THEN the system discards the weight value — it is not persisted to the `workout_exercise` table because no `target_weight` column exists

1.4 WHEN a user starts executing a workout THEN the system displays empty weight fields for every set row, regardless of any weight the user previously entered during workout creation

---

### Expected Behavior (Correct)

#### Bug 1: Exercise Reorder Drag Not Working

2.1 WHEN a user opens the reorder dialog and touches the drag handle icon (`ivDragHandle`) on an exercise item THEN the system SHALL initiate a drag gesture allowing the item to be moved up or down in the list

2.2 WHEN a user drags an exercise item to a new position and releases it THEN the system SHALL update the visual order in the RecyclerView to reflect the new position immediately

2.3 WHEN a user confirms the reorder dialog (taps Save) after dragging exercises to new positions THEN the system SHALL persist the updated `workout_exercise_order_index` values to the database

#### Bug 2: Weight Not Pre-filled During Workout Execution

2.4 WHEN a user creates or edits a workout and enters a target weight for an exercise THEN the system SHALL persist the weight value in a `target_weight` column of the `workout_exercise` table

2.5 WHEN a user starts executing a workout THEN the system SHALL pre-fill the weight field in each set row with the `target_weight` value stored for that exercise in the workout

2.6 WHEN a user creates or edits a workout and leaves the weight field empty (or zero) THEN the system SHALL store `0.0` as the target weight and leave the weight field empty during execution

---

### Unchanged Behavior (Regression Prevention)

#### Bug 1: Exercise Reorder Drag Not Working

3.1 WHEN a user opens the reorder dialog without performing any drag operations and taps Cancel THEN the system SHALL CONTINUE TO dismiss the dialog without modifying exercise order

3.2 WHEN a user reorders exercises and taps Save THEN the system SHALL CONTINUE TO update all `workout_exercise_order_index` values via `WorkoutRepository.updateWorkout()` and show a success toast

3.3 WHEN the workout is loaded for execution after a successful reorder THEN the system SHALL CONTINUE TO display exercises in the saved order (sorted by `workout_exercise_order_index ASC`)

#### Bug 2: Weight Not Pre-filled During Workout Execution

3.4 WHEN a user executes a workout THEN the system SHALL CONTINUE TO pre-fill the reps field with the `num_reps` value from the `workout_exercise` table

3.5 WHEN a user modifies the weight field during workout execution (overriding or clearing the pre-filled value) THEN the system SHALL CONTINUE TO log the user-entered weight in the exercise result, not the target weight

3.6 WHEN a user creates a workout without entering any weight values THEN the system SHALL CONTINUE TO create the workout successfully with `target_weight` defaulting to `0.0`

3.7 WHEN existing workouts (created before this fix) are loaded THEN the system SHALL CONTINUE TO function correctly, treating the missing `target_weight` as `0.0` (empty weight field)
