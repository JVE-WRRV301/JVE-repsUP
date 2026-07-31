# Spec: B200 — View Schedule Page

## Overview
The schedule page displays a full monthly calendar with workout and goal 
indicators. Below the calendar, users can toggle between "My Plan" (weekly 
workouts) and "Goals" tabs.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is not on the schedule page

## Post-Conditions
- The system displays the schedule page with calendar and plan/goals tabs

## Screen
Single Fragment: ScheduleFragment.java (inside MainActivity with bottom navigation)

**Header:**
- Avatar thumbnail with level badge
- Streak count with fire icon
- Coin count with coin icon

**Calendar Section:**
- "MY SCHEDULE" heading in Orbitron
- Plus button (+) → opens schedule workout/goal form (B210/B221)
- Month/year navigation (< September 2026 >)
- Full month grid (sun–sat)
- Days with workouts highlighted in light blue
- Days with goals highlighted in red
- Current day has a distinct marker
- Tapping a day shows that day's scheduled items

**Tab Section (below calendar):**
- "My Plan" tab | "Goals" tab
- My Plan: shows current week's workouts (same as B240)
- Goals: shows list of goals (same as B220)

## UI Requirements
- Dark grey background (#393944)
- Calendar grid with light blue (#B2D6FD) for workout days
- Red (#FF5050) for goal deadline days
- Yellow (#FAD942) for current day highlight
- Orbitron for "MY SCHEDULE" heading
- Inter for calendar text and tab content

## Files to Create
- `android/app/src/main/java/com/jve/repsup/fragments/ScheduleFragment.java`
- `android/app/src/main/res/layout/fragment_schedule.xml`
- `android/app/src/main/java/com/jve/repsup/adapters/CalendarAdapter.java`

## Database Operations
1. SELECT sched_date, is_comp FROM scheduled_workouts WHERE user_id = ? AND MONTH(sched_date) = ? AND YEAR(sched_date) = ?
2. SELECT deadline FROM goals WHERE user_id = ? AND MONTH(deadline) = ? AND YEAR(deadline) = ?
3. SELECT sw.*, w.w_name FROM scheduled_workouts sw JOIN workouts w ON sw.w_id = w.w_id WHERE sw.user_id = ? AND WEEK(sw.sched_date) = WEEK(CURDATE())

## Navigation
- Entry point: bottom nav "Calendar" tab
- Plus button → Schedule Workout (B210) or Set Goal (B221) form
- Tapping a scheduled day → Edit Calendar (B230)

## Reference Designs
See designs/ folder — schedule page with full month calendar, My Plan/Goals tabs.
