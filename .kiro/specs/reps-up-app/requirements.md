# Requirements Document

## Introduction

Reps Up is a gamified Android fitness application that combines workout tracking, scheduling, goal management, and avatar customization with a progression system (XP, coins, levels, ranks, streaks). The app targets users who struggle with fitness consistency by providing game-like motivation mechanics. The system connects to a MySQL database via JDBC, features a dark-themed UI, and includes an admin portal for managing default workouts and exercises. Three developers are building the app for delivery before September 2026 integration.

## Glossary

- **App**: The Reps Up Android application running on user devices
- **User**: A registered person who uses the App to track workouts and progress
- **Admin**: A privileged user (Dean) who manages the default workout and exercise library
- **Session_Manager**: The component responsible for persisting and validating user login sessions using SharedPreferences
- **Database_Helper**: The component responsible for establishing and managing JDBC connections to the MySQL database
- **User_Repository**: The data access component responsible for CRUD operations on User, User_Profile, and authentication records
- **Workout_Repository**: The data access component responsible for CRUD operations on Workout and Workout_Exercise records
- **Goal_Repository**: The data access component responsible for CRUD operations on Goal records
- **Schedule_Repository**: The data access component responsible for CRUD operations on Scheduled_Workout records
- **XP_Calculator**: The component responsible for computing XP awards based on exercise base_xp, sets completed, and active multipliers
- **Streak_Manager**: The component responsible for evaluating daily workout completion and updating streak counts
- **Avatar_Compositor**: The component responsible for layering PNG assets (base body, clothing, accessories) into a composite avatar image
- **Navigation_Shell**: The MainActivity host containing BottomNavigationView and the persistent top bar
- **Assessment_Flow**: The sequence of 9 onboarding screens collecting user fitness profile data
- **Workout_Execution**: The screen flow for logging sets, reps, and weight during an active workout session
- **Locker**: The in-app store where users browse and purchase cosmetic items using earned coins
- **Rank**: A progression tier (e.g., Bronze, Silver, Gold) unlocked at specific level thresholds
- **Streak**: The count of consecutive days with a completed scheduled workout
- **XP_Multiplier**: A scaling factor applied to XP earnings, increased at 14-day streak milestones

## Requirements

### Requirement 1: Secure Database Connection

**User Story:** As a developer, I want database credentials loaded from local.properties via BuildConfig, so that secrets are never hardcoded or committed to version control.

#### Acceptance Criteria

1. THE Database_Helper SHALL load connection parameters (host, port, user, password, database name) exclusively from BuildConfig fields injected at build time
2. WHEN the App is built, THE build system SHALL read db.host, db.port, db.user, db.password, and db.name from android/local.properties and expose them as BuildConfig string fields
3. IF local.properties is missing any required database field, THEN THE build system SHALL fail with a descriptive error message
4. THE App SHALL include local.properties in .gitignore to prevent credential leakage

### Requirement 2: User Authentication — Login

**User Story:** As a user, I want to log in with my username and password, so that I can access my workouts and progress.

#### Acceptance Criteria

1. WHEN a user submits valid credentials, THE User_Repository SHALL authenticate against the User table and THE Session_Manager SHALL store the user_id
2. WHEN authentication succeeds, THE App SHALL navigate from LoginActivity to MainActivity
3. IF a user submits invalid credentials, THEN THE App SHALL display an error message without revealing which field was incorrect
4. WHEN the App launches with an existing session, THE Session_Manager SHALL bypass LoginActivity and navigate directly to MainActivity
5. THE LoginActivity SHALL display a dark top section with a gym image and "Welcome back!" text, and a white bottom card containing username and password input fields

### Requirement 3: User Authentication — Register

**User Story:** As a new user, I want to create an account, so that I can start using the fitness app.

#### Acceptance Criteria

