# Workout Reorder and Weight Pre-fill Bugfix Design

## Overview

This design addresses two related bugs in the workout management feature:

1. **Exercise Reorder Drag Not Working** — The drag-and-drop reorder dialog fails to initiate drags because the `ivDragHandle` touch listener is not connected to `ItemTouchHelper.startDrag()`, and the default long-press trigger conflicts with dialog touch handling.

2. **Weight Not Pre-filled During Workout Execution** — Users enter target weights during workout creation/editing, but the weight is discarded because there is no `target_weight` column in the `workout_exercise` table, no corresponding field in `WorkoutExercise.java`, and the execution screen never receives or pre-fills it.

The fix is minimal and targeted: connect the drag handle to trigger drag initiation, and thread a `target_weight` value from schema through model, repository, creation, and execution.

## Glossary

- **Bug_Condition (C)**: The conditions that trigger each bug — (1) touching the drag handle without drag initiating, (2) weight values being discarded on save and absent on execution
- **Property (P)**: The desired behavior — (1) drag initiates on handle touch, (2) weight persists and pre-fills
- **Preservation**: Existing behaviors that must remain unchanged — cancel dismisses without changes, save persists order, reps pre-fill works, user-entered weight during execution is logged
- **ItemTouchHelper**: Android RecyclerView utility that provides swipe-to-dismiss and drag-and-drop support
- **ReorderExerciseAdapter**: Inner adapter class in `WorkoutFragment.java` that renders items in the reorder dialog
- **WorkoutExercise**: Model class mapping to the `workout_exercise` join table, linking workouts to exercises with sets/reps/order configuration
- **ConnectionManager.executeAsync()**: Background thread execution wrapper for all JDBC calls

## Bug Details

### Bug 1: Exercise Reorder Drag Not Working

#### Bug Condition

The bug manifests when a user touches the drag handle icon in the reorder dialog. The `ItemTouchHelper` is attached to the RecyclerView with long-press as the default drag trigger, but long-press conflicts with the `AlertDialog` touch handling. The drag handle `ivDragHandle` is rendered visually but has no touch listener connecting it to `ItemTouchHelper.startDrag()`.

**Formal Specification:**
```
FUNCTION isBugCondition_Reorder(input)
  INPUT: input of type TouchEvent on reorder dialog item
  OUTPUT: boolean
  
  RETURN input.target == ivDragHandle
         AND reorderDialogIsVisible()
         AND itemTouchHelper.isAttached()
         AND NOT dragGestureInitiated(input)
END FUNCTION
```

#### Examples

- User touches drag handle on "Bench Press" item → expected: drag starts, actual: nothing happens
- User long-presses exercise item body → expected: drag starts (fallback), actual: conflicts with dialog scroll, unreliable
- User drags "Squats" from position 3 to position 1 → expected: visual reorder + order numbers update, actual: cannot initiate drag at all
- User touches drag handle then moves finger up/down → expected: item follows finger, actual: no response

### Bug 2: Weight Not Pre-filled During Workout Execution

#### Bug Condition

The bug manifests when a user enters weight values during workout creation/editing. The `onWorkoutSaved` method receives a `weightList` parameter but never assigns the values to `WorkoutExercise` objects because the model has no `targetWeight` field, and the database table has no `target_weight` column.

**Formal Specification:**
```
FUNCTION isBugCondition_Weight(input)
  INPUT: input of type WorkoutSaveRequest with weightList
  OUTPUT: boolean
  
  RETURN input.weightList IS NOT NULL
         AND input.weightList.size() > 0
         AND ANY weight IN input.weightList WHERE weight > 0
         AND NOT weightPersistedToDatabase(input.workoutId, input.weightList)
END FUNCTION
```

#### Examples

