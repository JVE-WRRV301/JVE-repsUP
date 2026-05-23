# Reps Up

A gamified mobile fitness application that helps users schedule, track, and execute workouts, with XP, levels, ranks, and avatar customization to keep them motivated.

**Team JVE** — James Ford, Vincent van Niekerk, Emma Martin

---

## Tech Stack

- **Mobile app:** Android (Kotlin), built in Android Studio
- **Database:** MySQL
- **Version control:** Git + GitHub

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

```bash
Get-Content database/schema.sql | mysql -u root -p
```

You'll be prompted for your MySQL root password. After entering it, this creates the `reps_up` database and all its tables.

### 3. Load the seed data

```bash
Get-Content database/seed.sql | mysql -u root -p
```

This populates the database with default ranks, the exercise library, default workouts, cosmetic items, and a few test users.

### 4. Set up your local environment file

```bash
cp .env.example .env
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
SELECT username, level FROM users;
```

You should see 16 tables, 23 exercises, and 3 test users (Dylan, Sarah, Mike). If you do, your setup is working.

---

## Project Structure

```
JVE-repsUP/
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

**Do not edit `schema.sql` after initial setup.** Once everyone has the database running with their own test data, editing the schema directly would force teammates to drop and rebuild — losing their work.

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
Get-Content database/migrations/002_filename.sql | mysql -u root -p
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
