# Implementation Plan: Reps Up App

## Overview

This plan implements the Reps Up gamified fitness Android application in Java using XML layouts, Activities/Fragments architecture, and MySQL 8 via JDBC. Tasks are ordered by dependency — foundation first, then authentication, admin features, core user features, gamification, avatar/locker, and finally integration. The plan aligns with the team's phased approach (James, Vincent, Emma) and covers all 30 requirements.

## Tasks

- [x] 1. Foundation — Project Setup and Database Connection Layer
  - [x] 1.1 Configure build system for secure database credentials
    - Read `db.host`, `db.port`, `db.user`, `db.password`, `db.name` from `android/local.properties`
    - Inject values as `BuildConfig` string fields in `app/build.gradle.kts`
    - Fail the build with a descriptive error if any required field is missing
    - Verify `local.properties` is in `.gitignore`
    - Add MySQL JDBC connector and INTERNET permission to AndroidManifest.xml
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 1.2 Create DatabaseHelper and ConnectionManager infrastructure
    - Implement `DatabaseHelper.java` singleton using `BuildConfig` fields to establish JDBC connections
    - Implement `ConnectionManager.java` with `ExecutorService` (fixed pool of 4 threads), `DatabaseCallback<T>` interface, and `runOnUiThread` result delivery
    - Wrap all connections with try-with-resources patterns
    - _Requirements: 1.1, 30.1, 30.2, 30.3_

  - [x] 1.3 Set up dark theme design system and base styles
    - Configure `res/values/themes.xml` with primary background `#393944`
    - Set accent `#B2D6FD`, warning/goal red `#FF5050`, XP/coin yellow `#FAD942` in `colors.xml`
    - Add Inter font family (regular, medium, bold) and Orbitron font family (regular, medium, bold) to `res/font/`
    - Create base styles: rounded light-blue buttons, blue/red bordered cards, white card containers, input field styles
    - _Requirements: 29.1, 29.2, 29.3, 29.4, 29.5_

  - [x] 1.4 Create all data model classes
    - Create/update `models/` package with: `User`, `Avatar`, `UserProfile`, `Rank`, `Exercise`, `Workout`, `WorkoutExercise`, `ScheduledWorkout`, `Goal`, `XPLog`, `CosmeticItem`, `UserCosmetic`, `OutfitSlot`, `CoinTransaction`, `ExerciseResult`, `Streak`
    - Each model must map to its corresponding MySQL table with appropriate field types
    - _Requirements: All (data foundation)_

  - [x] 1.5 Create SessionManager and Navigation Shell
    - Implement `SessionManager.java` using SharedPreferences to store user_id, username, isAdmin
    - Implement `MainActivity.java` with BottomNavigationView (Home, Calendar, Workout, Me tabs)
    - Create placeholder fragments: `HomeFragment`, `CalendarFragment`, `WorkoutFragment`, `MeFragment`
    - Implement fragment switching without reloading previously visited fragments
    - Create persistent top bar layout (avatar thumbnail, level indicator, streak fire icon with count, coin count)
    - Add session check: no session → LoginActivity, active session → MainActivity
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 2.4_

- [x] 2. Checkpoint — Foundation verification
  - Ensure app builds, connects to MySQL on emulator, dark theme is applied, navigation shell works, and session redirect logic functions correctly. Ask the user if questions arise.