- User creates workout with "Bench Press" at 60kg → expected: 60kg stored and pre-filled on execution, actual: weight discarded, field empty during execution
- User edits workout changing "Squat" from 80kg to 100kg → expected: 100kg persisted, actual: weight discarded
- User creates workout with 0kg (empty weight) → expected: 0.0 stored, empty field during execution, actual: same behavior (no regression for zero case)
- User starts executing a workout → expected: weight fields pre-filled with stored target weight, actual: all weight fields empty

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Cancel button on the reorder dialog must continue to dismiss without modifying exercise order
- Save button on the reorder dialog must continue to persist `workout_exercise_order_index` values via `WorkoutRepository.updateWorkout()`
- After successful reorder, exercises must continue to display in saved order (sorted by `workout_exercise_order_index ASC`)
- Reps field must continue to be pre-filled with `num_reps` from the `workout_exercise` table during execution
- User-modified weight values during workout execution must continue to be logged as the actual weight in `ExerciseResult`, not overridden by `target_weight`
- Creating a workout without entering weight values must continue to succeed (defaulting to 0.0)
- Existing workouts (created before this fix) must continue to function correctly, treating missing `target_weight` as 0.0

**Scope:**
All inputs that do NOT involve drag handle touches (Bug 1) or weight persistence/pre-fill (Bug 2) should be completely unaffected by these fixes. This includes:
- Normal RecyclerView scrolling in the reorder dialog
- Workout creation/editing without weight values
- Workout execution where user manually enters weight (overriding pre-fill)
- All other workout CRUD operations (delete, duplicate, schedule)

## Hypothesized Root Cause

### Bug 1: Reorder Drag

Based on code analysis of `WorkoutFragment.java`:

1. **Drag Handle Not Connected**: The `ItemTouchHelper.SimpleCallback` is created with `UP | DOWN` directions and attached to the RecyclerView, but the `ReorderExerciseAdapter` never exposes a mechanism to call `itemTouchHelper.startDrag(viewHolder)` when the drag handle is touched. The adapter's `onBindViewHolder` sets the exercise name and order number but does not set any touch listener on `ivDragHandle`.

2. **Long-Press Default Conflicts**: The `SimpleCallback` uses `isLongPressDragEnabled() == true` (default), but long-press inside an `AlertDialog` is unreliable due to the dialog's own touch interception for scrolling and dismissal.

3. **Order Numbers Not Updated After Move**: The `onMove` callback swaps items in the list and calls `notifyItemMoved`, but does not call `notifyItemRangeChanged` to update the visible order number text in the `tvReorderExerciseOrder` views.

### Bug 2: Weight Not Persisted

Based on code analysis of `CreateWorkoutActivity.java`, `WorkoutExercise.java`, and `WorkoutRepository.java`:

1. **No Database Column**: The `workout_exercise` table schema has no `target_weight` column — only `workout_id`, `exercise_id`, `num_sets`, `num_reps`, `workout_exercise_order_index`, and `updated_at`.

2. **No Model Field**: `WorkoutExercise.java` has fields for `sets`, `reps`, `orderIndex` but no `targetWeight` field.

3. **Weight Parameter Discarded**: `CreateWorkoutActivity.onWorkoutSaved()` receives `List<Double> weightList` but the loop building `WorkoutExercise` objects never calls a setter for weight — `we.setReps(repsList.get(i))` exists but no `we.setTargetWeight(weightList.get(i))`.

4. **Repository Ignores Weight**: Both `createWorkout` and `updateWorkout` INSERT into `workout_exercise` with only `(workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index)` — no weight column included.

5. **Execution Never Pre-fills**: `WorkoutExecutionActivity.addSetRow(int defaultReps)` only accepts a reps parameter and pre-fills reps. There is no weight parameter and no pre-fill logic for the `etWeight` EditText.

## Correctness Properties

Property 1: Bug Condition - Drag Handle Initiates Drag

_For any_ touch event on the `ivDragHandle` view within the reorder dialog while the dialog is visible and exercises are loaded, the fixed code SHALL initiate a drag gesture via `ItemTouchHelper.startDrag(viewHolder)`, allowing the item to be repositioned up or down in the list with immediate visual feedback (updated order numbers).

**Validates: Requirements 2.1, 2.2**

Property 2: Bug Condition - Target Weight Persisted and Pre-filled

_For any_ workout save operation where `weightList` contains non-zero values, the fixed code SHALL persist the weight to the `target_weight` column in `workout_exercise`, and _for any_ subsequent workout execution, the weight field in each set row SHALL be pre-filled with the stored `target_weight` value.

