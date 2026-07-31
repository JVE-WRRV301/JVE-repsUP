# Code Style Guide — Reps Up

## Java Conventions
- Class names: PascalCase (e.g. `FitnessAssessmentActivity`, `UserRepository`)
- Method names: camelCase (e.g. `getUserById`, `updateStreak`)
- Variable names: camelCase (e.g. `currentUser`, `workoutList`)
- Constants: UPPER_SNAKE_CASE (e.g. `MAX_EXERCISES = 8`)
- One class per file, filename matches class name exactly

## XML Layout Conventions
- File names: snake_case (e.g. `activity_fitness_assessment.xml`, `fragment_home.xml`)
- View IDs: camelCase with type prefix (e.g. `tvTitle`, `btnNext`, `rvWorkouts`, `etUsername`)
  - `tv` = TextView
  - `btn` = Button
  - `rv` = RecyclerView
  - `et` = EditText
  - `iv` = ImageView
  - `pb` = ProgressBar
  - `ll` = LinearLayout
  - `cl` = ConstraintLayout

## Package Structure
- `activities/` — full screen flows (login, register, assessment, workout execution)
- `fragments/` — tab content (home, calendar, workout, me)
- `adapters/` — RecyclerView adapters
- `models/` — plain Java data classes matching database tables
- `repositories/` — all database query methods (JDBC calls go here only)
- `database/` — DatabaseHelper and ConnectionManager only
- `utils/` — helper classes (XPCalculator, StreakManager, AvatarCompositor, SessionManager)
- `dialogs/` — popup dialogs (LevelUpDialog, RankUpDialog, StreakDialog, ConfirmDialog)

## Database Rules
- All JDBC calls must run on a background thread (use ExecutorService, never on UI thread)
- All database access goes through Repository classes only — no direct DB calls from Activities or Fragments
- Always close connections in a finally block or use try-with-resources

## UI Rules
- All colours referenced from colors.xml only — no hardcoded hex values in layouts
- All text sizes in SP, all dimensions in DP
- All strings in res/values/strings.xml — no hardcoded strings in Java or XML
- Follow the dark theme — background always dark_grey, cards use dark_grey_variant

## Git Rules
- Each team member works on their own branch (Emma, Vincent, James)
- Commit messages must be descriptive (e.g. "Add FitnessAssessmentActivity step 1 - gender screen")
- Never commit directly to main — always via pull request
- Pull from main before starting any new work session