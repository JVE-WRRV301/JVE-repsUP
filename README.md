# Reps Up

A gamified mobile fitness application that helps users schedule, track, and execute workouts, with XP, levels, ranks, and avatar customization to keep them motivated.

**Team JVE** - James Ford, Vincent van Niekerk, Emma Martin

---

## Tech Stack

- **Mobile app:** Android, written in **Java** (minSdk 26, compileSdk 36), built with Gradle (Groovy `build.gradle` files) in Android Studio
- **Database:** MySQL, reached directly from the app over JDBC
- **Tests:** JUnit 5 and jqwik (property-based tests), run with Gradle
- **Version control:** Git + GitHub

---

## Quick start

The short version. Each step is explained in detail further down.

1. Clone the repo.
2. Build the database: run `database/schema.sql`, then `database/seed.sql`.
3. Create `android/local.properties` with your database details (`db.host`, `db.port`, `db.user`, `db.password`, `db.name`).
4. Open the `android/` folder in Android Studio and run the app.
5. Log in as `dyllan12345` / `Password1`.

---

## Database Setup

Follow these steps to get the Reps Up database running on your local machine.

### Prerequisites

- MySQL 8 installed and running locally
- Git installed
- A terminal (Command Prompt or PowerShell on Windows, Terminal on Mac/Linux)

### 1. Clone the repository