1. WHEN a user submits a valid registration form (Name, Username, Email, Password, Confirm Password), THE User_Repository SHALL insert a new User record with default values (xp=0, coins=0, level=1, rank_id=1, streak=0)
2. IF the username already exists in the User table, THEN THE App SHALL display a "username taken" error
3. IF the password and confirm password fields do not match, THEN THE App SHALL display a validation error
4. IF any required field is empty, THEN THE App SHALL display a field-specific validation error
5. WHEN registration succeeds, THE Session_Manager SHALL store the new user_id and THE App SHALL navigate to the Assessment_Flow
6. THE WelcomeActivity SHALL be the launcher activity, displaying "Welcome to REPS UP" with Sign Up and Login navigation options

### Requirement 4: Fitness Assessment Onboarding

**User Story:** As a new user, I want to complete a fitness assessment, so that the app can personalize my experience and generate my avatar.

#### Acceptance Criteria

1. THE Assessment_Flow SHALL present 9 sequential steps: Welcome, Gender, Age, Weight, Height, Body Type, Workout Level, Workout Schedule, and Fitness Goals
2. THE Assessment_Flow SHALL display a segmented progress bar indicating the current step
3. WHEN the user completes all 9 steps and confirms, THE App SHALL insert a User_Profile record with the collected data
4. WHILE the user is in the Assessment_Flow, THE App SHALL preserve previously entered values when navigating backward
5. THE Weight step SHALL provide a horizontal ruler slider with a kg/lbs toggle
6. THE Height step SHALL provide a horizontal ruler slider with a cm/ft toggle
7. THE Body Type step SHALL display 6 body type images in a 2x3 grid for selection
8. THE Fitness Goals step SHALL allow multi-selection from: Lose weight, Stay Consistent, Build muscle, Get fitter, Improve endurance

### Requirement 5: Avatar Generation

**User Story:** As a new user, I want the app to generate an avatar based on my assessment data, so that I have a personalized visual identity.

#### Acceptance Criteria

1. WHEN the Assessment_Flow completes, THE Avatar_Compositor SHALL generate a default avatar by layering a base body PNG (selected by gender and body type) with default outfit overlays (white tank, black shorts, white sneakers)
2. WHEN the avatar is generated, THE App SHALL insert an Avatar record, link default cosmetic items as equipped in User_Cosmetic, and update the User record with the new avatar_id
3. THE App SHALL display a "MEET YOUR AVATAR!" screen showing the generated avatar with a "Continue" button that navigates to MainActivity

### Requirement 6: Navigation Shell and Session

**User Story:** As a user, I want a bottom navigation bar with four tabs, so that I can easily move between Home, Calendar, Workout, and Me sections.

#### Acceptance Criteria

1. THE Navigation_Shell SHALL display a BottomNavigationView with four tabs: Home, Calendar, Workout, and Me
2. WHEN a tab is selected, THE Navigation_Shell SHALL switch to the corresponding fragment without reloading previously visited fragments
3. THE Navigation_Shell SHALL display a persistent top bar showing the user's avatar thumbnail, level indicator, streak fire icon with count, and coin count
4. WHEN no active session exists at App launch, THE App SHALL redirect to LoginActivity

### Requirement 7: Home Page

**User Story:** As a user, I want to see my scheduled workout and next goal on the home page, so that I know what to do today.

#### Acceptance Criteria

1. THE HomeFragment SHALL display a week strip showing the current week (S M T W T F S) with today highlighted
2. THE HomeFragment SHALL display a "TODAY" card (blue border) showing the workout scheduled for today, including workout name, date, first 3 exercises with images and set counts, and a "START" button
3. THE HomeFragment SHALL display a "NEXT GOAL" card (red border) showing the nearest upcoming goal with deadline, title, priority badge, and description
4. WHEN no workout is scheduled for today, THE HomeFragment SHALL display a rest day empty state in the TODAY card
5. WHEN no goals exist, THE HomeFragment SHALL display an empty state in the NEXT GOAL card
6. WHEN the user taps "START" on the TODAY card, THE App SHALL navigate to the Workout_Execution for that scheduled workout

### Requirement 8: Goal Management

