# Reps Up — Technical Context

## Tech stack

- **Language:** Java
- **Platform:** Android (native)
- **Minimum SDK:** API 26 (Android 8.0)
- **UI approach:** XML layouts + Java Activities (NOT Jetpack Compose)
- **Build system:** Gradle (Groovy DSL, `build.gradle`)
- **Database:** MySQL 8 (local during development)
- **DB connection:** Direct JDBC via `mysql-connector-j:8.2.0` (background threads only)
- **Version control:** Git + GitHub, hosted at github.com/JVE-WRRV301/JVE-repsUP
- **IDE:** Kiro (primary editor) + Android Studio (for builds, emulator, layout preview)

## Project structure

```
JVE-repsUP/
├── .kiro/                  Kiro spec-driven development files
│   ├── steering/           Project-wide AI context (read every conversation)
│   └── specs/              Feature specifications (per use case)
├── android/                The Android app project
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/jve/repsup/   Java source files
│   │       └── res/                    XML resources (layouts, values, drawables)
│   ├── build.gradle        Top-level build config
│   └── local.properties    DB credentials (gitignored)
├── database/
│   ├── schema.sql          16-table MySQL schema
│   ├── seed.sql            Development data
│   └── migrations/         Numbered schema changes
├── docs/                   Team documentation
│   └── plan.md             Full implementation plan & task breakdown
├── .env.example            Environment variable template
├── .gitignore
└── README.md               Setup instructions
```

## Database connection approach

The app connects directly to MySQL via JDBC for development:
- Credentials are stored in `android/local.properties` (gitignored)
- `build.gradle` reads `local.properties` and injects values as `BuildConfig` fields
- `DatabaseHelper.java` uses `BuildConfig.DB_HOST`, `BuildConfig.DB_PASSWORD`, etc.
- The emulator reaches the host machine's MySQL via `10.0.2.2` (Android's alias for host localhost)
- All JDBC calls run on a background thread pool via `ConnectionManager.executeAsync()`

## Coding conventions

- **Java classes:** PascalCase (`RegisterActivity`, `WorkoutViewModel`)
- **Java methods and variables:** camelCase (`getUserById`, `currentStreak`)
- **XML files:** snake_case (`activity_register.xml`, `item_workout.xml`)
- **String, colour, dimension resources:** snake_case (`@string/welcome_message`)
- **Database columns:** snake_case (`user_id`, `current_streak`)
- **Package name:** `com.jve.repsup`

## UI design system

- Background: `#393944` (dark grey)
- Surface/cards: `#4A4A55` (dark grey surface)
- Accent: `#B2D6FD` (light blue, used for primary buttons and highlights)
- Alert: `#FF5050` (red, used for goal cards)
- Highlight: `#FAD942` (yellow)
- Text: `#FFFFFF` (white primary), `#B0B0B8` (muted secondary)
- Dark text (on light buttons): `#1A1A2E`
- Headings: Orbitron font
- Body: Inter font
- The app is always dark mode — no light mode

## Database table names (actual schema)

Use these exact names when writing queries:
- `user` (not users)
- `exercise` (not exercises)
- `workout`
- `workout_exercise`
- `scheduled_workout`
- `goal`
- `xp_log`
- `coin_transaction`
- `ranks`
- `cosmetic_item`
- `user_cosmetic`
- `outfit_slot`
- `avatar`
- `user_profile`
- `streak`
- `admins`

## Key column name mappings

- User's name: `first_name` (not `name`)
- User's level: `current_level` (not `level`)
- User's password: `set_password` (not `password`)
- User's measurement system: `measurment_system` (note: typo in schema, keep as-is)
- User's language: `set_language`

## Team ownership

- **Vincent:** Login, Register, Workout pages, Start/Complete Workout, Level Up, Rank Up
- **Emma:** Fitness Assessment, Avatar, Calendar/Schedule, Locker/Cosmetics
- **James:** Foundation/setup, Admin features, Home page, Goals, Streaks, XP, Profile