**Validates: Requirements 2.4, 2.5, 2.6**

Property 3: Preservation - Reorder Dialog Cancel and Non-Drag Interactions

_For any_ interaction with the reorder dialog that does NOT involve touching the drag handle (cancel button, scrolling, tapping outside), the fixed code SHALL produce exactly the same behavior as the original code, preserving dialog dismissal without data modification and normal scroll behavior.

**Validates: Requirements 3.1, 3.2, 3.3**

Property 4: Preservation - Workout Execution Reps Pre-fill and User-Entered Weight

_For any_ workout execution where the user modifies the weight field (overriding or clearing the pre-filled value), the fixed code SHALL continue to log the user-entered weight in `ExerciseResult`, not the stored `target_weight`. The reps pre-fill behavior SHALL remain identical to the original code.

**Validates: Requirements 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

#### Bug 1: Connect Drag Handle to ItemTouchHelper

**File**: `android/app/src/main/java/com/jve/repsup/fragments/WorkoutFragment.java`

**Specific Changes**:

1. **Disable long-press drag**: Override `isLongPressDragEnabled()` in the `SimpleCallback` to return `false`, eliminating the conflict with dialog touch handling.

2. **Expose ItemTouchHelper reference to adapter**: Store the `ItemTouchHelper` instance in a variable accessible to the adapter, or pass a drag-start listener interface to the adapter.

3. **Add drag listener interface**: Create a `OnDragStartListener` interface with method `onDragStart(RecyclerView.ViewHolder viewHolder)` that calls `itemTouchHelper.startDrag(viewHolder)`.

4. **Set touch listener on drag handle**: In `ReorderExerciseAdapter.onBindViewHolder()`, set an `OnTouchListener` on `ivDragHandle` that calls the drag-start listener on `ACTION_DOWN` events.

5. **Update order numbers after move**: In the `onMove` callback, after `notifyItemMoved`, call `reorderAdapter.notifyItemRangeChanged(Math.min(fromPos, toPos), Math.abs(fromPos - toPos) + 1)` to refresh the visible order numbers.

#### Bug 2: Thread Target Weight Through the Stack

**File**: `database/migrations/003_add_target_weight.sql`

**Specific Changes**:
1. **Add column**: `ALTER TABLE workout_exercise ADD COLUMN target_weight DECIMAL(6,2) NOT NULL DEFAULT 0.00 AFTER workout_exercise_order_index;`

**File**: `android/app/src/main/java/com/jve/repsup/models/WorkoutExercise.java`

**Specific Changes**:
2. **Add field**: `private double targetWeight;`
3. **Add getter/setter**: `getTargetWeight()` / `setTargetWeight(double targetWeight)`
4. **Update constructor**: Add `targetWeight` parameter to the full constructor

**File**: `android/app/src/main/java/com/jve/repsup/repositories/WorkoutRepository.java`

**Specific Changes**:
5. **Update `getExercisesForWorkout`**: Add `we.target_weight` to the SELECT query and call `we.setTargetWeight(rs.getDouble("target_weight"))` when mapping results.
6. **Update `createWorkout` INSERT**: Add `target_weight` to the column list and bind `we.getTargetWeight()` as a parameter.
7. **Update `updateWorkout` INSERT**: Same as above for the re-insert after delete.

**File**: `android/app/src/main/java/com/jve/repsup/activities/CreateWorkoutActivity.java`

**Specific Changes**:
8. **Set target weight in `onWorkoutSaved`**: After `we.setOrderIndex(i + 1)`, add `we.setTargetWeight(weightList.get(i))` in the loop.

**File**: `android/app/src/main/java/com/jve/repsup/activities/WorkoutExecutionActivity.java`

