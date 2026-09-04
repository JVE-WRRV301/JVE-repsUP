# Implementation Plan

## Overview

This plan addresses two bugs: (1) exercise reorder drag not working because the drag handle is not connected to `ItemTouchHelper`, and (2) target weight not persisted or pre-filled during workout execution. The implementation follows a bug-condition-first testing approach: write failing tests to confirm the bugs, write preservation tests to guard regressions, then fix each bug and verify all tests pass.

## Task Dependency Graph

```json
{
  "waves": [
    ["1", "2"],
    ["3.1", "4.1"],
    ["3.2", "4.2"],
    ["3.3", "4.3"],
    ["4.4"],
    ["4.5"],
    ["4.6"],
    ["3.4", "4.7"],
    ["3.5", "4.8"],
    ["4.9"],
    ["4.10"],
    ["5"]
  ]
}
```

## Tasks

- [ ] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Drag Handle Not Connected and Weight Discarded
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bugs exist
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate both bugs exist
  - **Scoped PBT Approach**: For Bug 1, scope property to: touching ivDragHandle triggers startDrag(). For Bug 2, scope property to: saving a workout with non-zero weightList results in target_weight persisted in DB and pre-filled during execution
  - Bug 1 Assertions: Verify that `ReorderExerciseAdapter.onBindViewHolder` sets an `OnTouchListener` on `ivDragHandle` that calls `startDrag(viewHolder)` on `ACTION_DOWN` — on unfixed code this listener does not exist
  - Bug 2 Assertions: Verify that `WorkoutExercise` has a `targetWeight` field, that `createWorkout` INSERT includes `target_weight`, and that `addSetRow` accepts and pre-fills a weight parameter — on unfixed code none of these exist
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bugs exist)
  - Document counterexamples: (1) ivDragHandle has no touch listener, isLongPressDragEnabled defaults to true; (2) WorkoutExercise has no targetWeight field, INSERT SQL has no target_weight column, addSetRow has no weight parameter
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Reorder Cancel, Reps Pre-fill, and User Weight Override
  - **IMPORTANT**: Follow observation-first methodology
  - Observe: Cancel button on reorder dialog dismisses without DB writes on unfixed code
  - Observe: `displayCurrentExercise` calls `addSetRow(workoutExercise.getReps())` correctly pre-filling reps on unfixed code
  - Observe: `saveCurrentExerciseResult` reads user-entered weight from `etWeight` EditText and logs it in `ExerciseResult` (not from any stored target weight) on unfixed code
  - Observe: Workouts created without weight values succeed and store default reps/sets on unfixed code
  - Write property-based tests:
    - For all cancel interactions on the reorder dialog, no DB write occurs and dialog dismisses
    - For all workout executions, reps field is pre-filled with `num_reps` from `workout_exercise` table
    - For all user weight inputs during execution, `ExerciseResult` logs the user-entered value regardless of any target weight
    - For all workouts with zero/missing target weight, weight field remains empty during execution
  - Verify tests pass on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [ ] 3. Fix for Exercise Reorder Drag Not Working (Bug 1)

  - [ ] 3.1 Add drag-start listener interface and connect drag handle
    - Add `OnDragStartListener` interface with method `onDragStart(RecyclerView.ViewHolder viewHolder)`
    - Modify `ReorderExerciseAdapter` constructor to accept an `OnDragStartListener` parameter
    - In `onBindViewHolder`, set `OnTouchListener` on `ivDragHandle` that calls `listener.onDragStart(holder)` on `MotionEvent.ACTION_DOWN`
    - _Bug_Condition: isBugCondition_Reorder(input) where input.target == ivDragHandle AND NOT dragGestureInitiated_
    - _Expected_Behavior: dragGestureInitiated(result) == true for all ivDragHandle touch events_
    - _Preservation: Cancel dismisses without changes, save persists order_
    - _Requirements: 2.1, 2.2_

  - [ ] 3.2 Disable long-press drag and store ItemTouchHelper reference
    - Override `isLongPressDragEnabled()` in `SimpleCallback` to return `false`
    - Store `ItemTouchHelper` in a local variable before attaching to RecyclerView
    - Pass the `ItemTouchHelper` reference to the adapter via the listener: `viewHolder -> itemTouchHelper.startDrag(viewHolder)`
    - _Bug_Condition: Long-press conflicts with AlertDialog touch handling_
    - _Expected_Behavior: Drag only initiates via handle touch, not long-press_
    - _Preservation: Dialog scroll and dismiss behavior unchanged_
    - _Requirements: 2.1, 3.1_

  - [ ] 3.3 Update order numbers after move
    - In the `onMove` callback, after `notifyItemMoved(fromPos, toPos)`, call `reorderAdapter.notifyItemRangeChanged(Math.min(fromPos, toPos), Math.abs(fromPos - toPos) + 1)` to refresh visible order numbers in `tvReorderExerciseOrder`
    - _Expected_Behavior: Order number TextViews update immediately after each drag move_
    - _Preservation: Save still persists correct order_index values_
    - _Requirements: 2.2_

  - [ ] 3.4 Verify bug condition exploration test now passes (Bug 1 portion)
    - **Property 1: Expected Behavior** - Drag Handle Initiates Drag
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The Bug 1 portion of the test from task 1 encodes the expected behavior
    - When this test passes, it confirms drag handle touch initiates drag gesture
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2_

  - [ ] 3.5 Verify preservation tests still pass (Bug 1 portion)
    - **Property 2: Preservation** - Reorder Cancel and Dialog Interactions
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2 (cancel, scroll, non-drag interactions)
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm reorder cancel, save persistence, and exercise order display are unchanged
    - _Requirements: 3.1, 3.2, 3.3_