- [ ] 3. Authentication and Onboarding
  - [x] 3.1 Implement LoginActivity and UserRepository authentication
    - Create `LoginActivity.java` with dark top section (gym image, "Welcome back!") and white bottom card (username, password fields, Login button)
    - Create `UserRepository.java` with `authenticate(username, password, callback)` method querying User table on background thread
    - On success: store user_id in SessionManager, navigate to MainActivity
    - On failure: display generic error without revealing which field was incorrect
    - Wire "Sign Up" link to navigate to RegisterActivity
    - _Requirements: 2.1, 2.2, 2.3, 2.5_

  - [x] 3.2 Implement WelcomeActivity and RegisterActivity
    - Create `WelcomeActivity.java` as launcher activity with "Welcome to REPS UP", Sign Up and Login navigation
    - Create `RegisterActivity.java` with form: Name, Username, Email, Password, Confirm Password, "Get Started" button
    - Add `UserRepository.register(user, callback)` — INSERT into User table with defaults (xp=0, coins=0, level=1, rank_id=1, streak=0)
    - Add `UserRepository.isUsernameTaken(username, callback)` for uniqueness validation
    - Validate: all fields required, password match, username uniqueness
    - On success: store session, navigate to FitnessAssessmentActivity
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x]* 3.3 Write property test for registration validation (Property 9)
    - **Property 9: Registration Validation Rejects Invalid Inputs**
    - For any registration attempt where username is taken OR password != confirm OR required field is empty, system rejects and no User record is inserted
    - Use jqwik to generate random invalid input combinations and verify rejection
    - **Validates: Requirements 3.2, 3.3, 3.4**

  - [x] 3.4 Implement FitnessAssessmentActivity (9-step wizard)
    - Create `FitnessAssessmentActivity.java` with ViewPager2 hosting 9 steps
    - Steps: Welcome, Gender, Age, Weight (ruler slider + kg/lbs toggle), Height (ruler slider + cm/ft toggle), Body Type (2x3 grid), Workout Level, Workout Schedule, Fitness Goals (multi-select)
    - Implement segmented progress bar at top
    - Preserve previously entered values when navigating backward
    - On completion: INSERT User_Profile record with collected data
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [x] 3.5 Implement Avatar Generation and "Meet Your Avatar" screen
    - Create `AvatarCompositor.java` — layers base body PNG (by gender + body type) with default outfit overlays (white tank, black shorts, white sneakers) using Android Canvas
    - Implement `getBaseBodyResource(gender, bodyType)` resource resolution
    - INSERT Avatar record, link default cosmetic items as equipped in User_Cosmetic, create Outfit_Slot, update User.avatar_id
    - Create "MEET YOUR AVATAR!" screen with composited avatar and "Continue" button → MainActivity
    - _Requirements: 5.1, 5.2, 5.3_

- [ ] 4. Checkpoint — Authentication and onboarding verification
  - Ensure full flow works: Welcome → Register → Assessment → Avatar → Home, and Login → Home. Ask the user if questions arise.

- [ ] 5. Admin Features
  - [ ] 5.1 Implement Admin Login and Dashboard
    - Create `AdminLoginActivity.java` with username/password form and dark branding
    - Create `AdminRepository.java` with `authenticate(username, password, callback)` against Admins table
    - Create `AdminMainActivity.java` with management navigation options
    - Add hidden entry point (long-press logo or "admin" username detection on WelcomeActivity)
    - Store admin session state in SessionManager
    - _Requirements: 24.1, 24.2, 24.3, 24.4_

  - [ ] 5.2 Implement Admin Create Default Workout flow
    - Create category selection screen: Abdominal, Full Body, Lower Body, Upper Body buttons
    - Create muscle group grid screen filtered by selected category
    - Create exercise selection screen showing exercises from Exercise table filtered by muscle group (max 8, highlighted in blue when selected)
    - Create naming screen with workout name and reps/sets per exercise fields
    - On save: INSERT Workout (is_default=TRUE, user_id=NULL) and Workout_Exercise records
    - _Requirements: 25.1, 25.2, 25.3, 25.4, 25.5, 25.6_

  - [ ] 5.3 Implement Admin Create Default Exercise
    - Create exercise form: Exercise Name, Muscle Group (dropdown), Difficulty (Easy/Medium/Hard), Default Sets, Default Reps, Base XP, Demo image
    - Validate required fields (name, muscle group, difficulty)
    - On valid submission: INSERT Exercise record
    - _Requirements: 26.1, 26.2, 26.3_

  - [ ] 5.4 Implement Admin Manage Default Workouts
    - Create `ManageWorkoutsFragment` with search bar and collapsible category sections with difficulty sub-sections
    - Display workout cards with exercise previews and 3-dot menu (Edit, Delete)
    - Edit: open Create Default Workout flow pre-populated with existing data
    - Delete: confirmation dialog → remove workout and associated Workout_Exercise records
    - Real-time search filtering by workout name
    - _Requirements: 27.1, 27.2, 27.3, 27.4, 27.5_

- [ ] 6. Checkpoint — Admin features verification
  - Ensure admin login, create workout, create exercise, manage workouts all function correctly. Ask the user if questions arise.

