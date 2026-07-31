# Spec: B220 — View Goals

## Overview
On the schedule page, the user selects the "Goals" tab to view a list of their 
active goals with details including deadline, priority, and description.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the schedule page

## Post-Conditions
- The system displays the user's goals below the calendar in a scrollable list

## Screen
Part of ScheduleFragment.java — Goals tab content

**Goals List:**
- Each goal displayed as a card with:
  - Deadline date (e.g. "15 Sunday")
  - Goal title in bold (e.g. "Move up a weight in bicep curls")
  - Priority label (e.g. "Medium Priority")
  - Description/notes text
  - Completion checkbox (tick circle) → invokes B222
  - Three-dot menu (top right) → invokes B223
- Cards have red (#FF5050) left border/accent
- Goals ordered by deadline (earliest first)

## UI Requirements
- Dark grey background (#393944)
- Goal cards with dark_grey_variant background
- Red (#FF5050) accent for priority/border
- Light blue (#B2D6FD) for completion checkbox when ticked
- Orbitron for tab heading
- Inter for goal content

## Files to Create
- Goal list adapter: `android/app/src/main/java/com/jve/repsup/adapters/GoalAdapter.java`
- Goal list item layout: `android/app/src/main/res/layout/item_goal.xml`

## Database Operations
1. SELECT * FROM goals WHERE user_id = ? AND is_comp = false ORDER BY deadline ASC

## Navigation
- Entry point: user clicks "Goals" tab on Schedule Page (B200)
- Three-dot menu → B223 (Manage Goals)
- Checkbox → B222 (Complete Goal)

## Reference Designs
See designs/ folder — goals tab with goal cards showing deadline, priority, and description.
