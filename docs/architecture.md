# Architecture — Reps Up

## Overview
Reps Up is an Android (Java) application that connects directly to a 
MySQL database via JDBC. There is no separate backend server — the app 
communicates with the database directly using secure credentials stored 
locally on each developer's machine.

## Architecture Diagram
Android App (Java)
|
| JDBC
|
MySQL Database (localhost / shared server)
|
Local Assets (Avatar PNGs in res/drawable)

## Layers

### 1. UI Layer
- Activities — full screen flows
- Fragments — tab content
- XML Layouts — screen designs
- Dialogs — popup overlays

### 2. Repository Layer
- One repository class per major feature
- All database queries live here
- Activities and Fragments never touch the database directly

### 3. Database Layer
- DatabaseHelper.java — singleton JDBC connection manager
- ConnectionManager.java — handles opening/closing connections safely
- Credentials loaded from BuildConfig (injected from local.properties)

### 4. Utility Layer
- XPCalculator.java — XP and coin calculations
- StreakManager.java — streak increment/reset logic
- AvatarCompositor.java — layers avatar PNG images using Android Canvas
- SessionManager.java — stores logged-in user session in SharedPreferences

## Database Connection
- Emulator connects to host machine MySQL via IP: 10.0.2.2
- Port: 3306
- Database name: reps_up
- Credentials: stored in local.properties (gitignored)

## Navigation Flow

WelcomeActivity
→ LoginActivity
→ RegisterActivity
→ FitnessAssessmentActivity
→ GenerateAvatarActivity
→ MainActivity (Bottom Nav Host)
→ HomeFragment
→ CalendarFragment
→ WorkoutFragment
→ MeFragment

AdminLoginActivity
→ AdminMainActivity
→ ManageWorkoutsFragment
→ CreateExerciseFragment

## Key Business Rules Affecting Architecture
- Max 8 exercises per workout
- Streak resets on missed scheduled workout
- XP multiplier applied every 14 day streak milestone
- Users can only equip owned cosmetic items
- Workout only completable if scheduled for today