- [ ] 7. Home Page and Goal Management
  - [x] 7.1 Implement HomeFragment with today's workout and next goal
    - Display week strip (S M T W T F S) with today highlighted
    - Display "TODAY" card (blue border): workout name, date, first 3 exercises with images and set counts, "START" button
    - Display "NEXT GOAL" card (red border): deadline, title, priority badge, description
    - Implement empty states for rest days and no goals
    - Wire START button to WorkoutExecutionActivity
    - Use `ScheduleRepository.getTodaysWorkout()` and `GoalRepository.getNextGoal()`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [x] 7.2 Implement GoalRepository and Goal Creation
    - Create `GoalRepository.java` with `getUserGoals`, `getNextGoal`, `createGoal`, `updateGoal`, `completeGoal`, `deleteGoal` methods
    - Create goal creation form accessible from Calendar "+" button: Title, Date picker, Importance (Top/Medium/Low), Notes
    - Validate Title and Date required
    - INSERT Goal record on save
    - _Requirements: 8.1, 8.2_

  - [ ] 7.3 Implement Goals List View and Goal Completion
    - Create `GoalsListFragment` in CalendarFragment Goals tab with goal cards (red border): deadline, title, priority badge, description, completion checkbox
    - Implement 3-dot menu with Edit Goal and Delete Goal options
    - On checkbox tap: UPDATE goal as completed with timestamp
    - Award XP on completion: Top=1000, Medium=500, Low=250
    - Display "CONGRATULATIONS!" popup with goal title, XP earned, Share/Close buttons
    - INSERT XP_Log, UPDATE User.xp
    - _Requirements: 8.3, 8.4, 8.5, 8.6, 8.7_

  - [ ]* 7.4 Write property test for Goal XP calculation (Property 2)
    - **Property 2: Goal XP Matches Priority Mapping**
    - For any goal priority in {Top, Medium, Low}, XP is exactly 1000, 500, or 250 respectively; any other value returns 0
    - Use jqwik to generate arbitrary priority strings and verify the mapping
    - **Validates: Requirements 8.5, 17.2**

- [ ] 8. Workout Features
  - [ ] 8.1 Implement WorkoutFragment with Saved and Library tabs
    - Create `WorkoutFragment.java` with TabLayout: "Saved" and "Library" sub-tabs
    - Create `WorkoutRepository.java` with all repository methods
    - Saved tab: display user workout cards (blue border) with exercise previews using `WorkoutCardAdapter`
    - Library tab: muscle group grid → tap group → filtered exercises with name, difficulty, image using `ExerciseListAdapter`
    - Wire "+" button to Create Workout flow
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [ ] 8.2 Implement Create User Workout flow
    - Create toggle between "Generate Workout" and "Create Workout" modes
    - Create Workout mode: select muscle groups → choose exercises (1 to 8 max) → set name, reps, weight → save
    - Generate Workout mode: select difficulty + muscle groups → system picks exercises → review → save
    - Enforce maximum 8 exercises with Toast notification on limit
    - INSERT Workout and Workout_Exercise records on save
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

  - [ ]* 8.3 Write property test for workout exercise count bounds (Property 8)
    - **Property 8: Workout Exercise Count Bounded**
    - For any workout creation attempt, system accepts exactly 1 to 8 exercises; counts outside this range are rejected
    - Use jqwik to generate random exercise counts [0..20] and verify acceptance/rejection
    - **Validates: Requirements 12.4, 25.4**

  - [ ] 8.4 Implement Manage Workout (3-dot menu actions)
    - 3-dot menu on saved workout cards: Share, Edit, Duplicate, Reorder, Delete
    - Share: invoke native Android share intent with workout details
    - Edit: open Create Workout flow pre-populated with existing data
    - Duplicate: create copy with "Copy" suffix via `WorkoutRepository.duplicateWorkout()`
    - Delete: confirmation dialog → remove workout and Workout_Exercise records
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 9. Calendar and Schedule Features
  - [ ] 9.1 Implement CalendarFragment with monthly grid
    - Create `CalendarFragment.java` with monthly calendar grid and navigation arrows
    - Mark days with scheduled workouts (blue indicator) and goals (red triangle indicator)
    - Tap day → display scheduled content with edit options
    - Provide "My Plan" and "Goals" tabs below calendar
    - Wire "+" button with options: Schedule Workout or Create Goal
    - Create `ScheduleRepository.java` with all repository methods
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ] 9.2 Implement Schedule Workout flow
    - Create schedule form: date picker, workout selection, repeat interval (Never/Weekly/Biweekly/Monthly)
    - On save: INSERT Scheduled_Workout record; if repeat ≠ Never, generate future occurrences
    - _Requirements: 10.1, 10.2_

  - [ ] 9.3 Implement Weekly Plan view ("My Plan" tab)
    - Create `WeeklyPlanFragment` with collapsible week sections using `WeeklyPlanAdapter`
    - Display workout cards: day, name, exercises, completion status or "START" button (today only)
    - _Requirements: 10.3_

