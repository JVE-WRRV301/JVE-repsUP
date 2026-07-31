# Spec: B221 — Set Goal

## Overview
The user creates a new fitness goal by entering a title, deadline date, 
importance level, and optional notes. The goal appears on the calendar and 
in the goals list.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the schedule page
- User has clicked the add (+) button and selected the Goal tab

## Post-Conditions
- A new goal is inserted into the goals table
- The goal appears on the calendar on its deadline date
- The goal appears in the Goals tab list

## Screen
Activity or dialog: SetGoalActivity.java

**Layout:**
- Tab toggle: "Workout" | "Goal" (selected)
- "CREATE YOUR GOAL" heading in Orbitron
- Title input field
- Date field → opens calendar date picker
- Importance dropdown: "Top Priority", "Medium Priority", "Low Priority"
- Notes text area (multiline input)
- Tick button (top right) to confirm and save

## UI Requirements
- Dark grey background (#393944)
- Light blue (#B2D6FD) for selected Goal tab
- Calendar picker in dark theme
- Input fields with rounded borders

## Validation Rules
- Title: required, non-empty
- Date: required, must be today or in the future
- Importance: required, must select one
- Notes: optional

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/SetGoalActivity.java`
- `android/app/src/main/res/layout/activity_set_goal.xml`

## Database Operations
1. INSERT INTO goals (user_id, description, deadline, priority, is_comp) VALUES (?, ?, ?, ?, false)

## Navigation
- Entry point: plus (+) button on Schedule Page (B200), Goal tab selected
- Exit point: tick/confirm button → returns to Schedule Page with goal added

## Reference Designs
See designs/ folder — create goal form with title, date picker, importance dropdown, and notes.
