# Reps Up – Pre-hand-in review

*Code and design review of the JVE-repsUP project · 9 October 2026*

# How to read this

This is a review of the code, layouts and database only. Nothing in the project was changed. Every item from your notes is included and marked **[YOUR NOTE]**, usually with the cause I found in the code. Everything else is new.

Tags show priority: **[MUST FIX]** is a real bug, a crash or something a marker will notice. **[SHOULD FIX]** is a visible inconsistency or broken behaviour. **[NICE TO HAVE]** is polish or an improvement. **[TEST THIS]** means the code points to a problem I couldn’t confirm without running the app.

Each item has a “Where” line naming the files involved. Items are numbered so the group can split them up (“I’ll take 14–20”).

# 1. Answers to your questions

## Will the keyboard cover the sign-up fields on a real phone?

Yes. It isn’t an emulator quirk, so fix it before hand-in. There are two causes:

- `RegisterActivity` (also `WelcomeActivity` and `FitnessAssessmentActivity`) sets `WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS`. That flag lets the window extend past the screen, so Android no longer shrinks the layout when the keyboard opens and the NestedScrollView has nothing to scroll.
- The app targets SDK 36, so on Android 15+ phones it is forced edge-to-edge. Setting `android:windowSoftInputMode="adjustResize"` alone no longer pushes content up. You also need to apply the keyboard (IME) inset yourself, e.g. `ViewCompat.setOnApplyWindowInsetsListener` on the scroll view, adding `insets.getInsets(WindowInsetsCompat.Type.ime()).bottom` as bottom padding.

Fix: remove `FLAG_LAYOUT_NO_LIMITS` (use `WindowCompat.setDecorFitsSystemWindows(window, false)` for the full-screen image), add `adjustResize` in the manifest and handle the IME inset. The form is already in a NestedScrollView, so it will then scroll as expected. Apply the same fix to Login, Create Goal, Personal Details, Account and the workout name screen.

## How do I turn off the pop-up keyboard in the emulator?

The on-screen keyboard is normal phone behaviour. You probably didn’t see it before because the emulator was treating your PC keyboard as a physical keyboard, and that setting has changed (new AVD image or emulator update). To hide it, open the emulator’s Android **Settings → System → Keyboard** (on some versions **Languages & input**) **→ Physical keyboard** and turn off **“Use on-screen keyboard”** (sometimes called “Show virtual keyboard”). Menu names differ slightly between Android versions.

I’d leave it on while you fix the screens above, since it shows exactly what a real user will see.

# 2. Critical bugs (fix these first)

### 1. [MUST FIX] Streak is counted twice and goes up every time the app is opened

- `WorkoutCompleteActivity` adds +1 to the streak when you finish today’s workout. The next day, `MainActivity.evaluateYesterdaysStreak()` sees “yesterday was completed” and adds +1 again via `StreakManager.evaluateStreak()`.
- That check runs in `onCreate` on every launch with no “already checked today” guard, so opening the app 5 times adds +5.
- Your own spec (.kiro/specs/streak-multiple-increment-fix) says the launch check should only **reset** the streak when yesterday was missed. Change it so it never increments, and store the last-evaluated date (SharedPreferences or a column) so it runs once per day.
- Small mismatch: the spec talks about a 14-day milestone but `StreakManager` uses every 7 days. Agree on one.

*Where:* `MainActivity.evaluateYesterdaysStreak`, `StreakManager.evaluateStreak`, `WorkoutCompleteActivity.persistCompletion`

### 2. [MUST FIX] Rotating the phone loses progress and can award XP twice

- No activity locks orientation and none saves state (no `onSaveInstanceState`, no `screenOrientation` in the manifest).
- Workout execution: rotating jumps back to exercise 1 and wipes every rep/weight entered.
- Workout complete: rotating re-runs `onCreate` → `persistCompletion()` again, so XP and the XP_Log row are added a second time.
- Fitness assessment loses all answers. Create Workout in edit mode becomes a blank screen.
- Quickest fix: add `android:screenOrientation="portrait"` to every activity. Also make completion safe to repeat with `UPDATE scheduled_workout … WHERE scheduled_workout_id = ? AND is_completed = 0`, and only award XP when that update affects 1 row.

*Where:* `AndroidManifest.xml`, `WorkoutExecutionActivity`, `WorkoutCompleteActivity`, `FitnessAssessmentActivity`, `CreateWorkoutActivity`

### 3. [MUST FIX] Editing a scheduled default workout changes it for every user

- My Plan → ⋮ → Edit opens `CreateWorkoutActivity` with the scheduled workout’s id. If that is an admin default (e.g. “Push Day”), `updateWorkout()` overwrites it and also sets `is_default = false`. The default then disappears from everyone’s schedule list.
- This will only show once the database is shared, so it’s easy to miss in local testing.
- Fix: if the workout is a default (or not owned by the user), save a personal copy and point that scheduled workout at the copy, or hide Edit for defaults. Also check ownership in SQL (`WHERE workout_id = ? AND user_id = ?`).

*Where:* `WeeklyPlanFragment.onEditScheduledWorkout`, `CreateWorkoutActivity.onWorkoutSaved`, `WorkoutRepository.updateWorkout`

### 4. [MUST FIX] Choosing 4 or more fitness goals makes sign-up fail

- Goals are saved as display text joined with commas, e.g. “Lose weight,Stay Consistent,Build muscle,Get fitter” = 51 characters. `User_Profile.fitness_goal` is `VARCHAR(50)`, so the insert fails and the user gets “Failed to save profile” and can’t finish the assessment.
- Fix: a migration to widen the column (e.g. `VARCHAR(255)`), or a separate goals table. Also store short keys (`lose_weight`), which is what the seed data already uses, instead of display text.