- [ ] 10. Workout Execution
  - [ ] 10.1 Implement WorkoutExecutionActivity
    - Create `WorkoutExecutionActivity.java` displaying one exercise at a time: large demo image, difficulty label, exercise name
    - Display set table with editable Reps and Weight (KG) fields and completion checkbox per row
    - Implement "ADD SET +" button to add new set rows
    - Enable "Next" button only when all set checkboxes are checked
    - Navigate exercises in order defined by `workout_exercise_order_index`
    - Prevent starting workout not scheduled for today
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6_

  - [ ] 10.2 Implement WorkoutCompleteActivity and XP awarding
    - Create `WorkoutCompleteActivity.java` with "GREAT WORKOUT!" screen showing total XP earned and muscles worked card
    - Implement `XPCalculator.calculateWorkoutXP()`: sum(base_xp × sets_completed) × streak_multiplier
    - UPDATE Scheduled_Workout as completed, UPDATE User XP and coins, INSERT XP_Log record
    - On "Continue": navigate to HomeFragment and trigger level-up and streak checks
    - _Requirements: 15.1, 15.2, 15.3, 15.4_

  - [ ]* 10.3 Write property test for XP calculation (Property 1)
    - **Property 1: XP Calculation is Non-Negative and Monotonic**
    - For any list of exercise results (base_xp >= 0, sets_completed >= 0) and streak multiplier >= 1.0, workout XP is non-negative and increases monotonically as any exercise's sets_completed increases
    - Use jqwik to generate random exercise result lists and multipliers
    - **Validates: Requirements 15.2, 17.1**

- [ ] 11. Checkpoint — Core features verification
  - Ensure Home, Goals, Workouts, Calendar, Schedule, and Workout Execution all function end-to-end. Ask the user if questions arise.

- [ ] 12. Gamification — Streaks
  - [ ] 12.1 Implement StreakManager utility class
    - Implement `StreakManager.evaluateStreak(currentStreak, hadScheduledWorkout, completedYesterday)`:
      - Scheduled + completed → streak + 1
      - Scheduled + not completed → streak = 0
      - No workout scheduled (rest day) → streak unchanged
    - Implement `StreakManager.getMultiplier(streakCount)`: 1.0 for <14, ≥1.5 for ≥14, non-decreasing
    - Implement `StreakManager.isMilestone(streakCount)`: true iff streak is a positive multiple of 14
    - Wire streak evaluation to post-workout flow and app launch
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

  - [ ]* 12.2 Write property test for streak evaluation (Property 3)
    - **Property 3: Streak Evaluation is Deterministic and Correct**
    - For any current streak >= 0, verify the three transition rules are correctly applied
    - Use jqwik to generate random streak counts and boolean states
    - **Validates: Requirements 16.1, 16.2, 16.3**

  - [ ]* 12.3 Write property test for streak multiplier (Property 4)
    - **Property 4: Streak Multiplier Increases at 14-Day Intervals**
    - For any streak >= 0, multiplier is 1.0 for <14 and >=1.5 for >=14, non-decreasing as streak increases
    - Use jqwik to generate random streak counts [0..500]
    - **Validates: Requirements 16.5**

  - [ ]* 12.4 Write property test for streak milestones (Property 5)
    - **Property 5: Streak Milestones Occur at Exact Multiples of 14**
    - For any positive integer streak, `isMilestone(streak)` returns true iff streak % 14 == 0 and streak > 0
    - Use jqwik to generate random positive integers
    - **Validates: Requirements 16.4**

  - [ ] 12.5 Implement StreakMilestoneDialog
    - Create `StreakMilestoneDialog.java` with fire icon, streak count, XP multiplier value, Share/Close buttons
    - Trigger on 14-day milestones (14, 28, 42, ...)
    - _Requirements: 16.4, 16.5_

