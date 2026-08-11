# Spec: B230 — Edit Calendar

## Overview
The user taps on a day in the calendar that has a scheduled workout or goal 
to edit its details. The system displays an edit form matching the original 
scheduling interface.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the schedule page
- The selected day has either a goal or a workout scheduled

## Post-Conditions
- The system saves changes to the chosen day's scheduled item(s)

## Screen
Reuses the schedule/goal form layouts with "EDIT" headings

**Edit Workout Tab:**
- "EDIT YOUR WORKOUT" heading in Orbitron
- Date field (pre-filled, editable via calendar picker)
- Repeat setting (pre-filled)
- "CHOOSE WORKOUT" list with current selection checked
- User can change the workout or the date
- Tick button to save changes

**Edit Goal Tab:**
- "EDIT YOUR GOAL" heading in Orbitron
- Title (pre-filled)
- Date (pre-filled, editable)
- Importance (pre-filled)
- Notes (pre-filled)
- Tick button to save changes

## Basic Flow
1. User taps on a day with a scheduled item in the calendar
2. System opens the edit form with two tabs (Workout | Goal)
3. The appropriate tab is pre-selected based on what exists on that day
4. User edits desired fields
5. User taps the tick/save button
6. System updates the database and refreshes the calendar

## Files to Create
- Reuses `ScheduleWorkoutActivity.java` and `SetGoalActivity.java` in edit mode
- Edit mode triggered by passing item ID as intent extra

## Database Operations
**Edit Workout:**
1. UPDATE scheduled_workouts SET w_id = ?, sched_date = ? WHERE user_id = ? AND sched_date = [original_date]

**Edit Goal:**
1. UPDATE goals SET description = ?, deadline = ?, priority = ? WHERE goal_id = ?

## Navigation
- Entry point: tapping a day with scheduled items on the calendar (B200)
- Exit point: tick/save → returns to Schedule Page with updated calendar

## Reference Designs
See designs/ folder — edit workout and edit goal forms with pre-filled data.