*Where:* `AssessmentGoalsFragment.goalValues`, `AssessmentData.getFitnessGoalsAsString`, `database/schema.sql`

### 5. [MUST FIX] Leaving sign-up halfway leaves a broken account

- `RegisterActivity` creates the session **before** the assessment. If the user closes the app during the assessment or avatar screen, the next launch goes straight to MainActivity with no profile and no avatar.
- `RegisterActivity` isn’t finished, so the back arrow on the assessment returns to the filled-in sign-up form. Pressing Get Started again gives “Username is taken”.
- The avatar screen’s back button returns to the assessment. Pressing Finish again tries to insert a second profile (`user_id` is UNIQUE), which fails with an error. Finish can also be double-tapped.
- Fix: `finish()` Register and the assessment when moving forward; disable or remove Back on the avatar screen; disable Finish while saving; on launch, check for a User_Profile/Avatar and resume the assessment if missing.

*Where:* `RegisterActivity.proceedToAssessment`, `FitnessAssessmentActivity.saveProfile`, `AvatarGenerationActivity`, `MainActivity.onCreate`

### 6. [MUST FIX] Sign-up rejects most real email addresses

- `isValidEmail()` only accepts a hard-coded list of providers (gmail, outlook, yahoo…) and extensions. Addresses like name@mandela.ac.za or a work address are rejected as “invalid”.
- Personal Details already uses `Patterns.EMAIL_ADDRESS`, so a user can change to an address they weren’t allowed to sign up with.
- Fix: use `Patterns.EMAIL_ADDRESS` in both places and delete the long commented-out reasoning in `isValidEmail`.

*Where:* `RegisterActivity.isValidEmail`

### 7. [MUST FIX] Passwords are stored in plain text and aren’t case-sensitive

- Login runs `WHERE username = ? AND set_password = ?`. MySQL’s default collation ignores case, so “Password1” and “password1” both work.
- Passwords (and the admin password “admin123”) are stored as plain text.
- Fix: hash passwords (SHA-256 with a salt, or BCrypt) and compare in Java. At minimum use `BINARY set_password = ?`. A marker who knows databases will likely check this.

*Where:* `UserRepository.authenticate/register/changePassword`, `AdminRepository`

### 8. [MUST FIX] Table names won’t match on a shared (Linux) MySQL server

- The schema creates `User`, `Workout`, `Goal`, `Exercise`, `Scheduled_Workout`, `Ranks`, `XP_Log`… but most queries use lowercase (`user`, `workout`, `goal`, `ranks`, `xp_log`…). Some use the capitalised names (`User_Cosmetic`, `Outfit_Slot`, `Admins`).
- This works on Windows because MySQL there ignores table-name case. Most hosted MySQL servers run on Linux, where names **are** case-sensitive, so almost every screen would fail once you move to the shared database.
- Fix: make all table names lowercase in schema.sql (and a migration), then search for every SQL string.

*Where:* all repositories, `FitnessAssessmentActivity`, `AvatarGenerationActivity`, `database/schema.sql`

### 9. [MUST FIX] Default workouts in the seed data are empty

- Only “Push Day” has exercises. “Pull Day”, “Leg Day”, “Core Burner” and Sarah’s “Beginner Full Body” have none.
- They still show in the Schedule Workout dropdown. If scheduled, START shows “Failed to load workout” and closes.
- Fix: add exercises in seed.sql, and hide workouts with 0 exercises from the schedule list.

*Where:* `database/seed.sql`, `ScheduleWorkoutActivity`, `WorkoutExecutionActivity.loadWorkoutExercises`

### 10. [MUST FIX] Goal XP is 0 (and can crash) for lowercase priorities

- `Goal.getXpReward()` and `XPCalculator.calculateGoalXP()` only match “Top”, “Medium”, “Low” exactly. The seed goals use “medium” and “top”, so they give 0 XP and the Top seed goal shows as “Medium Priority”.
- A null priority makes `switch(priority)` throw a NullPointerException.
- Fix: normalise (`priority.toLowerCase()`) and fix the seed data to match what the app saves.

*Where:* `models/Goal.java`, `GoalCardAdapter.bind`, `database/seed.sql`

### 11. [MUST FIX] Goal-complete popup and share text show “---” and the notes

- Title and notes are stored together as `title + "\n---\n" + notes`. Every list screen splits them, but `GoalCompleteDialog` and the share message receive the full string, so users see the separator and their notes.
- Fix: pass only the title. Better long-term: add a `goal_title` column instead of the separator.

*Where:* `GoalsListFragment.checkLevelUpAfterGoal`, `GoalCompleteDialog`

### 12. [MUST FIX] Settings shows “Imperial” selected for every new user, and the setting does nothing

- The database default is `metric` (lowercase) but `SettingsActivity` compares with “Metric”, so new users see Imperial highlighted.
- The measurement system isn’t read anywhere else. Weights are always kg (“Weight (KG)”, “Weight (kg)”).
- Fix: compare case-insensitively, then either convert weights and labels or remove the setting.

*Where:* `SettingsActivity.updateMeasurementToggleUI`, `item_set_row.xml`, `strings.xml`

### 13. [MUST FIX] Female “toned” avatars wear clothes made for the average body

- Clothing is loaded from `assets/{type}/female-toned/`, but that folder is named `female-built`. The compositor falls back to `female-average`, so clothes don’t fit the toned body.
- Fix: rename the `female-built` folders to `female-toned` (tops, bottoms, shoes, socks), or map toned → built in `AvatarCompositor.loadClothingBitmap`.

