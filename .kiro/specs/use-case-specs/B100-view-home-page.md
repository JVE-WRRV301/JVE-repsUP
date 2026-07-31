# Spec: B100 — View Home Page

## Overview
The home page is the main landing screen after login. It displays the user's 
avatar, level, streak, coins, a weekly calendar strip, today's scheduled workout, 
and the next upcoming goal. Users can start a workout from here.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into their account
- User is not currently on the home page

## Post-Conditions
- The system displays the home page with all user data populated

## Screen
Single Fragment: HomeFragment.java (inside MainActivity with bottom navigation)

**Header Section:**
- Avatar thumbnail (circular) with level badge (e.g. "Lv.10")
- Streak count with fire icon
- Coin count with coin icon

**Weekly Calendar Strip:**
- Shows S M T W T F S with day numbers
- Current day highlighted in light blue (#B2D6FD)
- Days with scheduled workouts marked with dots

**Today Section:**
- "TODAY" heading in Orbitron
- Card showing today's scheduled workout:
  - Day + date label
  - Workout name (e.g. "Chest, Shoulders and Triceps")
  - First 3 exercises with avatar demo thumbnails and set count
  - "and X more" if more than 3 exercises
  - "START" button (green) → invokes B110
  - Three-dot menu for options

**Next Goal Section:**
- "NEXT GOAL" heading in Orbitron
- Card showing the nearest upcoming goal:
  - Date label
  - Goal title
  - Priority level
  - Goal description/notes
  - Completion checkbox

**Bottom Navigation Bar:**
- Home (selected), Calendar, Workout, Me

## UI Requirements
- Dark grey background (#393944)
- Cards with slightly lighter background (dark_grey_variant)
- Light blue accent for selected nav items and highlights
- Red (#FF5050) border on goal cards
- Orbitron font for section headings
- Inter font for body text

## Files to Create
- `android/app/src/main/java/com/jve/repsup/fragments/HomeFragment.java`
- `android/app/src/main/res/layout/fragment_home.xml`
- `android/app/src/main/res/layout/item_workout_card.xml`
- `android/app/src/main/res/layout/item_goal_card.xml`

## Database Operations
1. SELECT u.level, u.curr_streak, u.coins, u.av_id FROM users u WHERE u.user_id = ?
2. SELECT sw.*, w.w_name FROM scheduled_workouts sw JOIN workouts w ON sw.w_id = w.w_id WHERE sw.user_id = ? AND sw.sched_date = CURDATE()
3. SELECT we.*, e.ex_name FROM workout_exercises we JOIN exercises e ON we.ex_id = e.ex_id WHERE we.w_id = ? ORDER BY we.w_ex_order_index
4. SELECT * FROM goals WHERE user_id = ? AND is_comp = false ORDER BY deadline ASC LIMIT 1

## Navigation
- Entry point: bottom nav "Home" tab, or after login/assessment completion
- B110 can be called from the "START" button on the workout card

## Reference Designs
See designs/ folder — home page with avatar header, calendar strip, today workout, and next goal.
