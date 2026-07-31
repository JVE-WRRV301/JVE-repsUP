# Database Schema Notes — Reps Up

## Overview
The database is called `reps_up` and is built in MySQL. 
The schema and seed data files are located in the `database/` folder.

## Key Design Decisions

### Circular Foreign Key (users ↔ avatars)
users.av_id references avatars.av_id, and avatars.user_id references 
users.user_id. To handle this:
- Both tables are created first without the circular foreign key
- avatars table is created after users
- The users.av_id foreign key is added after via ALTER TABLE

### Soft Deletes
No soft deletes are used — records are hard deleted with CASCADE rules 
set on foreign keys so child records are cleaned up automatically.

### Default Workouts
Workouts with user_id = NULL and is_default = TRUE are admin-created 
default workouts. These are used when a user requests a generated workout.

### Measurement System
Weight is always stored in kg and height always in cm regardless of what 
unit the user selected during assessment. Conversion happens in the app 
before saving.

### XP Logging
Every XP award is logged in xp_logs with src_type ('workout' or 'goal') 
and src_id so there is a full audit trail of how XP was earned.

### Streak Tracking
Two places track streaks:
- users.curr_streak — current active streak count
- streaks table — weekly history with multiplier values

### Cosmetics Ownership
user_cosmetics tracks which items a user owns (is_unlocked) and which 
are currently equipped (is_equipped). outfit_slots stores the single 
active outfit combination per user.

## Tables Summary
| Table              | Purpose                                      |
|--------------------|----------------------------------------------|
| users              | Core user accounts                           |
| user_profiles      | Fitness assessment data per user             |
| avatars            | Avatar appearance data                       |
| ranks              | Tier progression ladder (Bronze → Diamond)   |
| cosmetic_items     | All available cosmetic items                 |
| user_cosmetics     | Which items each user owns/has equipped      |
| outfit_slots       | Currently equipped outfit per user           |
| exercises          | Pre-populated exercise library               |
| workouts           | User and admin workouts                      |
| workout_exercises  | Exercises within each workout                |
| scheduled_workouts | Calendar entries per user                    |
| goals              | User fitness goals                           |
| xp_logs            | History of all XP earned                     |
| coin_transactions  | History of all coin spending                 |
| streaks            | Weekly streak history with multipliers       |
| admins             | Admin accounts                               |

## Files
- `database/schema.sql` — full table definitions
- `database/seed.sql` — test data and required defaults