**User Story:** As a user, I want to create, view, and complete fitness goals, so that I can stay motivated toward specific targets.

#### Acceptance Criteria

1. WHEN a user creates a goal with Title, Date, Importance (Top/Medium/Low), and optional Notes, THE Goal_Repository SHALL insert a Goal record
2. IF the Title or Date field is empty during goal creation, THEN THE App SHALL display a validation error
3. THE CalendarFragment Goals tab SHALL display goal cards (red border) ordered by deadline, showing deadline date, title, priority badge, description, and a completion checkbox
4. WHEN a user taps the completion checkbox on a goal, THE Goal_Repository SHALL update the goal as completed with the current timestamp
5. WHEN a goal is completed, THE XP_Calculator SHALL award XP based on priority: Top=1000, Medium=500, Low=250
6. WHEN a goal is completed, THE App SHALL display a "CONGRATULATIONS!" popup showing the goal title, XP earned, and Share/Close buttons
7. THE goal card SHALL provide a 3-dot menu with Edit Goal and Delete Goal options

### Requirement 9: Calendar and Schedule View

**User Story:** As a user, I want to see a monthly calendar with workout and goal indicators, so that I can plan and review my fitness schedule.

#### Acceptance Criteria

1. THE CalendarFragment SHALL display a monthly calendar grid with navigation arrows to move between months
2. THE CalendarFragment SHALL mark days with scheduled workouts using a blue indicator and days with goals using a red triangle indicator
3. WHEN a user taps a day on the calendar, THE App SHALL display the scheduled content for that day with edit options
4. THE CalendarFragment SHALL provide "My Plan" and "Goals" tabs below the calendar grid
5. WHEN the user taps "+", THE App SHALL present options to Schedule Workout or Create Goal

### Requirement 10: Schedule Workout

**User Story:** As a user, I want to schedule workouts on specific dates with optional repetition, so that I have a structured plan.

#### Acceptance Criteria

1. WHEN a user selects a workout, a date, and a repeat interval (Never/Weekly/Biweekly/Monthly), THE Schedule_Repository SHALL insert Scheduled_Workout records accordingly
2. WHEN a repeat interval other than "Never" is selected, THE Schedule_Repository SHALL generate future Scheduled_Workout records for the specified pattern
3. THE weekly plan view ("My Plan" tab) SHALL display collapsible week sections with workout cards showing day, name, exercises, and a completion status or "START" button (for today only)

### Requirement 11: Workout Page

**User Story:** As a user, I want to browse my saved workouts and the exercise library, so that I can manage my workout collection.

#### Acceptance Criteria

1. THE WorkoutFragment SHALL display two sub-tabs: "Saved" (user workouts) and "Library" (all exercises grouped by muscle group)
2. THE Saved tab SHALL display workout cards (blue border) with workout name and exercise previews
3. THE Library tab SHALL display a muscle group grid; tapping a group SHALL show exercises filtered by that muscle group with name, difficulty, and image
4. WHEN the user taps "+", THE App SHALL navigate to the Create User Workout flow

### Requirement 12: Create User Workout

**User Story:** As a user, I want to create custom or generated workouts, so that I can train according to my preferences.

#### Acceptance Criteria

1. THE Create Workout flow SHALL provide a toggle between "Generate Workout" and "Create Workout" modes
2. WHEN in "Create Workout" mode, THE App SHALL allow the user to select muscle groups, choose exercises (1 to 8 maximum), set name, reps, and weight, then save
3. WHEN in "Generate Workout" mode, THE App SHALL allow the user to select difficulty and muscle groups, generate a recommended set of exercises, and allow the user to review before saving
4. IF the user attempts to add more than 8 exercises, THEN THE App SHALL prevent addition and display a limit notification
5. WHEN the workout is saved, THE Workout_Repository SHALL insert a Workout record and associated Workout_Exercise records

### Requirement 13: Manage Workout

**User Story:** As a user, I want to share, edit, duplicate, reorder, and delete my saved workouts, so that I can keep my collection organized.

