# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - XP Persisted As Zero Due To Race Condition
  - **IMPORTANT**: Write this property-based test BEFORE implementing the fix
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Scope the property to concrete failing cases — any non-empty exerciseResults list with valid baseXp and setsCompleted values
  - **File**: `android/app/src/test/java/com/jve/repsup/WorkoutCompleteRaceConditionTest.java`
  - Simulate the race condition: call `persistCompletion()` before `calculateResults()` callback completes
  - Test that for any non-empty exerciseResults (isBugCondition: exerciseResults IS NOT EMPTY AND persistCompletion called before callback), the persisted XP should equal `XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier)` (not 0)
  - On UNFIXED code: `totalXp` is 0 at persistence time, so assertions that XP > 0 will FAIL
  - Document counterexamples: e.g., "3 exercises with baseXp=10, sets=3 should persist 90 XP but persists 0"
  - Run test on UNFIXED code — expect FAILURE (confirms the race condition exists)
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - XP Calculation Formula And Side Behaviors Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - **File**: `android/app/src/test/java/com/jve/repsup/WorkoutXPCalculatorTest.java` (extend existing)
  - Observe on UNFIXED code: `calculateWorkoutXP([ExerciseResult(1, 10, 3, 30, 50.0)], 1.0)` returns 30
  - Observe on UNFIXED code: `calculateWorkoutXP([ExerciseResult(1, 10, 3, 30, 50.0), ExerciseResult(2, 20, 4, 40, 80.0)], 1.5)` returns 165
  - Observe on UNFIXED code: `calculateWorkoutXP([], 1.5)` returns 0 (empty results correctly yield 0)
  - Observe on UNFIXED code: `calculateWorkoutXP(results, 0.5)` clamps multiplier to 1.0
  - Write property-based tests: for all non-empty exerciseResults lists (1-8 exercises, baseXp 10-100, sets 1-5) and valid multipliers (1.0, 1.25, 1.5), result equals `sum(baseXp * setsCompleted) * streakMultiplier` truncated to int
  - Write property-based test: for all empty/null exerciseResults, result is always 0 regardless of multiplier
  - Write preservation test: `shouldLevelUp(totalXp, currentLevel)` returns true when `totalXp >= 500 * (2^currentLevel - 1)`
  - Verify all tests PASS on UNFIXED code (these behaviors are not affected by the timing bug)
  - **EXPECTED OUTCOME**: Tests PASS (confirms baseline behavior to preserve)
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 3. Fix XP log race condition in WorkoutCompleteActivity

  - [x] 3.1 Implement the fix
    - Remove `persistCompletion()` call from `onCreate()` method
    - Remove initial `displaySummary()` call from `onCreate()` (the async callback already calls it after computing totalXp)
    - Add `persistCompletion()` call inside `calculateResults()` success callback, after `totalXp`, `totalSets`, and `totalReps` are assigned and `displaySummary()` is queued
    - Add `persistCompletion()` call inside `calculateResults()` error callback, after the fallback multiplier 1.0 is used and values are computed
    - Keep `loadMuscleGroups()` in `onCreate()` as it is independent of `totalXp`
    - Final `onCreate()` flow: `extractExtras()` → `initViews()` → `calculateResults()` → `loadMuscleGroups()`
    - _Bug_Condition: isBugCondition(input) where exerciseResults IS NOT EMPTY AND persistCompletion() called before calculateResults callback completes AND totalXp == 0 at persistence time_
    - _Expected_Behavior: persistCompletion() SHALL only execute after totalXp has been assigned by the calculateResults() async callback, ensuring persisted XP == XPCalculator.calculateWorkoutXP(exerciseResults, streakMultiplier)_
    - _Preservation: XP formula, streak fallback to 1.0, UI display, streak updates, level-up (50 coins), rank-up (100 coins), dialog flow, and navigation all remain unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - XP Persisted After Calculation
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior: persisted XP equals calculated XP
    - After fix, `persistCompletion()` runs inside the callback where `totalXp` is already assigned
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed — XP is no longer 0)
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - XP Calculation Formula And Side Behaviors Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions to XP formula, level-up logic, or streak behavior)
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint - Ensure all tests pass
  - Run full test suite: `./gradlew test` from `android/` directory
  - Confirm WorkoutCompleteRaceConditionTest passes (bug is fixed)
  - Confirm WorkoutXPCalculatorTest passes (preservation intact)
  - Ensure no compilation errors in WorkoutCompleteActivity.java
  - Ensure all tests pass, ask the user if questions arise
