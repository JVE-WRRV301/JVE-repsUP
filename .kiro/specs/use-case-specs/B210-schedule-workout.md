# Spec: B210 — Schedule Workout

## Overview
The user schedules one of their saved workouts for a specific date with an 
optional repeat setting. The scheduled workout appears on the calendar.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the schedule page
- User has clicked the add (+) button and selected the Workout tab

## Post-Conditions
- The selected workout is added to the calendar for the selected date(s)
- A new scheduled_workouts row is inserted

## Screen
Activity or dialog: ScheduleWorkoutActivity.java

**Layout:**
- Tab toggle: "Workout" (selected) | "Goal"
- "SCHEDULE YOUR WORKOUT" heading in Orbitron
- Date field → opens calendar date picker
- Repeat dropdown: "Once", "Every week", "Every 2 weeks"
- "CHOOSE WORKOUT" section:
  - List of user's saved workouts
  - Each with workout name and + button to select
  - Selected workout shows a checkmark
- Tick button (top right) to confirm and save

## UI Requirements
- Dark grey background (#393944)
- Light blue (#B2D6FD) for selected workout tab and date highlights
- Calendar picker same style as main calendar
- Workout list items in card style

## Validation Rules
- Date must be selected (today or future)
- At least one workout must be selected
- Cannot schedule the same workout on the same day twice

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/ScheduleWorkoutActivity.java`
- `android/app/src/main/res/layout/activity_schedule_workout.xml`

## Database Operations
1. SELECT w.w_id, w.w_name FROM workouts WHERE user_id = ? AND is_default = false ORDER BY w_order_index
2. INSERT INTO scheduled_workouts (user_id, w_id, sched_date, is_comp, comp_date, earned_xp, comp_sets, comp_reps) VALUES (?, ?, ?, false, NULL, 0, 0, 0)
3. For repeat "Every week": insert multiple rows for each week within a reasonable range

## Navigation
- Entry point: plus (+) button on Schedule Page (B200), Workout tab selected
- Exit point: tick/confirm button → returns to Schedule Page with calendar updated

## Reference Designs
See designs/ folder — schedule workout form with date picker, repeat setting, and workout list.
