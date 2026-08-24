# XP Log Race Condition Bugfix Design

## Overview

The `WorkoutCompleteActivity.onCreate()` method calls `persistCompletion()` synchronously after `calculateResults()`, but `calculateResults()` computes `totalXp` inside an asynchronous database callback (fetching the user's streak). This means `persistCompletion()` fires while `totalXp` is still 0, causing the database to record incorrect XP values across three tables. The fix restructures the async flow so that `persistCompletion()` is only invoked from within the `calculateResults()` callback, after `totalXp` has been assigned.

## Glossary

- **Bug_Condition (C)**: The race condition where `persistCompletion()` executes before the async streak-fetch callback in `calculateResults()` has assigned `totalXp`, resulting in 0 XP being persisted
- **Property (P)**: After the fix, `persistCompletion()` shall only execute after `totalXp` has been correctly computed (non-zero for valid workout results)
- **Preservation**: All existing behaviors unrelated to the timing of `persistCompletion()` — XP formula, streak fallback, UI display, streak updates, level-up, rank-up — must remain unchanged
- **calculateResults()**: Method in `WorkoutCompleteActivity.java` that asynchronously fetches the user's streak and computes `totalXp` via `XPCalculator.calculateWorkoutXP()`
- **persistCompletion()**: Method in `WorkoutCompleteActivity.java` that writes `totalXp` to the scheduled_workout record, user XP, and XP log
- **totalXp**: Instance field holding the computed workout XP; default 0 until the async callback assigns it

## Bug Details

### Bug Condition

The bug manifests when a user completes a workout. The `onCreate()` method calls `calculateResults()` followed immediately by `persistCompletion()`. Since `calculateResults()` fetches the streak via an async database call and computes `totalXp` inside the callback, `persistCompletion()` executes before the callback runs — using the default value of 0.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type WorkoutCompletionEvent
  OUTPUT: boolean
  
  RETURN input.exerciseResults IS NOT EMPTY
         AND persistCompletion() IS CALLED BEFORE calculateResults callback completes
         AND totalXp == 0 AT TIME OF persistCompletion() execution
END FUNCTION
```

### Examples

- **Normal workout (3 exercises, streak=5):** User completes workout earning 450 XP. UI shows "+450 XP" but database records `xp_earned = 0` in xp_log, user.xp is incremented by 0, and scheduled_workout.xp_earned = 0.
- **First workout ever (streak=0):** User completes workout earning 200 XP. UI shows "+200 XP" but all three DB writes persist 0.
- **Streak fetch fails (error path):** User completes workout, streak fetch errors, fallback multiplier of 1.0 is used. UI shows correct XP but database still records 0 because `persistCompletion()` already fired before the error callback ran.
- **Empty exercise results (edge case):** User somehow completes a workout with no exercises. `totalXp` is correctly 0 — this is NOT a bug manifestation since 0 is the correct value.

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- XP calculation formula: `sum(baseXp * setsCompleted) * streakMultiplier` must remain the same
- Streak multiplier fallback to 1.0 on error must continue to work
- UI display of XP on the completion screen must continue to show the correct value
- `totalSets` and `totalReps` computation must remain unchanged
- Streak update logic (only increment on first completion of the day) must remain unchanged
- Level-up check (50 coins awarded) must continue to trigger correctly
- Rank promotion check (100 bonus coins) must continue to trigger correctly
- LevelUpDialog, RankUpDialog, and StreakMilestoneDialog display flow must remain unchanged
- Navigation home with cleared back stack must remain unchanged

**Scope:**
All inputs where `exerciseResults` is empty (resulting in legitimately 0 XP) should be completely unaffected by this fix. The fix only changes the _timing_ of when `persistCompletion()` is called — not what it does or what values it persists.

## Hypothesized Root Cause

Based on the code analysis, the root cause is clearly identified:

1. **Synchronous call ordering in onCreate()**: `onCreate()` calls `calculateResults()` then immediately calls `persistCompletion()`. Since `calculateResults()` performs an async DB call (`userRepository.getCurrentStreak()`), it returns immediately while the callback hasn't fired yet.

2. **No callback chaining**: There is no mechanism to defer `persistCompletion()` until the async computation completes. The two methods are called sequentially as if both were synchronous.

3. **Default field value**: `totalXp` is an int instance field defaulting to 0. When `persistCompletion()` reads it before the callback assigns it, it gets 0.

4. **Same pattern affects displaySummary()**: The initial `displaySummary()` call in `onCreate()` also runs before `totalXp` is computed, but this is partially masked because the async callback calls `displaySummary()` again after computing `totalXp` — so the UI eventually shows the correct value.

## Correctness Properties

Property 1: Bug Condition - XP Persisted After Calculation

_For any_ workout completion event where the exercise results are non-empty, the fixed `persistCompletion()` SHALL only execute after `totalXp` has been assigned by the `calculateResults()` async callback, ensuring the persisted XP value equals `XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier)`.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation - Non-Timing Behavior Unchanged

_For any_ workout completion event, the fixed code SHALL produce the same XP calculation result, the same streak update behavior, the same level-up/rank-up checks, and the same UI display as the original code — differing only in the timing of when `persistCompletion()` is invoked relative to `calculateResults()`.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

**File**: `android/app/src/main/java/com/jve/repsup/activities/WorkoutCompleteActivity.java`

**Function**: `onCreate()` and `calculateResults()`

**Specific Changes**:

1. **Remove `persistCompletion()` call from `onCreate()`**: The call `persistCompletion()` on line after `calculateResults()` must be removed from `onCreate()`.

2. **Remove initial `displaySummary()` call from `onCreate()`**: The `displaySummary()` call that runs before the async completes should be removed (or left as a placeholder showing "Calculating..." state). The async callback already calls `displaySummary()` after computing `totalXp`.

3. **Add `persistCompletion()` inside `calculateResults()` success callback**: After `totalXp`, `totalSets`, and `totalReps` are computed and `displaySummary()` is called on the UI thread, invoke `persistCompletion()`:
   ```java
   @Override
   public void onSuccess(Integer streak) {
       int currentStreak = streak != null ? streak : 0;
       double streakMultiplier = StreakManager.getMultiplier(currentStreak);
       totalXp = XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier);
       totalSets = 0;
       totalReps = 0;
       for (ExerciseResult result : exerciseResults) {
           totalSets += result.getSetsCompleted();
           totalReps += result.getTotalReps();
       }
       runOnUiThread(() -> displaySummary());
       persistCompletion();  // Now safe — totalXp is assigned
   }
   ```

4. **Add `persistCompletion()` inside `calculateResults()` error callback**: The error path also computes `totalXp` (with multiplier 1.0) before calling `displaySummary()`. Add `persistCompletion()` there too:
   ```java
   @Override
   public void onError(Exception e) {
       double streakMultiplier = 1.0;
       totalXp = XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier);
       totalSets = 0;
       totalReps = 0;
       for (ExerciseResult result : exerciseResults) {
           totalSets += result.getSetsCompleted();
           totalReps += result.getTotalReps();
       }
       runOnUiThread(() -> displaySummary());
       persistCompletion();  // Now safe — totalXp is assigned with fallback multiplier
   }
   ```

5. **Update `onCreate()` flow**: The final `onCreate()` should call:
   ```java
   extractExtras();
   initViews();
   calculateResults();  // This now triggers persistCompletion() internally
   loadMuscleGroups();  // Independent — can still run in parallel
   ```
   Note: `loadMuscleGroups()` is independent of `totalXp` (it only fetches muscle group names) so it can remain in `onCreate()`.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm that the race condition causes 0 XP to be persisted.

**Test Plan**: Instrument the code to log the value of `totalXp` at the moment `persistCompletion()` is called. Run on unfixed code to observe that `totalXp` is 0 at persistence time despite non-empty exercise results.

**Test Cases**:
1. **Standard Workout Test**: Complete a workout with 3 exercises (non-zero baseXp). Verify xp_log records 0 (will fail on unfixed code — demonstrates the bug)
2. **High Streak Test**: Complete a workout with streak=14 (1.5x multiplier). Verify XP is still persisted as 0 (will fail on unfixed code)
3. **Error Path Test**: Kill DB connection briefly during streak fetch so error callback fires. Verify XP is persisted as 0 (will fail on unfixed code)
4. **Multiple Sets Test**: Complete a workout with high sets/reps. Verify user.xp is not incremented (will fail on unfixed code)

**Expected Counterexamples**:
- `xp_log.xp_earned = 0` when it should be > 0
- `user.xp` not incremented (incremented by 0)
- Level-up not triggered when it should have been (XP threshold met but 0 was added)
- Possible cause: confirmed — `persistCompletion()` fires before async callback

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (non-empty exercise results), the fixed function persists the correct XP value.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := completedWorkout_fixed(input)
  ASSERT result.persistedXp == XPCalculator.calculateWorkoutXP(input.exerciseResults, streakMultiplier)
  ASSERT result.xpLogEntry.xpEarned == result.persistedXp
  ASSERT result.userXpDelta == result.persistedXp
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (empty exercise results, or non-XP-related behaviors), the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT completedWorkout_original(input).streakBehavior == completedWorkout_fixed(input).streakBehavior
  ASSERT completedWorkout_original(input).levelUpCheck == completedWorkout_fixed(input).levelUpCheck
  ASSERT completedWorkout_original(input).uiDisplay == completedWorkout_fixed(input).uiDisplay
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many combinations of exercise results and streak values
- It catches edge cases in XP formula calculation that manual tests might miss
- It provides strong guarantees that the XP formula and all side behaviors remain unchanged

**Test Plan**: Observe behavior on UNFIXED code for non-timing-dependent aspects (XP formula, streak logic, level-up logic), then write property-based tests capturing that behavior.

**Test Cases**:
1. **XP Formula Preservation**: Verify `calculateWorkoutXP()` returns the same value for any combination of exercise results and streak multiplier — formula is not changed
2. **Streak Update Preservation**: Verify streak increment logic (first-completion-only-per-day) continues to work correctly
3. **Level-Up Preservation**: Verify `shouldLevelUp()` and coin awards (50 for level, 100 for rank) continue to trigger at correct thresholds
4. **UI Display Preservation**: Verify the XP summary screen displays the correct computed XP value

### Unit Tests

- Test that `calculateResults()` callback assigns correct `totalXp` for various exercise result combinations
- Test that `persistCompletion()` is not callable/invocable before `totalXp` is assigned (structural check)
- Test XP formula: `sum(baseXp * setsCompleted) * streakMultiplier` for edge cases (single exercise, max exercises, multiplier=1.0, multiplier=1.5)
- Test empty exercise results correctly yields 0 XP (not a bug — correct behavior)

### Property-Based Tests

- Generate random lists of ExerciseResult (1-8 exercises, varying baseXp 10-100, sets 1-5) and random streak multipliers (1.0, 1.25, 1.5) — verify `calculateWorkoutXP()` always returns `sum(baseXp * sets) * multiplier`
- Generate random workout completions and verify that the ordering guarantee holds: `totalXp` is assigned before any persistence call reads it
- Generate random user XP totals and levels — verify `shouldLevelUp()` produces consistent results regardless of fix

### Integration Tests

- Complete a full workout flow end-to-end: schedule workout → start → complete all sets → verify xp_log, user.xp, and scheduled_workout all contain the correct non-zero XP value
- Complete a workout when streak fetch fails: verify fallback multiplier 1.0 is used AND the correct XP is persisted (not 0)
- Complete a workout that should trigger level-up: verify the level-up occurs because the correct XP was persisted (not 0)