- [ ] 13. Gamification — XP, Levels, and Ranks
  - [ ] 13.1 Implement XPCalculator utility class
    - Implement `calculateWorkoutXP(results, streakMultiplier)`: sum(base_xp × sets_completed) × multiplier
    - Implement `calculateGoalXP(priority)`: Top=1000, Medium=500, Low=250, other=0
    - Implement `xpForNextLevel(currentLevel)`: Level N requires N × 100 XP
    - Implement `shouldLevelUp(totalXp, currentLevel)`: check if totalXp >= cumulative threshold
    - Wire XP awarding to INSERT XP_Log and UPDATE User.xp and User.coins
    - _Requirements: 17.1, 17.2, 17.3_

  - [ ]* 13.2 Write property test for level-up threshold (Property 6)
    - **Property 6: Level-Up Threshold is Deterministic**
    - For any level >= 1 and totalXp >= 0, `shouldLevelUp` returns true iff totalXp >= sum of (level × 100) for levels 1 through currentLevel; calling it twice with same inputs produces same result
    - Use jqwik to generate random levels and XP values
    - **Validates: Requirements 18.1**

  - [ ] 13.3 Implement Level Up logic and LevelUpDialog
    - After XP earned → check `shouldLevelUp` → increment User.current_level
    - Award 50 coins on level up, UPDATE User.coins
    - Create `LevelUpDialog.java`: blurred overlay, bicep icon, new level number, "+50 COINS"
    - After level up → check rank promotion eligibility
    - _Requirements: 18.1, 18.2, 18.3, 18.4_

  - [ ] 13.4 Implement Rank Up logic and RankUpDialog
    - After level up → query Ranks table for next rank where user's level >= min_lvl_required
    - If qualified: UPDATE User.rank_id, award 100 bonus coins
    - Create `RankUpDialog.java`: "CONGRATULATIONS! NEW RANK UNLOCKED", medal image, rank name, "+100 COINS", Share/Close buttons
    - _Requirements: 19.1, 19.2, 19.3, 19.4_

  - [ ]* 13.5 Write property test for coin balance invariant (Property 7)
    - **Property 7: Coin Balance Invariant on Cosmetic Purchase**
    - For any user with coins >= item cost, new balance = previous - cost; if coins < cost, balance unchanged
    - Use jqwik to generate random coin balances and item costs
    - **Validates: Requirements 22.2, 22.3**

- [ ] 14. Checkpoint — Gamification verification
  - Ensure streaks track correctly, XP calculates properly, level ups trigger with coin rewards, rank promotions work. Ask the user if questions arise.

- [ ] 15. Avatar Page and Locker
  - [ ] 15.1 Implement MeFragment with avatar display and progress
    - Create `MeFragment.java`: large composited avatar image, gear icon → SettingsActivity, locker icon → CustomizeAvatarActivity
    - Display current gym rank with medal icon and rank name
    - Display current level with circular progress indicator showing progress toward next level
    - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.5_

  - [ ] 15.2 Implement CustomizeAvatarActivity (Locker)
    - Create `CustomizeAvatarActivity.java`: avatar preview at top, coin balance display, category tabs (Body, Face, Hair, Clothing)
    - Create `CosmeticRepository.java` with all repository methods
    - Display owned items fully visible, locked items with coin price overlay using `CosmeticGridAdapter`
    - Tap owned item → equip and update avatar preview in real-time via AvatarCompositor
    - Checkmark to save → persist to User_Cosmetic and Outfit_Slot tables
    - Back without saving → discard changes
    - _Requirements: 21.1, 21.2, 21.3, 21.4, 21.5_

  - [ ] 15.3 Implement Cosmetic Purchase flow
    - Tap locked item → `PurchaseDialog.java`: item image, price in coins, Cancel/Buy buttons
    - If sufficient coins: deduct cost, mark unlocked in User_Cosmetic, INSERT Coin_Transaction
    - If insufficient: display "Not enough coins" message, disable Buy button
    - _Requirements: 22.1, 22.2, 22.3_

- [ ] 16. User Profile Management
  - [ ] 16.1 Implement SettingsActivity
    - Create `SettingsActivity.java` with sections: User (Personal Details, Account) and Application (Measurement System, Language)
    - Measurement system change → UPDATE User.measurement_system
    - Logout: confirmation dialog → SessionManager.clearSession() → navigate to WelcomeActivity
    - _Requirements: 23.1, 23.2, 23.3, 23.4_

