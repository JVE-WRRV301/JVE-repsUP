-- Migration 019: record when a goal was created.
--
-- Goal XP rule: a goal can only be completed once it is at least 24 hours old
-- (and a user can complete at most 3 goals per day). That needs to know when each
-- goal was created, so goal gets a created_at column.
--
-- Goals that already exist are given a created date 3 days in the past so they can
-- be completed straight away; only goals created from now on wait the 24 hours.
--
-- (The WHERE clause lets the UPDATE run even with MySQL Workbench "safe updates" on.)
--
-- Run this once on an existing database. A fresh setup already gets the column
-- from schema.sql.

USE reps_up;

ALTER TABLE goal ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER completed_at;

UPDATE goal SET created_at = DATE_SUB(NOW(), INTERVAL 3 DAY) WHERE goal_id > 0;
