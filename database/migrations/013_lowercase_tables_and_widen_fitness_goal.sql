-- Migration 013: lowercase all table names + widen user_profile.fitness_goal
--
-- Reps Up review items 8 and 4.
--
-- Item 8: the shared production MySQL runs on Linux, where table names are
-- case-sensitive. The schema originally created MixedCase tables (User,
-- Workout, ...) while most Java queries used lowercase, so every screen would
-- fail once the app points at the Linux server. This migration renames all 16
-- tables to lowercase so the (now consistently lowercase) Java SQL matches.
--
-- NOTE: on a case-INSENSITIVE server (typical Windows MySQL, where
-- lower_case_table_names is 1 or 2) 'User' and 'user' are already the same
-- table, so these RENAME statements are effectively no-ops / already-applied
-- and can be ignored. On the shared Linux server they are required to line the
-- physical table names up with the lowercase names in the code and in schema.sql.

USE reps_up;

-- Rename each table to its lowercase form. Plain RENAME on distinct names is
-- safe in any order on Linux; harmless where the names are already equal.
RENAME TABLE Ranks TO ranks;
RENAME TABLE Cosmetic_Item TO cosmetic_item;
RENAME TABLE Exercise TO exercise;
RENAME TABLE Admins TO admins;
RENAME TABLE User TO user;
RENAME TABLE Avatar TO avatar;
RENAME TABLE User_Profile TO user_profile;
RENAME TABLE User_Cosmetic TO user_cosmetic;
RENAME TABLE Outfit_Slot TO outfit_slot;
RENAME TABLE Workout TO workout;
RENAME TABLE Workout_Exercise TO workout_exercise;
RENAME TABLE Scheduled_Workout TO scheduled_workout;
RENAME TABLE Goal TO goal;
RENAME TABLE XP_Log TO xp_log;
RENAME TABLE Coin_Transaction TO coin_transaction;
RENAME TABLE Streak TO streak;

-- Item 4: user_profile.fitness_goal was VARCHAR(50). The assessment stores the
-- selected goals as a comma-joined string, so choosing 4+ goals overflowed the
-- column and sign-up failed. Widen it to VARCHAR(255).
ALTER TABLE user_profile MODIFY COLUMN fitness_goal VARCHAR(255);