*Where:* `AvatarCompositor.loadClothingBitmap`, `assets/*/female-built`

### 14. [TEST THIS] Changing gender after picking a body type

- The body-type page reads gender once, when its view is created. ViewPager2 keeps nearby pages alive, so Male → pick “Built” → Back → Female → Next may still show male bodies with “built” selected. “built” isn’t a female body type.
- Test it. If it happens, clear `bodyType` when gender changes and rebuild the body-type images in `onResume`.

*Where:* `AssessmentBodyTypeFragment`, `AssessmentGenderFragment`

### 15. [MUST FIX] The levelling curve makes ranks above Bronze unreachable

- The XP needed doubles every level: level 2 = 500, level 5 = 15,500, level 10 = 511,500, level 15 ≈ 16 million total XP. A workout earns about 150–300 XP, so Silver (level 15) and above can never be reached.
- Past level 22 the int maths overflows and the `while (shouldLevelUp…)` loop could spin forever.
- `MainActivity.loadTopBarData` and `MeFragment` silently “fix” the level from XP. Seed users like Dylan (level 10, 450 XP) drop to level 1, and any level gained this way skips the level-up dialog and coins.
- Fix: use a gentler curve (e.g. 500 × level, or +250 per level, sized so ranks are reachable in a semester) and handle level-ups in one shared method.

*Where:* `XPCalculator.xpForNextLevel/shouldLevelUp`, `MainActivity.loadTopBarData`, `MeFragment.displayUserProfile`, `seed.sql` (Ranks)

# 3. Database, setup and README

### 16. [YOUR NOTE] [MUST FIX] Shared database: things that will break or need care

- Table-name case (item 8) must be fixed first.
- Dates mix the phone’s date and the server’s. Home uses the phone date, but the “is this scheduled for today” check, the streak check and `countOtherCompletedToday` use MySQL `CURDATE()`. If the shared server runs on UTC, between 00:00 and 02:00 SA time the app says today’s workout “is not scheduled for today” and the streak uses the wrong day. Pass the phone’s date as a parameter everywhere.
- Goal and schedule dates are sent with a time of day. If the server timezone differs, a date can shift by one day. Test once the server is up.
- The app logs in as `root` with the real password compiled into the APK (BuildConfig). For a shared server, create a separate MySQL user with only the rights the app needs. local.properties is already git-ignored, which is good.

*Where:* `ScheduleRepository`, `WorkoutExecutionActivity.validateAndLoad`, `app/build.gradle`

### 17. [YOUR NOTE] Add more exercises, and split Legs into Quads and Hamstrings

- The muscle-group list is hard-coded in **five** files. Make it one shared constant first so the rename happens once.
- Seed rule from the file: rows 1–23 must keep their order because workouts reference them by id. Add new exercises at the end.
- Clean-up while you’re there: “Tricep Pushdown” and “Cable Tricep Pushdown” are the same exercise. “Squats” is under Legs but “Sumo Squats”, “Bulgarian Split Squats” and “Walking Lunges” are under Glutes. Plank is “30 reps” but means seconds.

*Where:* `WorkoutFragment`, `UserMuscleGroupSelectionFragment`, `GenerateWorkoutOptionsFragment`, `AdminCreateExerciseActivity`, `AdminCreateWorkoutActivity`, `seed.sql`

### 18. [SHOULD FIX] Seed data mistakes

- Dylan is given cosmetics 8 and 9. The comments say “White Sneakers” and “Grey Sweatpants”, but id 8 is the female Default Hair and id 9 is White T-Shirt.
- All test dates are in early September 2026, so in a demo everything shows as “Missed”. Consider dates relative to `CURDATE()` so the demo account always has something today and this week.
- The comment says female hair has no grey variant, but `grey_default_hairstyle.png` exists. Fix the comment.

*Where:* `database/seed.sql`

### 19. [SHOULD FIX] Every cosmetic is still free “for testing”

- All wearable items have `coin_cost 0`, so coins (from level-ups and rank-ups) can’t buy anything. The “Coming soon” placeholders show a price but can’t be bought.
- Decide the real prices before hand-in, or the shop and coins will look unfinished.

*Where:* `seed.sql`, migration 008

### 20. [SHOULD FIX] Multi-step saves aren’t wrapped in transactions

- Buying an item: coins are taken first, then the transaction row, then unlock. If a later step fails, the user loses coins without getting the item.
- Editing a workout deletes all its exercises, then re-inserts them. If the insert fails, the workout is left empty.
- Fix: `connection.setAutoCommit(false)` … `commit()` / `rollback()` inside the existing `executeAsync` blocks.

*Where:* `CosmeticRepository.purchaseCosmetic`, `WorkoutRepository.updateWorkout/createWorkout`

### 21. [NICE TO HAVE] Schema tidy-ups

- Column typo `measurment_system`.
- Goal has no title column (see item 11).
- XP_Log has both `amount` and `xp_earned` (always the same value).
- `Coin_Transaction` has no date column.
- The `Streak` table is never used by the app.
- Migrations start at 003. The README doesn’t say whether a fresh setup (schema + seed) should also run them.

*Where:* `database/schema.sql`, `database/migrations`

### 22. [YOUR NOTE] [SHOULD FIX] Update the README

