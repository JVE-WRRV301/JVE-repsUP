# Bugfix Requirements Document

## Introduction

When a user completes a workout, the XP log in the database records 0 XP instead of the correctly calculated amount. This occurs because `WorkoutCompleteActivity.onCreate()` calls `persistCompletion()` synchronously after `calculateResults()`, but `calculateResults()` performs an asynchronous database call to fetch the user's streak before computing `totalXp`. The persistence logic fires before the async callback assigns the calculated value, so `totalXp` is still at its default value of 0 when it gets written to the database.

The UI eventually displays the correct XP (the async callback updates the display), but the database records — scheduled_workout completion, user total XP, and XP log — all persist with 0.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a workout is completed THEN the system persists `totalXp = 0` to the scheduled_workout record because `persistCompletion()` executes before the async streak-fetch callback in `calculateResults()` assigns the computed XP value

1.2 WHEN a workout is completed THEN the system updates the user's total XP by 0 because `userRepository.updateXp()` is called with `totalXp` before the async calculation completes

1.3 WHEN a workout is completed THEN the system inserts an XP log entry with `xp_earned = 0` because `userRepository.insertXpLog()` is called with `totalXp` before the async calculation completes

1.4 WHEN a workout is completed THEN the system checks for level-up against a stale user XP total (incremented by 0) so level-ups that should occur are missed

### Expected Behavior (Correct)

2.1 WHEN a workout is completed THEN the system SHALL persist the correctly calculated XP value (sum of baseXp × setsCompleted per exercise × streakMultiplier) to the scheduled_workout record

2.2 WHEN a workout is completed THEN the system SHALL update the user's total XP by the correctly calculated workout XP amount

2.3 WHEN a workout is completed THEN the system SHALL insert an XP log entry with the correctly calculated XP value as both `xp_earned` and `xp_after` delta

2.4 WHEN a workout is completed THEN the system SHALL check for level-up only after the user's XP has been correctly updated with the full calculated amount

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a workout is completed with valid exercise results THEN the system SHALL CONTINUE TO calculate XP using the formula sum(baseXp × setsCompleted) × streakMultiplier

3.2 WHEN the streak cannot be retrieved (error case) THEN the system SHALL CONTINUE TO fall back to a streak multiplier of 1.0

3.3 WHEN a workout is completed THEN the system SHALL CONTINUE TO display the correct XP value in the UI summary screen

3.4 WHEN a workout is completed THEN the system SHALL CONTINUE TO mark the scheduled_workout as completed with correct totalSets and totalReps values

3.5 WHEN a workout is completed THEN the system SHALL CONTINUE TO update the user's streak (incrementing only on first completion of the day)

3.6 WHEN a level-up occurs THEN the system SHALL CONTINUE TO award 50 coins and show the LevelUpDialog

3.7 WHEN a rank promotion occurs THEN the system SHALL CONTINUE TO award 100 bonus coins and show the RankUpDialog
