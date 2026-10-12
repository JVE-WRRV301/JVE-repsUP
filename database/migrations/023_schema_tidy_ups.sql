-- Migration 023: schema tidy-ups
-- 1. Rename user.measurment_system to measurement_system (typo).
-- 2. Drop the unused streak table (the app keeps the streak on user.current_streak).
-- 3. Drop xp_log.amount, which always held the same number as xp_earned.
-- 4. Add coin_transaction.created_at so purchases have a date. Existing rows get the
--    time this migration runs.
-- Safe to run more than once. Run in MySQL Workbench against reps_up.

USE reps_up;

-- 1. Rename the column (only if the old name still exists).
SET @has_old = (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'user'
                  AND column_name = 'measurment_system');
SET @sql = IF(@has_old > 0,
    'ALTER TABLE user CHANGE COLUMN measurment_system measurement_system VARCHAR(10) DEFAULT ''metric''',
    'SELECT ''user.measurement_system is already renamed'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Drop the unused streak table.
DROP TABLE IF EXISTS streak;

-- 3. Drop the duplicate xp_log.amount column (only if it still exists).
SET @has_amount = (SELECT COUNT(*) FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = 'xp_log'
                     AND column_name = 'amount');
SET @sql = IF(@has_amount > 0,
    'ALTER TABLE xp_log DROP COLUMN amount',
    'SELECT ''xp_log.amount is already dropped'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Add coin_transaction.created_at (only if it is missing).
SET @has_created = (SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = 'coin_transaction'
                      AND column_name = 'created_at');
SET @sql = IF(@has_created = 0,
    'ALTER TABLE coin_transaction ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT ''coin_transaction.created_at already exists'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
