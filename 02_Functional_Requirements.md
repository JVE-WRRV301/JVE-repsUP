# Functional Requirements & UI Guidelines

## 2.4.1 UI Guidelines
- **Theme:** Dark mode inspired by gaming applications (reduces eye strain in low-lit gyms).
- **Primary Color:** Dark Grey (`#393944`) for backgrounds.
- **Accent Color:** Light Blue (`#B2D6FD`) for buttons and highlights.
- **Highlight Colors:** Red (`#FF5050`) and Yellow (`#FAD942`) for warnings and XP/Coins indicators.
- **Typography:**
  - Headings: `HK MODULAR` (Game-like look).
  - Body Text: `Inter` (Simple and readable).

## 2.4.2 Use Case Narratives

### Team Features (Login & Dashboard)
- **Login/Logout:** Users log in via username/password. Logout requires a confirmation pop-up.
- **Dashboard (Home):**
  - Top: Avatar, Level, Streak Count (Fire icon), Coin Count.
  - Week Strip: Shows the current week days; highlighted day shows today.
  - **TODAY Section:** Displays the workout scheduled for today. Includes a "START" button.
  - **NEXT GOAL Section:** Displays the current user goal with a progress/completion checkbox.

### User-Facing Features (Allocated to James Ford)
- **A100 Register User:** User enters Name, Username, Email, Password, Confirms Password.
- **A110 Assess Fitness:** Multi-step form: Weight, Height, Body Type (select from 6 visual styles).
- **A111 Generate Avatar:** System automatically creates a default avatar based on the fitness assessment data.
- **A200 Track Streak:** System checks at 12 AM if the user completed their scheduled workout. If yes, streak increments; if no, streak resets.
- **A300 Earn XP:** Awarded upon completing a workout or goal.
- **A310 Level Up:** Triggered when XP threshold is met. Resets XP bar and increases required XP for the next level.
- **A311 Earn Coins:** Users earn coins upon leveling up. Bonus coins are awarded on rank-up levels.
- **A320 Rank Up:** When a user hits a rank-up level, a pop-up appears. Ranks are tiered (e.g., Bronze II).
- **A321/A400/A500 Share Features:** Users can share Rank Ups, Streak Milestones, and Completed Goals via native share sheet (WhatsApp, Instagram, Notes, etc.).

### Workout & Scheduling Features (Allocated to Vincent van Niekerk)
- **B100 View Home Page:** Displays the dashboard (as described above). "Start Workout" triggers B110.
- **B110 Start Workout:**
  - Opens the workout screen showing the current exercise.
  - Each set has a checkbox, Reps input, and Weight input.
  - User can "Add Set" or move to "Next" exercise only after checking all boxes for the current set.
- **B111 Complete Workout:** When the final exercise is finished, a summary screen appears showing Muscles Worked and total XP earned.
- **B200 View Schedule Page:** A calendar view showing which days have workouts/goals scheduled.
- **B210 Schedule Workout:** User selects a workout from their library, picks a date, and sets a repetition interval (e.g., "Every week").
- **B220/B221/B223 View/Set/Manage Goals:** Users can set a Goal (Title, Deadline, Priority, Notes). Goals appear in the "Goals" tab on the Schedule page. Users can Edit or Delete goals via a 3-dot menu.
- **B230 Edit Calendar:** Tapping a day on the calendar allows the user to edit the workout or goal scheduled for that specific day.
- **B240 View Weekly Plan:** Shows a list of workouts scheduled for the current week.

### Avatar & Workout Library Features (Allocated to Emma Martin)
- **C100 View Avatar Page:** Displays the user's 3D avatar, Gym Rank (Medal), and Level progress.
- **C110 View Locker:** Users view cosmetic items (Tops, Bottoms, Shoes, Accessories).
- **C111 Unlock Cosmetic:** Users purchase locked items using Coins (if they have enough balance).
- **C112 Customize Avatar:** Users can tap unlocked items to equip them. The avatar updates in real-time.
- **C113 Manage User Profile:** Access settings via a gear icon (Change preferences, Log out).
- **C200 View Workout Page:** Displays two tabs: "Saved Workouts" and "Exercise Library".
- **C210 Create User Workout:**
  - **Flow 1 (Generate):** User selects difficulty and muscle groups; system generates a workout.
  - **Flow 2 (Custom):** User selects specific exercises from the library, sets reps/weight, and saves the workout.
- **C220 Manage Workout:** Users can Share, Edit, Duplicate, Reorder, or Delete saved workouts via a 3-dot menu.

### Admin Features (Allocated to Emma Martin)
- **C300 Create Default Workout:** Admins create preset workouts for the system (used for generation).
- **C400 Create Default Exercise:** Admins add new exercises (Name, Demo animation, Difficulty, Muscle Group).
- **C500 Manage Default Workout:** Admins can Edit or Delete default system workouts.