#### Acceptance Criteria

1. THE workout card 3-dot menu SHALL provide options: Share, Edit, Duplicate, Reorder, and Delete
2. WHEN the user selects Share, THE App SHALL invoke the native Android share intent with workout details
3. WHEN the user selects Edit, THE App SHALL open the Create Workout flow pre-populated with existing data
4. WHEN the user selects Duplicate, THE Workout_Repository SHALL create a copy of the workout with a "Copy" suffix appended to the name
5. WHEN the user selects Delete, THE App SHALL display a confirmation dialog before removing the workout and associated exercise records

### Requirement 14: Workout Execution

**User Story:** As a user, I want to log my sets, reps, and weight during a workout, so that my progress is recorded and XP is earned.

#### Acceptance Criteria

1. THE Workout_Execution SHALL display one exercise at a time with a large demo image, difficulty label, and exercise name
2. THE Workout_Execution SHALL display a table of sets with editable Reps and Weight (KG) fields and a completion checkbox per row
3. WHEN the user taps "ADD SET +", THE Workout_Execution SHALL add a new set row to the current exercise
4. WHEN all set checkboxes for the current exercise are checked, THE Workout_Execution SHALL enable the "Next" button to proceed to the next exercise
5. IF a workout is not scheduled for today, THEN THE App SHALL prevent the user from starting that workout
6. THE Workout_Execution SHALL navigate exercises in the order defined by workout_exercise_order_index

### Requirement 15: Complete Workout

**User Story:** As a user, I want to see a summary after finishing a workout showing XP earned and muscles worked, so that I feel rewarded.

#### Acceptance Criteria

1. WHEN the user completes the final exercise, THE App SHALL display a "GREAT WORKOUT!" completion screen showing total XP earned and a muscles worked card
2. WHEN a workout is completed, THE XP_Calculator SHALL compute XP as sum(base_xp per exercise multiplied by sets_completed) multiplied by the current streak multiplier
3. WHEN a workout is completed, THE App SHALL update the Scheduled_Workout as completed, update User XP and coins, and insert an XP_Log record
4. WHEN the user taps "Continue" on the completion screen, THE App SHALL navigate to HomeFragment and trigger level-up and streak checks

### Requirement 16: Streak Tracking

**User Story:** As a user, I want my workout streak tracked daily, so that I earn multipliers for consistency.

#### Acceptance Criteria

1. WHEN a scheduled workout was completed yesterday, THE Streak_Manager SHALL increment the user's current streak count
2. WHEN a scheduled workout was not completed yesterday, THE Streak_Manager SHALL reset the user's streak to zero
3. WHILE a day has no scheduled workout (rest day), THE Streak_Manager SHALL leave the streak unchanged
4. WHEN the streak reaches a 14-day milestone (14, 28, 42, etc.), THE App SHALL display a milestone popup with fire icon, streak count, and the current XP multiplier value
5. WHEN the streak reaches a 14-day milestone, THE XP_Multiplier SHALL increase (starting at 1.5x, increasing every subsequent 14 days)

### Requirement 17: XP Earning

**User Story:** As a user, I want to earn XP from completing workouts and goals, so that I can level up.

#### Acceptance Criteria

