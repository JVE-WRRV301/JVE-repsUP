# Spec: B223 — Manage Goals

## Overview
The user can edit or delete an existing goal from the goals list by tapping 
the three-dot menu on a goal card.

## Owner
Vincent van Niekerk

## Pre-Conditions
- User is registered and logged into the system
- User is on the goals tab on the schedule page
- User has at least one goal

## Post-Conditions
- The goal is either updated (edited) or removed (deleted) from the database

## Screen
Uses a popup menu + edit form or confirmation dialog

**Three-dot Menu Options:**
- Edit Goal
- Delete Goal

**Edit Goal Form (reuses SetGoalActivity layout):**
- Pre-filled with existing goal data
- Same fields: Title, Date, Importance, Notes
- "Goal" tab header with "EDIT YOUR GOAL" heading
- Tick button to save changes

**Delete Confirmation Dialog:**
- "Are you sure you want to delete this goal?"
- "Cancel" button (dark) | "Delete" button (red)

## Basic Flow
**Flow 1 — Edit:**
1. User taps three-dot menu on a goal card
2. User selects "Edit Goal"
3. System opens the edit form with pre-filled data
4. User modifies desired fields
5. User taps the tick/save button
6. System updates the goal and refreshes the list

**Flow 2 — Delete:**
1. User taps three-dot menu on a goal card
2. User selects "Delete Goal"
3. System shows confirmation dialog
4. User clicks "Delete"
5. System removes the goal from the database
6. Goal list refreshes

## Files to Create
- Edit reuses: `SetGoalActivity.java` (with edit mode flag)
- `android/app/src/main/java/com/jve/repsup/dialogs/ConfirmDeleteDialog.java`
- `android/app/src/main/res/layout/dialog_confirm_delete.xml`

## Database Operations
**Edit:**
1. UPDATE goals SET description = ?, deadline = ?, priority = ? WHERE goal_id = ?

**Delete:**
1. DELETE FROM goals WHERE goal_id = ?

## Navigation
- Entry point: three-dot menu on a goal card in Goals tab (B220)
- Exit point: save/delete → returns to Goals tab with refreshed data

## Reference Designs
See designs/ folder — edit goal form and delete confirmation dialog.