```bash
git clone https://github.com/JVE-WRRV301/JVE-repsUP.git
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

You'll be prompted for your MySQL root password. This **drops any existing `reps_up` database** and creates a fresh `reps_up` database with all its tables.

### 3. Load the seed data

**PowerShell:**
```powershell
Get-Content database/seed.sql | mysql -u root -p
```

**Command Prompt:**
```cmd
mysql -u root -p < database\seed.sql
```

This populates the database with the ranks, the exercise library, default workouts, cosmetic items (with their coin prices), the admin account, and a few test users.

### 4. Do I need to run the migrations?

**On a fresh database: no.** `schema.sql` and `seed.sql` are kept up to date with every migration, so after steps 2 and 3 your database is already current. Do **not** run the files in `database/migrations/` on top of it.

**On an existing database** (one you built earlier and want to keep your data in): do not re-run `schema.sql`, because that wipes the database. Instead, run only the migration files you have not run yet, **in number order**. See [Schema Changes (Migrations)](#schema-changes-migrations) below. Every migration can safely be run twice, so if you are unsure whether you ran one, run it again.

### 5. Connect the app to the database

The app reads its database details from **`android/local.properties`** at build time. This file is in `.gitignore`, so it is never committed. Create it (or add these lines to the existing file Android Studio made):

```properties
db.host=10.0.2.2
db.port=3306
db.user=repsup_app
db.password=your_password_here
db.name=reps_up
```

- `10.0.2.2` is how the **Android emulator** reaches MySQL running on your own computer. On a **real phone**, use your computer's local network IP address instead (for example `192.168.1.20`), and make sure MySQL accepts connections from the network. For a shared server, use the server's address.
- All five `db.*` keys are required. If one is missing, the Gradle sync stops and tells you which.
- After changing `local.properties`, sync Gradle (**File → Sync Project with Gradle Files**) and run the app again.

> **Use a dedicated MySQL user, not `root`.** For the shared server (and ideally everywhere) create a separate MySQL user that only has the rights the app needs on the `reps_up` database, and put that user's credentials in `android/local.properties`. That way the password built into the APK can't administer the whole server. For example:
>
> ```sql
> CREATE USER 'repsup_app'@'%' IDENTIFIED BY 'a_strong_password';
> GRANT SELECT, INSERT, UPDATE, DELETE ON reps_up.* TO 'repsup_app'@'%';
> FLUSH PRIVILEGES;
> ```
>
> Then set `db.user=repsup_app` and `db.password=a_strong_password`.

### 6. Verify the setup

Log into MySQL:

```bash
mysql -u root -p
```

Then run:

```sql
USE reps_up;
SHOW TABLES;
SELECT COUNT(*) FROM exercise;
SELECT username, current_level FROM user;
```

You should see 16 tables, 193 exercises, and 3 test users (dyllan12345, sarahj, jaskrug). If you do, your setup is working. (Table names are all lowercase and singular, for example `user` and `exercise`.)

---

## Android App Setup

### Prerequisites

- [Android Studio](https://developer.android.com/studio) installed (latest stable version)
- A JDK 11 or newer (the one that comes with Android Studio is fine)
- An Android device or emulator to run the app on
- The database set up and `android/local.properties` created (see above)

### Opening the project

1. Open Android Studio.
2. On the Welcome screen, click **Open**.
3. Navigate to the `android/` folder inside your cloned repo and click **Open**.
4. If prompted to trust the project, click **Trust Project**.
5. Android Studio will run Gradle sync. The first sync takes 5–15 minutes - it downloads SDK components and dependencies. Subsequent opens are fast.
6. When sync finishes, you should see `MainActivity.java` in the editor and "BUILD SUCCESSFUL" in the bottom panel.

### Running the app

1. At the top of the Android Studio window, you'll see a green play button ▶ with a device selector to its left.
2. To use an emulator: click the device selector → **Device Manager** → **Create Device** → pick a Pixel model → pick a system image (download if prompted) → Finish.
3. To use a real phone: enable Developer Options and USB Debugging on the phone, plug it in via USB, and select it from the device dropdown.
4. Click the green play button to build and install the app on your chosen device.

### Running the unit tests

From the `android/` folder:

**PowerShell / Command Prompt:**
```cmd
gradlew.bat test
```

**Mac/Linux:**
```bash
./gradlew test
```

Or in Android Studio: right-click `app/src/test` and choose **Run Tests**.

### Troubleshooting

**Gradle sync fails with "SDK location not found"**
Android Studio can't find your Android SDK. Go to **File → Project Structure → SDK Location** and set the Android SDK path (usually `C:\Users\YourName\AppData\Local\Android\Sdk` on Windows).

**Gradle sync fails with a message about missing `db.*` fields**
`android/local.properties` is missing or incomplete. Add all five `db.*` lines (see [Connect the app to the database](#5-connect-the-app-to-the-database)).

**The app opens but shows database errors or can't log in**
The app can't reach MySQL. Check that MySQL is running, that `db.host` is right for where you are running the app (`10.0.2.2` for the emulator, your computer's IP for a real phone), and that the user in `db.user` can connect.

**"Add files to Git" popup appears**
Always click **Cancel**. Git is handled at the repo level, not by Android Studio.

---

## Logging in

Passwords are stored hashed (PBKDF2), so you can't read them out of the database. The seed data sets these plain passwords for the test accounts:

| Username | Password | Notes |
|---|---|---|
| dyllan12345 | Password1 | Male, level 10, has an outfit and workouts |
| sarahj | Password2 | Female |
| jaskrug | Password3 | |

### Admin side

The admin tools (creating and managing the default workouts and exercises) are separate from the normal login. There are two ways in:

- On the welcome screen, **long-press the "REPS UP." title**. This opens the admin login.
- Or log in as the admin account on that screen:

| Username | Password |
|---|---|
| Admin | admin123 |

These accounts are for local development only.

---

## Project Structure

```
JVE-repsUP/
├── android/                Android Studio project (Java)
│   ├── app/                App source code (src/main) and unit tests (src/test)
│   ├── gradle/             Gradle wrapper
│   ├── build.gradle        Top-level build config (Groovy)
│   └── local.properties    Your database details (you create this; never committed)
├── database/
│   ├── schema.sql          Database structure (tables, foreign keys, indexes)
│   ├── seed.sql            Sample data for development
│   └── migrations/         Numbered schema change files (see below)
├── docs/                   Project documents
├── .gitignore              Files Git should never commit
└── README.md               This file
```

---

## Schema Changes (Migrations)

**Never edit a migration after it has been pushed**, and don't change `schema.sql` on its own. Once everyone has the database running with their own test data, rebuilding it would cost teammates their work.

Instead, schema changes are done through **migration files**. Each change gets its own numbered SQL file in `database/migrations/`. The files so far are `003` to `023`.

### Creating a migration

1. Create a new file in `database/migrations/` named like `024_short_description.sql`
2. Use the next number after the highest one already in the folder
3. Start it with `USE reps_up;` and write only the changes (e.g. `ALTER TABLE`, `CREATE INDEX`). Make it safe to run twice where you can, and filter every `UPDATE`/`DELETE` on a key column (see the note below).
4. Test it locally before committing
5. Update `schema.sql` (and `seed.sql` if the data changed) to reflect the new state, so fresh setups stay current
6. Commit and tell the team: "New migration 024, please pull and run"

### Running a migration after someone else creates it

```bash
git pull
```

**PowerShell:**
```powershell
Get-Content database/migrations/024_filename.sql | mysql -u root -p
```

**Command Prompt:**
```cmd
mysql -u root -p < database\migrations\024_filename.sql
```

Or open the file in MySQL Workbench and run the whole script with the lightning-bolt button.

> **Workbench "safe update mode" error (Error Code 1175):** MySQL Workbench refuses an `UPDATE` or `DELETE` that doesn't filter on a key column. Write migrations so they always do (for example `WHERE exercise_id > 0 AND ...`), and make sure you run the whole file from an up-to-date copy.

Never reuse numbers. If two people create migration 024 at the same time, the second person renames theirs to 025.

---

## Common Issues

**`ERROR 1045 (28000): Access denied for user 'root'@'localhost'`**
Your password is wrong. Double-check the password you set when installing MySQL.

**`ERROR 1049 (42000): Unknown database 'reps_up'`**
You skipped step 2, or `schema.sql` didn't run properly. Re-run it.

**`Can't connect to MySQL server on 'localhost'`**
MySQL isn't running. On Windows, start it from Services. On Mac, run `brew services start mysql`. On Linux, `sudo systemctl start mysql`.

**`local.properties` appears in `git status`**
Your `.gitignore` isn't being applied. Check that `local.properties` is listed in it. Never commit this file, because it holds your database password.

---

## Team Workflow

- **Daily:** Pull before you start (`git pull`). Run any new migrations in `database/migrations/`.
- **Schema changes:** Always create a migration, and keep `schema.sql` and `seed.sql` in step with it.
- **Migration numbering:** Coordinate in Discord — "I'm taking 025 for the new table."
- **Before pushing:** Make sure `android/local.properties` is **not** in `git status`. If it is, your `.gitignore` is broken.