1. WHEN a workout is completed, THE XP_Calculator SHALL calculate XP as sum of (each exercise's base_xp multiplied by sets_completed) multiplied by the current XP_Multiplier
2. WHEN a goal is completed, THE XP_Calculator SHALL award XP based on goal priority: Top=1000, Medium=500, Low=250
3. WHEN XP is earned, THE App SHALL insert an XP_Log record and update the User's total XP and coin balance

### Requirement 18: Level Up

**User Story:** As a user, I want to level up when I earn enough XP, so that I feel progression and earn coins.

#### Acceptance Criteria

1. WHEN a user's total XP meets the threshold for the next level, THE App SHALL increment the user's current_level
2. WHEN a level up occurs, THE App SHALL award 50 coins and update the User's coin balance
3. WHEN a level up occurs, THE App SHALL display a LevelUpDialog with a blurred overlay showing a bicep icon, the new level number, and "+50 COINS"
4. WHEN a level up occurs, THE App SHALL check whether the new level qualifies for a rank promotion

### Requirement 19: Rank Up

**User Story:** As a user, I want to earn new ranks at milestone levels, so that I have long-term goals to strive for.

#### Acceptance Criteria

1. WHEN a user's level meets or exceeds the min_lvl_required for the next Rank, THE App SHALL update the user's rank_id to the new rank
2. WHEN a rank up occurs, THE App SHALL display a RankUpDialog with "CONGRATULATIONS! NEW RANK UNLOCKED", the rank medal image, rank name, and "+100 COINS"
3. WHEN a rank up occurs, THE App SHALL award 100 bonus coins and update the User's coin balance
4. THE RankUpDialog SHALL provide Share and Close buttons

### Requirement 20: Avatar Page (Me Tab)

**User Story:** As a user, I want to view my avatar, rank, and level on the Me tab, so that I can see my identity and progress.

#### Acceptance Criteria

1. THE MeFragment SHALL display the user's composited avatar image at large size
2. THE MeFragment SHALL display the user's current gym rank with medal icon and rank name
3. THE MeFragment SHALL display the user's current level with a circular progress indicator showing progress toward the next level
4. WHEN the user taps the gear icon, THE App SHALL navigate to SettingsActivity
5. WHEN the user taps the locker icon, THE App SHALL navigate to CustomizeAvatarActivity

### Requirement 21: Locker and Avatar Customization

**User Story:** As a user, I want to browse cosmetic items and equip them on my avatar, so that I can personalize my appearance.

#### Acceptance Criteria

1. THE CustomizeAvatarActivity SHALL display the user's avatar at the top with current coin balance, and category tabs: Body, Face, Hair, Clothing
2. THE CustomizeAvatarActivity SHALL show owned items fully visible and locked items with a coin price overlay
3. WHEN the user taps an owned item, THE Avatar_Compositor SHALL equip it and update the avatar preview in real-time
4. WHEN the user taps the checkmark to save, THE App SHALL persist equipped items to User_Cosmetic and Outfit_Slot tables
5. WHEN the user taps back without saving, THE App SHALL discard changes

### Requirement 22: Unlock Cosmetic Item

**User Story:** As a user, I want to purchase locked cosmetic items with coins, so that I can expand my wardrobe.

#### Acceptance Criteria

1. WHEN a user taps a locked cosmetic item, THE App SHALL display a purchase dialog showing the item image, price in coins, and Cancel/Buy buttons
2. WHEN the user confirms purchase and has sufficient coins, THE App SHALL deduct the cost from the user's coin balance, mark the item as unlocked in User_Cosmetic, and insert a Coin_Transaction record
3. IF the user's coin balance is insufficient, THEN THE App SHALL display a "Not enough coins" message and prevent the purchase

### Requirement 23: User Profile Management

**User Story:** As a user, I want to manage my settings and log out, so that I can control my preferences and account.

#### Acceptance Criteria

1. THE SettingsActivity SHALL display sections: User (Personal Details, Account) and Application (Measurement System, Language)
2. WHEN the user changes the measurement system, THE App SHALL update the User's measurement_system field and persist the change
3. WHEN the user taps "Log out", THE App SHALL display a confirmation dialog
4. WHEN logout is confirmed, THE Session_Manager SHALL clear all session data and THE App SHALL navigate to WelcomeActivity

### Requirement 24: Admin Authentication

**User Story:** As an admin (Dean), I want a separate login flow, so that I can access the workout and exercise management portal.

#### Acceptance Criteria

1. THE AdminLoginActivity SHALL authenticate credentials against the Admins table
2. WHEN admin authentication succeeds, THE App SHALL navigate to AdminMainActivity with management options
3. IF admin credentials are invalid, THEN THE App SHALL display an error message
4. THE admin login SHALL be accessible via a hidden entry point (long-press on the logo or detection of the "admin" username)

### Requirement 25: Admin — Create Default Workout

**User Story:** As an admin, I want to create default workouts categorized by body area and muscle group, so that users have a library of pre-built workouts.

#### Acceptance Criteria

1. THE Admin Create Workout flow SHALL present category buttons: Abdominal, Full Body, Lower Body, Upper Body
2. WHEN a category is selected, THE App SHALL display a muscle group grid for that category
3. WHEN a muscle group is selected, THE App SHALL display exercises filtered from the Exercise table for that muscle group
4. THE Admin SHALL be able to select up to 8 exercises (highlighted in blue when selected)
5. WHEN the admin confirms, THE App SHALL present a naming screen with fields for workout name and reps/sets per exercise
6. WHEN saved, THE Workout_Repository SHALL insert a Workout record with is_default=TRUE and user_id=NULL, plus associated Workout_Exercise records

### Requirement 26: Admin — Create Default Exercise

**User Story:** As an admin, I want to add new exercises to the library, so that users and workouts have a comprehensive exercise pool.

#### Acceptance Criteria

1. THE Admin Create Exercise form SHALL accept: Exercise Name, Muscle Group (dropdown), Difficulty (Easy/Medium/Hard), Default Sets, Default Reps, Base XP, and Demo image
2. IF Exercise Name, Muscle Group, or Difficulty is empty, THEN THE App SHALL display a validation error
3. WHEN the form is submitted with valid data, THE App SHALL insert a new Exercise record

### Requirement 27: Admin — Manage Default Workouts

**User Story:** As an admin, I want to search, view, edit, and delete default workouts, so that I can maintain the workout library.

#### Acceptance Criteria

1. THE Admin Manage Workouts screen SHALL display a search bar and collapsible category sections with difficulty sub-sections
2. THE Admin Manage Workouts screen SHALL display workout cards with exercise previews and a 3-dot menu (Edit, Delete)
3. WHEN the admin selects Edit, THE App SHALL open the Create Default Workout flow pre-populated with existing data
4. WHEN the admin selects Delete, THE App SHALL display a confirmation dialog before removing the workout and associated exercise records
5. WHEN the admin types in the search bar, THE App SHALL filter displayed workouts by name in real-time

### Requirement 28: Sharing Features

**User Story:** As a user, I want to share my streak milestones, rank ups, and completed goals, so that I can celebrate achievements with friends.

#### Acceptance Criteria

1. WHEN the user taps Share on the streak milestone popup, THE App SHALL invoke the native Android share intent with text including the streak count and XP multiplier
2. WHEN the user taps Share on the rank up popup, THE App SHALL invoke the native Android share intent with text including the new rank name and bonus coins
3. WHEN the user taps Share on the goal completion popup, THE App SHALL invoke the native Android share intent with text including the goal title and XP earned

### Requirement 29: Dark Theme and Design System

**User Story:** As a user, I want a consistent dark-themed UI, so that the app is visually cohesive and comfortable to use in gym environments.

#### Acceptance Criteria

1. THE App SHALL use #393944 as the primary background color across all screens
2. THE App SHALL use #B2D6FD as the accent color for buttons and interactive highlights
3. THE App SHALL use #FF5050 (red) for warnings and goal-related highlights and #FAD942 (yellow) for XP/coin indicators
4. THE App SHALL use Inter font family for body text and HK Modular or Orbitron font family for headings
5. THE App SHALL display form sections in white card containers against the dark background

### Requirement 30: Background Threading for Database Operations

**User Story:** As a user, I want the app to remain responsive during database operations, so that the UI does not freeze.

#### Acceptance Criteria

1. THE App SHALL execute all JDBC database queries on a background thread using ExecutorService
2. THE App SHALL update UI elements on the main thread after background database operations complete
3. IF a database connection fails, THEN THE App SHALL display an error message to the user without crashing
