# Reps Up — Technical Context

## Tech stack

- **Language:** Java
- **Platform:** Android (native)
- **Minimum SDK:** API 26 (Android 8.0)
- **UI approach:** XML layouts + Java Activities (NOT Jetpack Compose)
- **Build system:** Gradle (Kotlin DSL, `build.gradle.kts`)
- **Database:** MySQL 8 (local during development)
- **Version control:** Git + GitHub, hosted at github.com/JVE-RepsUp/JVE-repsUP
- **IDE:** Kiro (primary editor) + Android Studio (for builds, emulator, layout preview)

## Project structure
JVE-repsUP/
├── .kiro/ Kiro spec-driven development files
│ ├── steering/ Project-wide AI context (read every conversation)
│ └── specs/ Feature specifications
├── android/ The Android app project
│ ├── app/ App source code
│ │ └── src/main/
│ │ ├── java/com/jve/repsup/ Java source files
│ │ └── res/ XML resources (layouts, values, drawables)
│ └── build.gradle.kts Top-level build config
├── database/
│ ├── schema.sql 16-table MySQL schema
│ ├── seed.sql Development data
│ └── migrations/ Numbered schema changes
├── .env.example Environment variable template
└── README.md Setup instructions

## Coding conventions

- **Java classes:** PascalCase (`RegisterActivity`, `WorkoutViewModel`)
- **Java methods and variables:** camelCase (`getUserById`, `currentStreak`)
- **XML files:** snake_case (`activity_register.xml`, `item_workout.xml`)
- **String, colour, dimension resources:** snake_case (`@string/welcome_message`)
- **Database columns:** snake_case (`user_id`, `curr_streak`)
- **Package name:** `com.jve.repsup`

## UI design system

Colours and fonts follow the Functional Specification & Solution Blueprint (page 10).

- Background: `#393944` (dark grey)
- Accent: `#B2D6FD` (light blue, used for all buttons)
- Alert: `#FF5050` (red)
- Highlight: `#FAD942` (yellow)
- Text: `#FFFFFF` (white)
- Headings: Orbitron (used as a free alternative to HK Modular from the spec)
- Body: Inter
- The app is always dark mode (does not switch to light mode based on system settings)

## Database

- 16 tables covering users, workouts, exercises, goals, streaks, cosmetics, ranks, XP logs, etc.
- Full schema in `database/schema.sql`
- Seed data in `database/seed.sql` (3 test users, 23 exercises, 15 ranks, 10 cosmetics, default workouts)
- Passwords must be hashed (e.g. bcrypt) — never stored in plain text
- Foreign keys use `ON DELETE CASCADE` to avoid orphaned rows

## How the Android app talks to MySQL

Mobile apps should NOT connect to MySQL directly (exposes credentials in the app bundle). The plan is to build a small REST API in between:
Android App → REST API (TBD) → MySQL Database

The API layer has not been built yet. For now, screens should be built with mocked/hardcoded data. The API is a future task.