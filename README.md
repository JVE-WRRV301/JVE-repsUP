# Reps Up

A gamified mobile fitness application that helps users schedule, track, and execute workouts, with XP, levels, ranks, and avatar customization to keep them motivated.

**Team JVE** - James Ford, Vincent van Niekerk, Emma Martin

---

## Tech Stack

- **Mobile app:** Android (Java), built in Android Studio
- **Database:** MySQL
- **Version control:** Git + GitHub

---

## Android App Setup

### Prerequisites

- [Android Studio] installed (latest stable version)
- An Android device or emulator to run the app on

### Opening the project

1. Clone the repo if you haven't already (see [Database Setup](#database-setup) below for the clone command).
2. Open Android Studio.
3. On the Welcome screen, click **Open**.
4. Navigate to the `android/` folder inside your cloned repo and click **Open**.
5. If prompted to trust the project, click **Trust Project**.
6. Android Studio will run Gradle sync. The first sync takes 5–15 minutes - it downloads SDK components and dependencies. Subsequent opens are fast.
7. When sync finishes, you should see `MainActivity.java` in the editor and "BUILD SUCCESSFUL" in the bottom panel.

### Running the app

1. At the top of the Android Studio window, you'll see a green play button ▶ with a device selector to its left.
2. To use an emulator: click the device selector → **Device Manager** → **Create Device** → pick a Pixel model → pick a system image (download if prompted) → Finish.
3. To use a real phone: enable Developer Options and USB Debugging on the phone, plug it in via USB, and select it from the device dropdown.
4. Click the green play button to build and install the app on your chosen device.

### Troubleshooting

**Gradle sync fails with "SDK location not found"**
Android Studio can't find your Android SDK. Go to **File → Project Structure → SDK Location** and set the Android SDK path (usually `C:\Users\YourName\AppData\Local\Android\Sdk` on Windows).

**"Add files to Git" popup appears**
Always click **Cancel**. Git is handled at the repo level, not by Android Studio.

---

## Database Setup

Follow these steps to get the Reps Up database running on your local machine.

### Prerequisites

- MySQL installed and running locally 
- Git installed
- A terminal (Command Prompt on Windows, Terminal on Mac/Linux)

### 1. Clone the repository

```bash
git clone https://github.com/emma-dillemma/JVE-repsUP.git
cd JVE-repsUP
```

### 2. Build the database

From the project root, open a terminal and run:

**PowerShell:**
```powershell
Get-Content database/schema.sql | mysql -u root -p
```

**Command Prompt:**
```cmd
mysql -u root -p < database\schema.sql
```

You'll be prompted for your MySQL root password. After entering it, this creates the `reps_up` database and all its tables.

### 3. Load the seed data

**PowerShell:**
```powershell
Get-Content database/seed.sql | mysql -u root -p
```

**Command Prompt:**
```cmd
mysql -u root -p < database\seed.sql
```

This populates the database with default ranks, the exercise library, default workouts, cosmetic items, and a few test users.

### 4. Set up your local environment file

**PowerShell:**
```powershell
cp .env.example .env
```

**Command Prompt:**
```cmd
copy .env.example .env
```

Then open `.env` in your editor and replace `your_mysql_password_here` with your actual MySQL root password. **Never commit this file** — it's already in `.gitignore`.

### 5. Verify the setup

Log into MySQL:

```bash
mysql -u root -p
```

Then run:

```sql
USE reps_up;
SHOW TABLES;
SELECT COUNT(*) FROM exercises;
SELECT username, current_level FROM users;
```

You should see 16 tables, 23 exercises, and 3 test users (dyllan12345, sarahj, jaskrug). If you do, your setup is working.

---

## Project Structure

```
JVE-repsUP/
├── android/                Android Studio project (Kotlin + Compose)
│   ├── app/                App source code
│   ├── gradle/             Gradle wrapper
│   └── build.gradle.kts    Top-level build config
├── database/
│   ├── schema.sql          Database structure (tables, foreign keys, indexes)
│   ├── seed.sql            Sample data for development
│   └── migrations/         Numbered schema change files (see below)
├── .env.example            Template for environment variables
├── .gitignore              Files Git should never commit
└── README.md               This file

```

---

## Schema Changes (Migrations)

**Do not edit `schema.sql` after initial setup.** Once everyone has the database running with their own test data, editing the schema directly would force teammates to drop and rebuild - losing their work.

Instead, schema changes are done through **migration files**. Each change gets its own numbered SQL file in `database/migrations/`.

### Creating a migration

1. Create a new file in `database/migrations/` named like `001_short_description.sql`
2. Increment the number from the previous migration
3. Write only the changes (e.g. `ALTER TABLE`, `CREATE INDEX`)
4. Test it locally before committing
5. Update `schema.sql` to reflect the new state (so fresh setups stay current)
6. Commit and tell the team in Discord/Slack: "New migration 002, please pull and run"

### Running a migration after someone else creates it

```bash
git pull
```

**PowerShell:**
```powershell
Get-Content database/migrations/002_filename.sql | mysql -u root -p
```

**Command Prompt:**
```cmd
mysql -u root -p < database\migrations\002_filename.sql
```

### Migration naming convention

```
001_add_user_avatar_url.sql
002_add_index_to_email.sql
003_add_workout_difficulty_column.sql
```

Never reuse numbers. If two people create migration 003 at the same time, the second person renames theirs to 004.

---

## Common Issues

**`ERROR 1045 (28000): Access denied for user 'root'@'localhost'`**
Your password is wrong. Double-check the password you set when installing MySQL.

**`ERROR 1049 (42000): Unknown database 'reps_up'`**
You skipped step 2, or `schema.sql` didn't run properly. Re-run it.

**`Can't connect to MySQL server on 'localhost'`**
MySQL isn't running. On Windows, start it from Services. On Mac, run `brew services start mysql`. On Linux, `sudo systemctl start mysql`.

**`.env` file appears in `git status`**
Your `.gitignore` isn't being applied. Check that `.gitignore` exists at the project root and contains `.env` on its own line.

---

## Team Workflow

- **Daily:** Pull before you start (`git pull`). Run any new migrations in `database/migrations/`.
- **Schema changes:** Always create a migration. Never edit existing migrations after they've been pushed.
- **Migration numbering:** Coordinate in Discord — "I'm taking 005 for the streak fix."
- **Before pushing:** Make sure `.env` is **not** in `git status`. If it is, your `.gitignore` is broken.
