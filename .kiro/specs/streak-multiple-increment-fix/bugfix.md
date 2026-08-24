# Bugfix Requirements Document

## Introduction

When a user completes more than one scheduled workout on the same day, the streak counter increments for each completion. This inflates the streak value incorrectly — the business rule is that a streak increases by at most 1 per calendar day, reflecting whether the user trained that day or not. The root cause is that `WorkoutCompleteActivity.persistCompletion()` unconditionally calls `StreakManager.evaluateStreak(currentStreak, true, true)` on every workout completion without checking whether the streak has already been incremented for the current day.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a user completes a second (or subsequent) scheduled workout on the same calendar day THEN the system increments `current_streak` again, resulting in the streak increasing by more than 1 for that day.

1.2 WHEN a user has N scheduled workouts on a single day and completes all N THEN the system increments the streak N times, yielding a streak increase of N instead of 1.

### Expected Behavior (Correct)

2.1 WHEN a user completes a second (or subsequent) scheduled workout on the same calendar day THEN the system SHALL NOT increment `current_streak` beyond the +1 already applied for that day.

2.2 WHEN a user has N scheduled workouts on a single day and completes all N THEN the system SHALL increment the streak by exactly 1 for that calendar day regardless of how many workouts were completed.

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a user completes their first scheduled workout of the day (and the streak has not yet been incremented today) THEN the system SHALL CONTINUE TO increment `current_streak` by 1.

3.2 WHEN a user has no scheduled workouts for a given day (rest day) THEN the system SHALL CONTINUE TO leave `current_streak` unchanged.

3.3 WHEN a user has a scheduled workout for a given day but does not complete it THEN the system SHALL CONTINUE TO reset `current_streak` to 0 during the next streak evaluation.

3.4 WHEN a user completes a workout THEN the system SHALL CONTINUE TO award XP using the streak multiplier derived from the current streak value at the time of completion.

3.5 WHEN a user's streak reaches a 14-day milestone THEN the system SHALL CONTINUE TO show the streak milestone dialog and apply the appropriate XP multiplier.