- [ ] 17. Sharing Features
  - [ ] 17.1 Implement Share Streak Milestone
    - Share button on streak milestone popup → invoke native Android share intent
    - Text includes streak count and XP multiplier value
    - _Requirements: 28.1_

  - [ ] 17.2 Implement Share Rank Up
    - Share button on rank up popup → invoke native Android share intent
    - Text includes new rank name and bonus coins
    - _Requirements: 28.2_

  - [ ] 17.3 Implement Share Goal Completion
    - Share button on goal completion popup → invoke native Android share intent
    - Text includes goal title and XP earned
    - _Requirements: 28.3_

- [ ] 18. Integration Testing and Wiring
  - [ ] 18.1 Wire all navigation flows end-to-end
    - Verify Welcome → Register → Assessment → Avatar → Home flow
    - Verify Login → Home flow with session persistence
    - Verify tab navigation preserves fragment state
    - Wire all button/action navigations across Activities
    - Ensure all callbacks handle lifecycle correctly (WeakReference or lifecycle checks)
    - _Requirements: 2.1, 2.2, 3.5, 5.3, 6.1, 6.2_

  - [ ]* 18.2 Write integration tests for database connectivity
    - Test ConnectionManager can execute queries against test database
    - Test full insert/query/update/delete cycles for UserRepository
    - Test full CRUD for WorkoutRepository and GoalRepository
    - _Requirements: 1.1, 30.1, 30.2, 30.3_

  - [ ]* 18.3 Write unit tests for XPCalculator and StreakManager
    - Test XPCalculator with concrete examples: 0 sets, 1 exercise, max exercises, multiplier edge cases
    - Test StreakManager state transitions for all three scenarios
    - Test Goal XP priority mapping for all values
    - _Requirements: 15.2, 16.1, 16.2, 16.3, 17.1, 17.2_

  - [ ]* 18.4 Write Espresso UI tests for critical flows
    - Test navigation: tab switching preserves fragment state
    - Test form validation: error messages on invalid input for login and registration
    - Test dialogs: LevelUp, RankUp, StreakMilestone render with correct data
    - _Requirements: 2.3, 3.3, 3.4, 18.3, 19.2_

- [ ] 19. Final Checkpoint — Full integration verification
  - Ensure all tests pass, all flows work end-to-end (Register → Assess → Home → Schedule → Start Workout → Complete → XP → Level Up → Rank Up), admin portal fully functional, error states handled gracefully. Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation between major phases
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests and integration tests validate specific examples and edge cases
- All database operations must execute on background threads via ConnectionManager
- The project uses Java (not Kotlin) with XML layouts (not Compose)
- Build system is Gradle Kotlin DSL (build.gradle.kts)
- Database: MySQL 8 via JDBC, local dev on emulator host 10.0.2.2
- Team ownership aligns with PlanTasks.md: James (foundation, admin, home, goals, gamification), Vincent (auth, workouts, execution, levels), Emma (assessment, avatar, calendar, locker)

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.3"] },
    { "id": 1, "tasks": ["1.2", "1.4"] },
    { "id": 2, "tasks": ["1.5"] },
    { "id": 3, "tasks": ["3.1", "3.2"] },
    { "id": 4, "tasks": ["3.3", "3.4", "5.1"] },
    { "id": 5, "tasks": ["3.5", "5.2", "5.3"] },
    { "id": 6, "tasks": ["5.4", "7.1", "7.2", "8.1"] },
    { "id": 7, "tasks": ["7.3", "7.4", "8.2", "9.1"] },
    { "id": 8, "tasks": ["8.3", "8.4", "9.2", "9.3"] },
    { "id": 9, "tasks": ["10.1"] },
    { "id": 10, "tasks": ["10.2", "10.3"] },
    { "id": 11, "tasks": ["12.1", "13.1"] },
    { "id": 12, "tasks": ["12.2", "12.3", "12.4", "12.5", "13.2"] },
    { "id": 13, "tasks": ["13.3"] },
    { "id": 14, "tasks": ["13.4", "13.5"] },
    { "id": 15, "tasks": ["15.1", "15.2"] },
    { "id": 16, "tasks": ["15.3", "16.1"] },
    { "id": 17, "tasks": ["17.1", "17.2", "17.3"] },
    { "id": 18, "tasks": ["18.1"] },
    { "id": 19, "tasks": ["18.2", "18.3", "18.4"] }
  ]
}
```