- It says the app is “Kotlin + Compose” with `build.gradle.kts`. It is Java with Groovy `build.gradle`.
- The clone URL (`emma-dillemma/JVE-repsUP`) doesn’t match the actual remote (`JVE-WRRV301/JVE-repsUP`).
- The check queries use tables `exercises` and `users`, which don’t exist, and expect 23 exercises (there are 28).
- It explains `.env`, but the app actually reads `db.host/db.port/db.user/db.password/db.name` from `android/local.properties` (use `10.0.2.2` for the emulator). Document that.
- Add how to reach the admin side: long-press the “REPS UP.” title or log in as the admin user.

*Where:* `README.md`

# 4. Design consistency (whole app)

## 4.1 Rounding

You asked for everything to match the Back/Next buttons on the fitness assessment. Those are **28dp**. These are the radii currently used across the app:

| Radius | Where it’s used |
|---|---|
| 0 (square) | Workout library exercise rows (`item_exercise_list`), Settings rows & Log out, Account/Personal Details fields, set-row reps/weight boxes, sets/reps dialog fields, Level Up & Rank Up dialogs, all snackbars, Metric/Imperial toggle |
| 4dp (Material default) | Any button with no `cornerRadius`: workout experience, fitness goals, cm/ft, kg/lbs, Login, Change Password, Delete Account, Save Changes |
| 8dp | Goal importance buttons, schedule repeat buttons, purchase dialog buttons, workout dropdown, coin badge, priority badge, admin fields |
| 10dp | My Plan workout cards |
| 12dp | Home cards, Me cards, muscle-group cards, Welcome “Sign Up”, Create/Generate Workout buttons, Save Goal, Schedule, popup menu, search field |
| 16dp | Purchase / goal-complete / streak dialogs, locker item cells |
| 24dp | START buttons, workout Next/Continue, avatar Continue, locker & sign-up panels |
| 26dp | Sign-up password fields |
| 28dp | Assessment Back/Next, Get Started, “how many days” buttons, sign-up name/username/email fields |

### 23. [YOUR NOTE] [SHOULD FIX] Use one corner radius everywhere

- Add `res/values/dimens.xml` with `<dimen name="radius">28dp</dimen>` and use it in every shape drawable, `app:cornerRadius`, `cardCornerRadius` and `boxCornerRadius*`.
- Easiest way to cover all buttons: set `materialButtonStyle` in `Theme.RepsUp` to a style with `cornerRadius = @dimen/radius`. Every `<Button>` then gets it automatically, including the ones with no radius now. `styles.xml` already has `Widget.RepsUp.Button*` styles (24dp), but **no layout uses them**.
- Design note: 28dp on a 52dp button is a full pill. On large cards (Home TODAY card, Me avatar card) 28dp will look very round. Check how it looks; you may want a pill for buttons/fields/chips and something like 20dp for big cards. That’s your call.

*Where:* `styles.xml`, `themes.xml`, every `bg_*.xml` drawable, layouts listed in the table

### 24. [YOUR NOTE] [SHOULD FIX] Square popups (the “lunchbox” is a Snackbar)

- `SnackbarUtils.style()` calls `setBackgroundColor()`, which replaces Material’s rounded background with a flat rectangle. Every snackbar in the app is square because of this.
- Fix: `sbView.setBackgroundTintList(...)` keeps the rounded shape, or set a rounded drawable with the 28dp radius and add side margins so it floats.
- Level Up and Rank Up dialogs are square grey boxes with an emoji, while Goal Complete and Streak use rounded cards with images. Make all four dialogs share one card design.
- Workout execution still uses Toasts for errors, while the rest of the app uses snackbars and dialogs.

*Where:* `SnackbarUtils`, `dialog_level_up.xml`, `dialog_rank_up.xml`, `WorkoutExecutionActivity`

## 4.2 Back buttons, arrows and icons

### 25. [YOUR NOTE] [SHOULD FIX] Use the same back arrow everywhere

- The U-shaped arrow is Android’s built-in `ic_menu_revert`, used in Settings, Account, Personal Details and the Workout library muscle-group list. Everything else uses `@drawable/ic_back` (←). Swap them.
- Sizes and positions also vary (40dp vs 48dp buttons, different margins). Make one back-button style or include.
- Login has no back button at all.

*Where:* `activity_settings.xml`, `activity_account.xml`, `activity_personal_details.xml`, `fragment_workout.xml`, `activity_login.xml`

### 26. [YOUR NOTE] [SHOULD FIX] Calendar and My Plan arrows

- Month and week arrows use Android’s media-player rewind/forward icons (`ic_media_previous` / `ic_media_next`). Replace with ← / → (or chevrons matching `ic_back`).
- Day show/hide uses Android’s `arrow_up_float` (and rotates it). Use your own `ic_chevron_up` / `ic_chevron_down`, which the locker already uses. The admin Manage Workouts header uses the same system arrow.

*Where:* `fragment_calendar.xml`, `fragment_weekly_plan.xml`, `item_weekly_plan_day_header.xml`, `item_manage_workout_category_header.xml`

### 27. [SHOULD FIX] Other mismatched icons

- “+” on the Workout tab is Android’s `ic_input_add` tinted blue. On Calendar it’s your `ic_add_circle` in white. Use the same icon and colour.
- Remove-exercise uses `ic_menu_close_clear_cancel`, the reorder handle uses `ic_menu_sort_by_size`, admin uses `ic_menu_more` while users get `ic_more_vert`. These system icons look different on different phones; use your own vectors.
- Settings rows use a typed “›” character instead of an icon.
- The top-bar streak uses `ic_fire`; elsewhere it’s `ic_streak_fire`.

*Where:* `fragment_workout.xml`, `item_exercise_detail_row.xml`, `item_reorder_exercise.xml`, `activity_settings.xml`, `activity_main.xml`

## 4.3 Fields and buttons