- [ ] 4. Fix for Weight Not Pre-filled During Workout Execution (Bug 2)

  - [ ] 4.1 Create database migration for target_weight column
    - Create file `database/migrations/003_add_target_weight.sql`
    - SQL: `ALTER TABLE workout_exercise ADD COLUMN target_weight DECIMAL(6,2) NOT NULL DEFAULT 0.00 AFTER workout_exercise_order_index;`
    - _Bug_Condition: No target_weight column exists in workout_exercise table_
    - _Expected_Behavior: Column exists, defaults to 0.00 for existing rows_
    - _Requirements: 2.4_

  - [ ] 4.2 Add targetWeight field to WorkoutExercise model
    - Add `private double targetWeight;` field to `WorkoutExercise.java`
    - Add `getTargetWeight()` and `setTargetWeight(double targetWeight)` methods
    - Update the full constructor to include `targetWeight` parameter
    - _Bug_Condition: WorkoutExercise model has no weight field_
    - _Expected_Behavior: Model can hold and expose targetWeight value_
    - _Requirements: 2.4, 2.5_

  - [ ] 4.3 Update WorkoutRepository.getExercisesForWorkout to SELECT and map target_weight
    - Add `we.target_weight` to the SELECT column list in `getExercisesForWorkout`
    - Add `we.setTargetWeight(rs.getDouble("target_weight"))` when mapping ResultSet to model
    - _Bug_Condition: Query never reads target_weight from database_
    - _Expected_Behavior: targetWeight populated from DB when loading workout exercises_
    - _Preservation: Existing workouts default to 0.0_
    - _Requirements: 2.5, 3.7_

  - [ ] 4.4 Update WorkoutRepository.createWorkout INSERT to include target_weight
    - Update INSERT SQL to: `INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index, target_weight) VALUES (?, ?, ?, ?, ?, ?)`
    - Bind `we.getTargetWeight()` as the 6th parameter in the batch statement
    - _Bug_Condition: INSERT never writes target_weight to database_
    - _Expected_Behavior: target_weight persisted on workout creation_
    - _Preservation: Workouts without weight values store 0.0_
    - _Requirements: 2.4, 3.6_

  - [ ] 4.5 Update WorkoutRepository.updateWorkout INSERT to include target_weight
    - Update the re-insert SQL (after DELETE) to include `target_weight` column
    - Bind `we.getTargetWeight()` as the 6th parameter in the batch statement
    - _Bug_Condition: UPDATE/re-insert never writes target_weight_
    - _Expected_Behavior: target_weight persisted on workout edit/reorder_
    - _Preservation: Reorder save continues to work correctly_
    - _Requirements: 2.4, 3.2_

  - [ ] 4.6 Update CreateWorkoutActivity.onWorkoutSaved to set targetWeight
    - In the loop building `WorkoutExercise` objects, after `we.setOrderIndex(i + 1)`, add `we.setTargetWeight(weightList.get(i))`
    - _Bug_Condition: onWorkoutSaved receives weightList but never assigns values to WorkoutExercise objects_
    - _Expected_Behavior: Each WorkoutExercise carries its target weight through to repository_
    - _Requirements: 2.4_

  - [ ] 4.7 Modify WorkoutExecutionActivity.addSetRow to accept and pre-fill weight
    - Add overloaded method: `addSetRow(int defaultReps, double defaultWeight)`
    - If `defaultWeight > 0`, set `etWeight.setText(String.valueOf(defaultWeight))`
    - If `defaultWeight == 0`, leave `etWeight` empty (do NOT show "0.0")
    - Update the no-arg `addSetRow()` to call `addSetRow(0, 0.0)`
    - _Bug_Condition: addSetRow only accepts reps, never pre-fills weight_
    - _Expected_Behavior: Weight pre-filled when target_weight > 0, empty otherwise_
    - _Preservation: User can still override pre-filled weight during execution_
    - _Requirements: 2.5, 2.6, 3.5_

  - [ ] 4.8 Update displayCurrentExercise to pass target weight to addSetRow
    - Change `addSetRow(workoutExercise.getReps())` to `addSetRow(workoutExercise.getReps(), workoutExercise.getTargetWeight())`
    - _Bug_Condition: displayCurrentExercise never passes weight to addSetRow_
    - _Expected_Behavior: Each set row pre-filled with both reps and target weight_
    - _Preservation: Reps pre-fill behavior unchanged_
    - _Requirements: 2.5, 3.4_

  - [ ] 4.9 Verify bug condition exploration test now passes (Bug 2 portion)
    - **Property 1: Expected Behavior** - Target Weight Persisted and Pre-filled
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The Bug 2 portion of the test from task 1 encodes the expected behavior
    - When this test passes, it confirms weight is persisted and pre-filled
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.4, 2.5, 2.6_

  - [ ] 4.10 Verify preservation tests still pass (Bug 2 portion)
    - **Property 2: Preservation** - Reps Pre-fill and User Weight Override
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2 (reps pre-fill, user weight override, zero weight)
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm reps still pre-fill correctly, user-entered weight still logged in ExerciseResult, zero target weight shows empty field
    - _Requirements: 3.4, 3.5, 3.6, 3.7_

- [ ] 5. Checkpoint - Ensure all tests pass
  - Run all exploration tests (Property 1) — should now PASS
  - Run all preservation tests (Property 2) — should still PASS
  - Verify no regressions in existing workout CRUD operations
  - Verify backward compatibility: existing workouts (no target_weight) load with 0.0 default and show empty weight fields
  - Ensure all tests pass, ask the user if questions arise.


## Notes

- Task 1 tests are expected to FAIL on unfixed code (confirming bugs exist) and PASS after fixes are applied.
- Task 2 tests are expected to PASS on both unfixed and fixed code (confirming no regressions).
- Bug 1 fix (tasks 3.1–3.5) and Bug 2 fix (tasks 4.1–4.10) are independent and can be developed in parallel after tests are written.
- The migration in task 4.1 must be applied to the database before running Bug 2 verification tests.