**Specific Changes**:
9. **Modify `addSetRow` signature**: Add a `double defaultWeight` parameter alongside `int defaultReps`.
10. **Pre-fill weight**: If `defaultWeight > 0`, set `etWeight.setText(String.valueOf(defaultWeight))`.
11. **Update `displayCurrentExercise`**: Call `addSetRow(workoutExercise.getReps(), workoutExercise.getTargetWeight())` instead of `addSetRow(workoutExercise.getReps())`.
12. **Update no-arg `addSetRow`**: Call `addSetRow(0, 0.0)` for the "Add Set" button.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bugs on unfixed code, then verify the fixes work correctly and preserve existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bugs BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Inspect the unfixed code to confirm that (1) `ivDragHandle` has no touch listener and `isLongPressDragEnabled` defaults to true, and (2) `onWorkoutSaved` never calls a weight setter, the INSERT SQL has no `target_weight` column, and `addSetRow` has no weight parameter.

**Test Cases**:
1. **Drag Handle Touch Test**: Touch the drag handle in the reorder dialog (will fail — no drag initiates on unfixed code)
2. **Long-Press Drag Test**: Long-press an exercise item in the dialog (will fail — unreliable due to dialog touch conflict)
3. **Weight Persistence Test**: Create a workout with weight values, query `workout_exercise` table (will fail — no `target_weight` column exists)
4. **Weight Pre-fill Test**: Execute a workout and observe weight fields (will fail — always empty on unfixed code)

**Expected Counterexamples**:
- Drag handle touch produces no visual or behavioral response
- Weight values logged during creation are absent from database and from execution screen
- Possible causes confirmed: missing touch listener binding, missing DB column, missing model field

### Fix Checking

**Goal**: Verify that for all inputs where the bug conditions hold, the fixed functions produce the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition_Reorder(input) DO
  result := handleDragHandleTouch_fixed(input)
  ASSERT dragGestureInitiated(result)
  ASSERT orderNumbersUpdatedVisually(result)
END FOR

FOR ALL input WHERE isBugCondition_Weight(input) DO
  result := saveWorkout_fixed(input)
  ASSERT targetWeightPersistedInDatabase(result)
  execution := executeWorkout_fixed(result.workoutId)
  ASSERT weightFieldPreFilled(execution, input.targetWeight)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug conditions do NOT hold, the fixed functions produce the same result as the original functions.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition_Reorder(input) DO
  ASSERT showReorderDialog_original(input) = showReorderDialog_fixed(input)
END FOR

FOR ALL input WHERE NOT isBugCondition_Weight(input) DO
  ASSERT executeWorkout_original(input) = executeWorkout_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss (e.g., empty weight lists, zero-exercise workouts)
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for cancel actions, scroll gestures, reps pre-fill, and user-entered weight logging, then write tests capturing that behavior.

**Test Cases**:
1. **Cancel Preservation**: Verify cancel dismisses dialog without DB writes on both original and fixed code
2. **Reps Pre-fill Preservation**: Verify reps fields still show `num_reps` values during execution after fix
3. **User Weight Override Preservation**: Verify that manually entered weight during execution is logged in `ExerciseResult`, not overridden by `target_weight`
4. **Zero Weight Preservation**: Verify workouts with 0.0 target weight show empty weight fields (not "0.0")

### Unit Tests

- Test `ReorderExerciseAdapter` drag handle touch triggers `startDrag` callback
- Test `onMove` followed by `notifyItemRangeChanged` updates order number text
- Test `WorkoutExercise.getTargetWeight()` / `setTargetWeight()` round-trip
- Test `addSetRow(12, 60.0)` pre-fills both reps and weight fields
- Test `addSetRow(12, 0.0)` pre-fills reps but leaves weight empty
- Test `onWorkoutSaved` with weight list builds `WorkoutExercise` objects with correct `targetWeight`

### Property-Based Tests

- Generate random exercise lists and verify drag handle touch always initiates drag for valid adapter positions
- Generate random weight lists (including zeros, decimals, large values) and verify persistence round-trip through create → read
- Generate random workout configurations and verify reps pre-fill behavior is preserved regardless of target weight value
- Generate random user weight inputs during execution and verify `ExerciseResult` always logs user-entered value

### Integration Tests

- Full flow: Create workout with weights → verify DB has `target_weight` → execute workout → verify fields pre-filled
- Full flow: Open reorder dialog → drag exercise via handle → save → verify new order persisted → execute → verify new order displayed
- Backward compatibility: Load existing workout (no `target_weight` in DB, defaults to 0.0) → execute → verify empty weight fields and no crash