### 28. [YOUR NOTE] [MUST FIX] Sign-up: password fields have no visible background

- Name, Username and Email are plain EditTexts using `bg_field_rounded` (colour `#5A5A66`, 28dp).
- Password and Confirm Password are TextInputLayouts with `boxBackgroundColor = dark_grey_surface` (`#4A4A55`). That’s the **same colour as the sign-up panel** behind them, so the box is invisible. They’re also 26dp, not 28dp.
- Fix: give all five fields the same background colour and radius (one style).

*Where:* `activity_register.xml`, `bg_field_rounded.xml`, `bg_register_panel.xml`

### 29. [SHOULD FIX] Six different text-field styles

- Sign-up: filled pill. Login: outlined, default corners, on a white panel. Create Goal / Schedule: outlined with a light-blue stroke. Workout name: default filled box. Settings/Account/Personal Details/sets dialog: square grey boxes. Workout sets: square boxes.
- Pick one (probably the sign-up pill, once fixed) and make it a style.

*Where:* layouts above

### 30. [SHOULD FIX] Login looks like a different app

- White bottom panel (every other screen is dark), outlined fields, square 4dp button, light-blue “Sign Up” on white (hard to read).
- The gym image never loads. `ivGymImage` has no `src` and the code never sets one, so the top half is empty dark grey.
- An empty field shows the hint word as the error (“Username” in red). Use “Please enter your username”.
- Fix: rebuild it to match the sign-up screen (image on top, dark rounded panel, pill fields, back arrow).

*Where:* `activity_login.xml`, `LoginActivity.attemptLogin`

### 31. [SHOULD FIX] Selected / disabled states differ between screens

- Selected options: assessment = light-blue fill (4dp or 28dp), goal importance and repeat = light-blue fill at 8dp, Generate difficulty = outlined chips, Metric/Imperial = square text toggle. Use one selectable-button style.
- Disabled Next: the assessment turns grey; workout execution fades to 50%; other screens never disable. Use one look.
- START buttons are TextViews with no press feedback (no ripple). Make them real buttons with the shared style.

*Where:* `fragment_generate_workout_options.xml`, `activity_settings.xml`, `item_day_content.xml`, `item_weekly_plan_workout.xml`, `fragment_home.xml`

## 4.4 Text, colours and wording

### 32. [SHOULD FIX] Headings and fonts

- Page titles range from 20sp to 28sp. Some use `orbitron_bold`, others `orbitron` plus `textStyle="bold"` (fake bold, looks different). E.g. “Workout” is Orbitron Regular 22sp, “MY SCHEDULE” is Orbitron Bold 22sp, “GREAT WORKOUT!” is Orbitron + bold.
- Both `@font/inter` and `@font/inter_regular` are used for the same thing.
- Use the `TextAppearance.RepsUp.*` styles that already exist in styles.xml (currently unused).

*Where:* `styles.xml`, all layouts

### 33. [SHOULD FIX] Wording and capitalisation

- Goals: “Stay Consistent” vs “Lose weight”, “Build muscle”. Use one style.
- “Lets Go” is missing its apostrophe (Let’s Go).
- “Log out” (settings) vs “Logout” (admin); app name written as “RepsUp”, “REPS UP.” and “Reps Up”.
- Spelling mix: “personalise” vs “customize”. Pick British or American.
- Units: “KG”, “LBS”, “CM”, “FT” on the assessment vs “kg” elsewhere.
- Difficulty shows raw database text (“medium”) on the workout screen. Capitalise it.
- Dates use five formats: “15 Sunday” (goal cards), “Jun 2 - Jun 8”, “2026-10-09” (date fields), “Oct 9, 2026”, “Friday, Oct 9”. Pick one or two.
- XP is yellow (the coin colour) on Workout Complete and Goal Complete but light blue on the Me tab. Use one colour per thing: coins yellow, XP blue.
- Text written directly in layouts/code instead of strings.xml: purchase dialog “Cancel / Buy / Not enough coins”, top bar “Lv. 1”, “Failed to save profile. Please try again.”, workout share text.

*Where:* `strings.xml`, `dialog_purchase.xml`, `activity_main.xml`, `FitnessAssessmentActivity`, `WorkoutFragment.onWorkoutShare`

### 34. [SHOULD FIX] The week starts on different days

- The Home week strip and Calendar start on Sunday. My Plan starts on Monday. Pick one (Monday is usual in SA).

*Where:* `HomeFragment.highlightToday`, `CalendarFragment.buildDaysList`, `WeeklyPlanFragment.getStartOfWeek`

### 35. [SHOULD FIX] Dark-mode theme file is out of date

- `values-night/themes.xml` is an older copy of the theme. It lacks the rounded popup-menu style and the navigation-bar colour, and the status bar differs. On a phone with dark mode on, the ⋮ menus and system bars look different from your emulator.
- Fix: delete `values-night/themes.xml` (the app is always dark anyway) or make it identical.

*Where:* `res/values-night/themes.xml`

# 5. Screen by screen

## 5.1 Welcome, Sign up, Login

### 36. [SHOULD FIX] Welcome “Sign Up” button is 12dp while the sign-up “Get Started” is 28dp

- Covered by the rounding fix, but this is the first screen anyone sees.

*Where:* `activity_welcome.xml`

## 5.2 Fitness assessment and avatar

### 37. [YOUR NOTE] [SHOULD FIX] The “how many days per week” page opens with 3 already selected

- `AssessmentData.workoutsPerWeek` defaults to 3 and `validateCurrentStep()` treats that step as always valid.
- Fix: default it to 0, require a selection for step 7 (like body type and workout level), so Next starts greyed out.

