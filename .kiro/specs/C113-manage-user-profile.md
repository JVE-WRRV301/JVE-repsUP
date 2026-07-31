# Spec: C113 — Manage User Profile

## Overview
The user accesses settings from the avatar page to view and modify their 
personal details, account information, measurement system preference, 
language, and to log out of the application.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User is on the avatar page (C100)

## Post-Conditions
- The user's changed settings are saved to the database
- Or the user logs out and is returned to the login screen

## Screen
Activity: SettingsActivity.java

**Layout:**
- Back arrow (top left) → returns to avatar page
- "SETTINGS" heading in Orbitron

**User Section:**
- "Personal Details" row → opens personal details sub-screen
  - Name (editable)
  - Email (editable)
  - Username (editable)
- "Account" row → opens account sub-screen
  - Change password
  - Delete account (with confirmation)

**Application Section:**
- "Measurement System" row → toggle metric/imperial
- "Language" row → currently "English" (future expansion)

**Log Out:**
- "Log out" button at bottom
- Confirmation dialog: "WARNING: Are you sure you want to log out?"
  - "Cancel" button | "Log out" button

## UI Requirements
- Dark grey background (#393944)
- Section headings in white
- Row items with right chevron (>)
- Orbitron for "SETTINGS" heading
- Inter for all other text
- Log out button centred, rounded

## Validation Rules
- Name: required, non-empty
- Email: required, valid format, unique
- Username: required, unique
- Password change: old password must match, new password minimum 8 chars

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/SettingsActivity.java`
- `android/app/src/main/res/layout/activity_settings.xml`
- `android/app/src/main/java/com/jve/repsup/dialogs/LogoutDialog.java`
- `android/app/src/main/res/layout/dialog_logout.xml`

## Database Operations
**Personal Details:**
1. UPDATE users SET name = ?, email = ?, username = ? WHERE user_id = ?

**Account - Change Password:**
1. SELECT password FROM users WHERE user_id = ?
2. UPDATE users SET password = [new_hashed] WHERE user_id = ?

**Measurement System:**
1. UPDATE users SET meas_system = ? WHERE user_id = ?

**Log Out:**
1. Clear SessionManager data (no DB write, just local session clear)

## Navigation
- Entry point: settings button (gear icon) on Avatar Page (C100)
- Back arrow → returns to Avatar Page
- Log out → clears session → LoginActivity

## Reference Designs
See designs/ folder — settings page with user/application sections and log out button.
