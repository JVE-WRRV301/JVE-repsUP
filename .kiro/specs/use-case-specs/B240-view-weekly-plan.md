# Spec: B240 — View Weekly Plan

## Overview
On the schedule page, the user selects the "My Plan" tab to view a list of 
the current week's workouts in order, showing which are completed and which 
are upcoming.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the schedule page

## Post-Conditions
- The list below the calendar displays the current week's workouts

## Screen
Part of ScheduleFragment.java — My Plan tab content

**Weekly Plan List:**
- "Week [X]" collapsible section header
- Each workout displayed as a card:
  - Day + date label (e.g. "4 Wednesday")
  - Workout name in bold (e.g. "Chest, Shoulders and Triceps")
  - First 3 exercises with avatar thumbnails and set count
  - "and X more" if more than 3 exercises
  - "START" button (green, only for today's workout) → B110
  - "Completed" label (green) for finished workouts
  - Three-dot menu (top right) → edit/reschedule options

## UI Requirements
- Dark grey background (#393944)
- Workout cards with dark_grey_variant background
- Green "START" button for today's workout
- Green "Completed" label for done workouts
- Greyed-out cards for past uncompleted workouts
- Orbitron for section headings
- Inter for workout content

## Files to Create
- Weekly plan adapter: `android/app/src/main/java/com/jve/repsup/adapters/WeeklyPlanAdapter.java`
- Weekly plan item layout: `android/app/src/main/res/layout/item_weekly_workout.xml`

## Database Operations
1. SELECT sw.*, w.w_name FROM scheduled_workouts sw JOIN workouts w ON sw.w_id = w.w_id WHERE sw.user_id = ? AND YEARWEEK(sw.sched_date) = YEARWEEK(CURDATE()) ORDER BY sw.sched_date
2. SELECT we.ex_id, e.ex_name, we.sets FROM workout_exercises we JOIN exercises e ON we.ex_id = e.ex_id WHERE we.w_id = ? ORDER BY we.w_ex_order_index LIMIT 3

## Navigation
- Entry point: user clicks "My Plan" tab on Schedule Page (B200)
- "START" button on today's workout → B110 (Start Workout)
- Three-dot menu → edit/reschedule options

## Reference Designs
See designs/ folder — My Plan tab with weekly workout cards showing exercises and status.