*Where:* `AssessmentData`, `FitnessAssessmentActivity.validateCurrentStep`

### 38. [YOUR NOTE] [SHOULD FIX] cm/ft, kg/lbs, workout experience and goal buttons are square (4dp)

- Covered by the rounding fix.

*Where:* `fragment_assessment_height.xml`, `fragment_assessment_weight.xml`, `fragment_assessment_workout_level.xml`, `fragment_assessment_goals.xml`

### 39. [SHOULD FIX] Assessment layout issues

- The top gap is 36dp on the root plus 36dp on the inner frame (72dp, hard-coded) instead of using the status-bar inset, so it looks different on every phone.
- Workout-level and goals pages aren’t scrollable. With 5 × 56dp buttons they can be cut off on small phones. The “days” page buttons are 48dp tall while the others are 56dp.
- The goals page says “Select all that apply”, but selected buttons look the same as single-choice ones. Consider a tick icon.

*Where:* `activity_fitness_assessment.xml`, `fragment_assessment_*.xml`

### 40. [TEST THIS] “Meet your avatar” may not match the avatar on the Me tab

- This screen shows a pre-drawn image (`avatar_{gender}_{type}.png`, about 700KB each), not the composited avatar (grey skin, default tank/shorts/socks/hair) the user sees afterwards. Check they look the same, or use `AvatarCompositor.composite` here too.

*Where:* `AvatarGenerationActivity.displayAvatar`

## 5.3 Home

### 41. [SHOULD FIX] Home issues

- If the database can’t be reached, two “Something went wrong” dialogs stack (workout + goal), and they come back every time you switch tabs.
- Only the first 3 exercises show, with no “+N more”. Use the same expand pattern you want for Saved workouts.
- The goal priority badge shows the raw word in light blue (“Top”), while the Goals list shows “Top Priority” in red. Match them.
- If two workouts are scheduled today, only one appears.

*Where:* `HomeFragment`, `ScheduleRepository.getTodaysWorkout` (LIMIT 1 / LIMIT 3)

### 42. [NICE TO HAVE] Make the week strip tappable

- Tapping a day could open that day in the Calendar.

*Where:* `fragment_home.xml`

## 5.4 Calendar, My Plan and Goals

### 43. [YOUR NOTE] [SHOULD FIX] “No goals yet…” sits too close to the tab line

- `tvGoalsEmpty` is centred inside a `wrap_content` FrameLayout, so it ends up at the very top. Give it a top margin (~24–32dp) and use the same position and padding as the My Plan empty text so both match.

*Where:* `fragment_goals_list.xml`

### 44. [YOUR NOTE] [SHOULD FIX] “No workouts scheduled…” appears below Sunday

- `tvEmptyState` comes after the RecyclerView, which always draws 7 day headers. Move the TextView above the list (or hide the day list when the week is empty) and match the Goals empty-state position.

*Where:* `fragment_weekly_plan.xml`, `WeeklyPlanFragment.loadWeekData`

### 45. [SHOULD FIX] Calendar behaviour bugs

- The Calendar opens on the Goals tab even though My Plan is the first tab (`setupTabs` selects index 1).
- After moving to another month, the same day number stays outlined (the adapter’s `selectedDay` isn’t reset).
- The selected-day outline uses 8 and 2 **pixels** (not dp), so on high-resolution phones it’s a thin, almost square line.
- Tapping a day that only has a goal (red dot) shows nothing. The day panel only lists workouts. Tapping an empty day also shows nothing; add a “Nothing planned” message.
- The selected-day panel doesn’t refresh after you finish a workout, so it still shows START.
- Goal and workout dots differ only by colour (the code comment says “triangle”). Consider a different shape for colour-blind users.

*Where:* `CalendarFragment`, `CalendarDayAdapter`, `item_calendar_day.xml`

### 46. [SHOULD FIX] Repeating schedules

- “Monthly” means every 30 days, so dates drift (31 Jan → 2 Mar). Use `Calendar.MONTH`.
- Exactly 12 copies are created. Weekly stops after 3 months with no warning.
- Delete removes only one occurrence, so there’s no way to remove a whole series. Deleting fails silently on error.

*Where:* `ScheduleRepository.scheduleWorkout`, `WeeklyPlanFragment`

### 47. [SHOULD FIX] Goals list

- Completed goals stay mixed in with active ones and look the same apart from the tick. Grey them out or move them to the bottom. Overdue goals aren’t flagged.
- Ticking a goal is instant and can’t be undone (no confirmation), so an accidental tap gives up to 1000 XP.
- The delete confirmation uses Android’s generic “OK / Cancel”. Use “Delete / Cancel” like the workout delete dialog.
- The ⋮ button on goal and saved-workout cards is 24dp. Android’s minimum touch target is 48dp.

*Where:* `GoalCardAdapter`, `GoalsListFragment.onGoalDelete`, `item_goal_card.xml`, `item_workout_card.xml`

## 5.5 Workout tab and Create Workout

### 48. [YOUR NOTE] [SHOULD FIX] Workout library exercise rows are square grey boxes

- `item_exercise_list.xml` uses a plain `@color/dark_grey_surface` background. Use the rounded card. Tapping an exercise also does nothing (`onExerciseClick` is empty): show details, or at least don’t look tappable.

*Where:* `item_exercise_list.xml`, `WorkoutFragment.onExerciseClick`

### 49. [YOUR NOTE] [SHOULD FIX] Saved workouts: show “+N more” and let it expand

