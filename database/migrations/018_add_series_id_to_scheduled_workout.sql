-- Migration 018: link repeating scheduled workouts together.
--
-- When a workout is scheduled with a repeat (Weekly, Biweekly, Monthly), the app
-- creates one scheduled_workout row per occurrence. series_id gives every row from
-- the same repeat the same number (the id of the first row), so My Plan can offer
-- "Delete this one" or "Delete all future" when a repeating workout is removed.
--
-- series_id is NULL for workouts that do not repeat and for any repeating workouts
-- scheduled before this migration (those can only be deleted one at a time).
--
-- Run this once on an existing database. A fresh setup already gets the column
-- from schema.sql.

USE reps_up;

ALTER TABLE scheduled_workout ADD COLUMN series_id INT NULL AFTER completed_reps;

CREATE INDEX idx_scheduled_series ON scheduled_workout(series_id);
