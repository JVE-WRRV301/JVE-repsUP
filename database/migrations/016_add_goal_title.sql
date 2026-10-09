-- Migration 016: give goals a proper title column (Reps Up review item 11)
--
-- Previously a goal's title and optional notes were stored together in the
-- single goal_description column, joined by a "\n---\n" separator, and every
-- screen had to split the string. The goal-complete popup and the share text
-- used the whole string, so users saw the "---" separator and their notes.
--
-- This migration adds a dedicated goal_title column and splits each existing
-- goal_description into its title and notes parts:
--   * goal_title       = the text before the first "\n---\n" (or the whole
--                        string when there is no separator)
--   * goal_description = the text after the first "\n---\n" (or NULL when there
--                        is no separator, i.e. the goal had no notes)
--
-- It is written to be safe to run more than once: the column is only added when
-- it does not already exist, and the split only runs on rows that still contain
-- the old separator.

USE reps_up;

-- ─── Add the goal_title column if it isn't already present ───
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'goal'
      AND column_name = 'goal_title'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE goal ADD COLUMN goal_title VARCHAR(255) NOT NULL DEFAULT '''' AFTER user_id',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ─── Allow goal_description to be NULL ───
-- It was TEXT NOT NULL in the old schema, but it now holds the optional notes,
-- which are NULL for a goal with no notes. Make it nullable before the UPDATEs
-- below (and before the app saves NULL notes) so strict mode doesn't reject them.
ALTER TABLE goal MODIFY COLUMN goal_description TEXT NULL;

-- ─── Backfill title and notes from the combined goal_description ───
-- Rows that contain the old "\n---\n" separator: title is the part before it,
-- notes the part after. SUBSTRING_INDEX with a positive/negative count returns
-- everything before/after the first occurrence of the separator.
UPDATE goal
SET goal_title = SUBSTRING_INDEX(goal_description, '\n---\n', 1),
    goal_description = SUBSTRING_INDEX(goal_description, '\n---\n', -1)
WHERE goal_description LIKE CONCAT('%', '\n---\n', '%');

-- Rows with no separator: the whole description was the title and there are no
-- notes. Only touch rows that haven't been given a title yet (goal_title still
-- blank from the DEFAULT) so re-running the migration is harmless.
UPDATE goal
SET goal_title = goal_description,
    goal_description = NULL
WHERE (goal_title IS NULL OR goal_title = '')
  AND goal_description NOT LIKE CONCAT('%', '\n---\n', '%');

-- ─── Drop the temporary DEFAULT now the column is populated ───
-- The schema.sql definition has no default; new inserts always supply a title.
ALTER TABLE goal ALTER COLUMN goal_title DROP DEFAULT;