- `WorkoutCardAdapter` shows at most 3 exercises (`MAX_PREVIEW_EXERCISES`) with no indicator. My Plan already has an “and X more” pattern (`weekly_plan_and_more_format`), so reuse it and make it expand on tap.

*Where:* `WorkoutCardAdapter`, `item_workout_card.xml`

### 50. [YOUR NOTE] [SHOULD FIX] Create Workout screen: add a short description under each option

- `fragment_create_workout_mode.xml` has just the two buttons. Add a line under each, e.g. “Pick your own exercises” and “We’ll build one for you from the muscle groups you choose”.

*Where:* `fragment_create_workout_mode.xml`

### 51. [YOUR NOTE] [SHOULD FIX] New per-muscle-group exercise picker (with search)

- Confirms your point: today every chosen group is loaded at once and merged into one list. Each group loads separately, so the groups appear in a **different order each time**. This will get much worse with more exercises.
- Your tabs + search idea is marked “check with group”. Either way, the implementation tip is the same: load all exercises in one query (`getAllExercises` already exists) and filter in memory. Search is then instant and can jump to the right tab, as you described.
- Keep a running “X added” count across groups (the admin flow already has one, `tvAddedCount`).

*Where:* `UserMuscleGroupSelectionFragment`, `UserExerciseSelectionFragment`, `CreateWorkoutActivity`

### 52. [SHOULD FIX] Create Workout logic

- There’s no field for the number of sets: users set reps and weight, and every exercise gets its default sets. Editing a workout also resets any custom sets back to the defaults.
- Workouts are capped at 8 exercises (in three places). With a bigger library, decide if that’s still right. When adding more to an existing workout, “x / 8 selected” ignores the ones already in it, so extras are dropped quietly.
- Save can be tapped twice during the 1.5-second delay before the screen closes, creating duplicate workouts. Disable it after the first tap. The same applies to the locker Save and the admin screens.
- Generate picks up to 8 random exercises across all chosen groups, so “Chest + Legs” can come out as 8 chest exercises. It always gives 8 regardless of difficulty and ignores the experience level from the assessment. Round-robin across groups and scale the count by difficulty.

*Where:* `CreateWorkoutActivity`, `UserWorkoutDetailsFragment`, `WorkoutValidator`

### 53. [SHOULD FIX] Saved workouts: missing actions

- Tapping a saved workout card does nothing, and you can’t start or schedule it from there. The only way is Calendar → + → Schedule. Add “Schedule” to the ⋮ menu (or a “Start now”).
- Admin default workouts aren’t visible anywhere except the schedule dropdown, so users can’t see what’s inside “Push Day” before scheduling it.
- Deleting a workout also deletes all its scheduled **and completed** history (ON DELETE CASCADE) without warning. Say so in the confirmation.
- The reorder dialog rows have a **white** background (`item_reorder_exercise.xml`), the only light element in the app.

*Where:* `WorkoutFragment`, `item_reorder_exercise.xml`

## 5.6 Doing a workout

### 54. [SHOULD FIX] Workout execution

- You can add sets but not remove them, and every set must be ticked before Next works. There’s no way to skip an exercise or do fewer sets.
- XP comes from ticked sets only, so ticking every box straight away gives full XP (0 reps is accepted).
- No quit button on the first exercise and no “Are you sure?” on Back, so all progress is lost with one tap.
- Demo images exist for only 3 exercises (bench press, shoulder press, lateral raises; file name typo `lataeral_raises_avatar`). Everything else shows the placeholder, and `assets/exercise_demos` is empty. Worth deciding how this looks with many more exercises.
- The admin “Select Demo Image” button has no click handler, so it does nothing.

*Where:* `WorkoutExecutionActivity`, `AdminCreateExerciseActivity`

### 55. [SHOULD FIX] Workout Complete

- If Continue is tapped before the XP update finishes, the level-up check reads old XP and misses the level-up. MainActivity then quietly corrects the level later with no dialog and no coins (see item 15).
- The screen has no status/navigation bar padding, so the Continue button can sit under the gesture bar on newer phones.

*Where:* `WorkoutCompleteActivity.navigateHome`

## 5.7 Locker and Me

### 56. [YOUR NOTE] [SHOULD FIX] Remove the Special tab and centre Top / Bottom / Shoes

- Remove `"Special"` from `CLOTHING_SUBTABS` (and its `currentClothingType` case and string).
- The tabs sit left because `tab_subcategories` uses `app:tabMode="scrollable"`. Change it to `fixed` with `tabGravity="fill"`, like the main Body/Hair/Clothing tabs.

*Where:* `CustomizeAvatarActivity`, `activity_customize_avatar.xml`

### 57. [YOUR NOTE] [SHOULD FIX] Body-type thumbnails stay grey

- `createBodyTypeView` always loads `avatar_{gender}_{bodytype}` (grey). Drawables for every tone already exist: `base_{gender}_{bodytype}_{fair|tan|dark|grey}`. Load the one for the current skin tone and refresh the grid in `onSkinToneSelected`.

*Where:* `CustomizeAvatarActivity.createBodyTypeView`

### 58. [YOUR NOTE] [SHOULD FIX] Arrow should lower the panel further

- The expanded avatar height is a fixed `AVATAR_HEIGHT_EXPANDED = 420dp`, so the result differs on every phone. Calculate it instead: screen height − top bar − arrow − both tab rows − one grid row. Then only the tabs and the first row stay visible, as you want.

*Where:* `CustomizeAvatarActivity.toggleExpand`

### 59. [YOUR NOTE] Buzz cut: skin shows around the face

- Art fix (`hair/male-hair/brown_buzz_hairstyle.png`).

*Where:* assets

### 60. [SHOULD FIX] Other locker issues

- The lock overlay on locked items has `layout_marginTop="4dp"` only, so it’s shifted down 4dp compared with the cell.
- “Coming soon” items show a price but can’t be bought. Show a “Soon” label instead of a price.
- A failed purchase says “Failed to save outfit”. Needs its own message.
- “Outfit saved!” shows even if saving skin tone, body or hair colour failed (those saves ignore errors). Save can also be tapped again during the 1.5-second delay.
- The class comment still describes a Face tab, Accessories and 9 placeholders per tab. Update it.

*Where:* `item_locker_placeholder.xml`, `CustomizeAvatarActivity`

### 61. [SHOULD FIX] Me tab

- The rank medal and rank-name colour (bronze/orange) never change, even for Silver, Gold or Diamond.
- Every time the tab is shown it redraws the full avatar and the top-bar face, which is noticeably slow once the database is remote.

*Where:* `MeFragment.displayUserProfile`, `fragment_me.xml`

## 5.8 Settings, Account, Personal details

### 62. [SHOULD FIX] Settings screens

- The Language row has a chevron and a press effect but does nothing. Remove it or make it non-clickable.
- Square rows, fields and buttons plus the U-turn back arrow (covered above).
- Personal Details doesn’t check whether the new email is already used, so you get a generic “Failed to save”.

*Where:* `activity_settings.xml`, `SettingsActivity`, `PersonalDetailsActivity`

# 6. Game logic (XP, coins, goals)

### 63. [SHOULD FIX] Goals are an easy XP exploit

- A user can create a goal, tick it immediately and get 1000 XP, more than several workouts. Consider less XP, a limit per day or week, or only allowing completion on or after a minimum time.

*Where:* `Goal.getXpReward`, `GoalsListFragment.onGoalComplete`

### 64. [NICE TO HAVE] Levelling and rewards in one place

- Level-up, coin and rank logic is copied in GoalsListFragment, WorkoutCompleteActivity, MainActivity and MeFragment, and they don’t all reward the same way. One `ProgressionManager` method called after any XP change would remove the mismatches in items 15 and 55.

*Where:* see above

# 7. Real-phone readiness

### 65. [MUST FIX] [TEST THIS] Edge-to-edge on Android 15+

- With targetSdk 36, real phones on Android 15+ draw the app under the status and navigation bars. Screens without inset handling will have content under the clock or gesture bar: Login, Workout Complete, Workout Execution, Fitness Assessment (hard-coded 72dp) and Locker (hard-coded 32dp).
- Test on a real phone or an API 35+ emulator with gesture navigation.

*Where:* layouts listed

### 66. [TEST THIS] Small screens and large text

- Try a small phone (~5") and Settings → Display → Font size at maximum. The assessment pages, Home cards and locker grid are the most likely to clip.

### 67. [SHOULD FIX] Too many database calls

- All four tab fragments get `onResume` each time you return to MainActivity, so every tab reloads (plus avatar renders) even when hidden. On a remote database this will feel slow. Only refresh the visible tab.

*Where:* `MainActivity`, all tab fragments

# 8. Clean-up before hand-in

### 68. [SHOULD FIX] Remove stray files

- Root: `e -i parent-hash` (looks like an accidental git output file), `test-output.txt`, the `output/` folder.
- `android/`: `gradle_out.txt`, `exit_code.txt`, `build_check2.log`, `test_error.txt`, `test_run_output.txt`, `app/.jqwik-database`. Most of these are committed to git.
- Decide with the group whether the `.kiro/` folder (AI-tool spec files) should be in the submitted repo.

*Where:* repo root

### 69. [NICE TO HAVE] Dead or duplicate code

- `layout_top_bar.xml` is never used (MainActivity has its own top bar).
- `CalendarFragment.PlaceholderFragment` and the “coming soon” strings are unused.
- `AvatarCompositor.composite(Context, Avatar, List)` hard-codes “male” and is unused. Delete it before someone calls it.
- `bg_button_rounded`, `bg_button_primary` and `bg_start_button` are identical.
- The `Widget.RepsUp.*` and `TextAppearance.RepsUp.*` styles are defined but never used. Use them (section 4) or delete them.
- Several error callbacks say “Log error silently” but don’t log anything, which makes bugs hard to trace. Add `Log.e`.
- The `DialogUtils` comment says hints are still Toasts, which is no longer true.

*Where:* files named

# 9. Re-test checklist after fixing

- [ ] Sign up with a non-Gmail address (e.g. @mandela.ac.za) and with 4–5 goals selected.
- [ ] Kill the app during the assessment, reopen it, and make sure you land back in the assessment.
- [ ] Press Back on every assessment and avatar screen; no duplicate-profile error.
- [ ] Log in with the password in the wrong case; it must fail.
- [ ] Complete a workout, close and reopen the app 3 times the next day; streak should be 1, not 4.
- [ ] Rotate the phone on every screen, especially mid-workout and on Workout Complete (XP added once only).
- [ ] Schedule “Pull Day” and press START.
- [ ] Schedule a default workout, Edit it from My Plan, then check another account still has the original.
- [ ] Create a female, toned account and try on clothes.
- [ ] Change Male → Female after choosing a body type.
- [ ] Complete a goal with notes; check the popup and the share text.
- [ ] Open Settings as a new user; Metric should be selected.
- [ ] Point the app at the shared database (Linux host) and click through every tab; test between 00:00 and 02:00 if the server runs on UTC.
- [ ] Test with the keyboard open on every form.
- [ ] Run on a real Android 15+ phone with gesture navigation and dark mode on.