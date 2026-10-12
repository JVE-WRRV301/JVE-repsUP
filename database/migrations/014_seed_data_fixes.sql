-- Migration 014: seed-data fixes (Reps Up review items 9, 10 and 18)
--
-- These are corrections to the DEVELOPMENT seed data. A fresh setup already
-- gets the right data from seed.sql; this migration brings an EXISTING database
-- that was built from the old seed.sql up to the same state. It is written to
-- be safe to run against the seeded demo data and does nothing useful on a
-- database with no seed data in it.
--
-- This migration never hard-codes id numbers. Cosmetics are resolved by
-- cosmetic_name + gender, exercises by exercise_name and workouts by
-- workout_name, all via subqueries. Every INSERT is a plain INSERT guarded by
-- NOT EXISTS: if a referenced row is missing the subquery yields NULL and the
-- INSERT fails loudly (NOT NULL / FK violation) instead of being skipped
-- silently, so a broken database surfaces an error.
--
-- Item 9: the default workouts Pull Day, Leg Day and Core Burner, and Sarah's
--         "Beginner Full Body", had no workout_exercise rows, so they couldn't
--         be started. Add sensible exercises using existing exercises.
-- Item 10: goal_priority must be exactly 'Top'/'Medium'/'Low' (the values the
--          app saves and matches on). The old seed used 'top'/'medium'.
-- Item 18: Dylan owned the wrong cosmetics (the female Default Hair and the
--          male White T-Shirt); give him genuine male items instead. Also move
--          the test scheduled-workout and goal dates to be relative to CURDATE().

USE reps_up;

-- ─── Item 9: add exercises to the empty workouts ───
-- Each row looks up its workout by name and its exercise by name, and only
-- inserts when that (workout, exercise) pairing does not already exist.

-- Pull Day (default)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index)
SELECT w.workout_id, e.exercise_id, v.num_sets, v.num_reps, v.order_index
FROM (
    SELECT 'Lat Pulldown' AS exercise_name, 3 AS num_sets, 10 AS num_reps, 1 AS order_index UNION ALL
    SELECT 'Seated Row',   3, 10, 2 UNION ALL
    SELECT 'Pull Ups',     3, 8,  3 UNION ALL
    SELECT 'Bicep Curls',  3, 12, 4 UNION ALL
    SELECT 'Hammer Curls', 3, 12, 5
) AS v
JOIN workout w  ON w.workout_name = 'Pull Day' AND w.is_default = 1
JOIN exercise e ON e.exercise_name = v.exercise_name
WHERE NOT EXISTS (
    SELECT 1 FROM workout_exercise we
    WHERE we.workout_id = w.workout_id AND we.exercise_id = e.exercise_id
);

-- Leg Day (default)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index)
SELECT w.workout_id, e.exercise_id, v.num_sets, v.num_reps, v.order_index
FROM (
    SELECT 'Squats' AS exercise_name,       3 AS num_sets, 10 AS num_reps, 1 AS order_index UNION ALL
    SELECT 'Leg Press',             3, 12, 2 UNION ALL
    SELECT 'Barbell RDLs',          3, 10, 3 UNION ALL
    SELECT 'Walking Lunges',        3, 12, 4 UNION ALL
    SELECT 'Standing Calf Raises',  3, 15, 5
) AS v
JOIN workout w  ON w.workout_name = 'Leg Day' AND w.is_default = 1
JOIN exercise e ON e.exercise_name = v.exercise_name
WHERE NOT EXISTS (
    SELECT 1 FROM workout_exercise we
    WHERE we.workout_id = w.workout_id AND we.exercise_id = e.exercise_id
);

-- Core Burner (default)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index)
SELECT w.workout_id, e.exercise_id, v.num_sets, v.num_reps, v.order_index
FROM (
    SELECT 'Plank' AS exercise_name, 3 AS num_sets, 30 AS num_reps, 1 AS order_index UNION ALL
    SELECT 'Crunches',      3, 20, 2 UNION ALL
    SELECT 'Fire Hydrants', 3, 15, 3
) AS v
JOIN workout w  ON w.workout_name = 'Core Burner' AND w.is_default = 1
JOIN exercise e ON e.exercise_name = v.exercise_name
WHERE NOT EXISTS (
    SELECT 1 FROM workout_exercise we
    WHERE we.workout_id = w.workout_id AND we.exercise_id = e.exercise_id
);

-- Beginner Full Body (Sarah's user workout)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index)
SELECT w.workout_id, e.exercise_id, v.num_sets, v.num_reps, v.order_index
FROM (
    SELECT 'Push Ups' AS exercise_name, 3 AS num_sets, 15 AS num_reps, 1 AS order_index UNION ALL
    SELECT 'Squats',                  3, 10, 2 UNION ALL
    SELECT 'Lat Pulldown',            3, 10, 3 UNION ALL
    SELECT 'Dumbbell Shoulder Press', 3, 12, 4 UNION ALL
    SELECT 'Crunches',                3, 20, 5
) AS v
JOIN workout w  ON w.workout_name = 'Beginner Full Body'
JOIN exercise e ON e.exercise_name = v.exercise_name
WHERE NOT EXISTS (
    SELECT 1 FROM workout_exercise we
    WHERE we.workout_id = w.workout_id AND we.exercise_id = e.exercise_id
);

-- ─── Item 10: normalise seed goal priorities to the exact casing the app uses ───
UPDATE goal SET goal_priority = 'Top'    WHERE goal_priority = 'top';
UPDATE goal SET goal_priority = 'Medium' WHERE goal_priority = 'medium';
UPDATE goal SET goal_priority = 'Low'    WHERE goal_priority = 'low';

-- ─── Item 18: fix Dylan's owned cosmetics ───
-- The old seed gave Dylan (first_name 'Dylan Woods') the female Default Hair
-- and the male White T-Shirt. Remove those two, then give him genuine male
-- items: White Sneakers and Grey Zip Hoodie. All rows are resolved by name
-- (+ gender for cosmetics) so no id numbers are relied on.

DELETE uc FROM user_cosmetic uc
JOIN user u ON u.user_id = uc.user_id AND u.first_name = 'Dylan Woods'
JOIN cosmetic_item c ON c.cosmetic_id = uc.cosmetic_id
WHERE (c.cosmetic_name = 'Default Hair'  AND c.gender = 'female')
   OR (c.cosmetic_name = 'White T-Shirt' AND c.gender = 'male');

-- White Sneakers (male)
INSERT INTO user_cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped)
SELECT u.user_id, c.cosmetic_id, TRUE, FALSE
FROM user u
JOIN cosmetic_item c ON c.cosmetic_name = 'White Sneakers' AND c.gender = 'male'
WHERE u.first_name = 'Dylan Woods'
  AND NOT EXISTS (
      SELECT 1 FROM user_cosmetic uc
      WHERE uc.user_id = u.user_id AND uc.cosmetic_id = c.cosmetic_id
  );

-- Grey Zip Hoodie (male)
INSERT INTO user_cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped)
SELECT u.user_id, c.cosmetic_id, TRUE, FALSE
FROM user u
JOIN cosmetic_item c ON c.cosmetic_name = 'Grey Zip Hoodie' AND c.gender = 'male'
WHERE u.first_name = 'Dylan Woods'
  AND NOT EXISTS (
      SELECT 1 FROM user_cosmetic uc
      WHERE uc.user_id = u.user_id AND uc.cosmetic_id = c.cosmetic_id
  );

-- ─── Item 18: move the test scheduled workouts to be relative to CURDATE() ───
-- One completed yesterday, one today and one later this week. Workouts are
-- matched by name (via a subquery) instead of by id. These were Dylan's three
-- fixed-date September 2026 rows.
UPDATE scheduled_workout sw
JOIN user u ON u.user_id = sw.user_id AND u.first_name = 'Dylan Woods'
SET sw.scheduled_date = DATE_ADD(CURDATE(), INTERVAL -1 DAY)
WHERE sw.workout_id = (SELECT workout_id FROM workout WHERE workout_name = 'Glutes and Hamstrings')
  AND sw.scheduled_date = '2026-09-02';

UPDATE scheduled_workout sw
JOIN user u ON u.user_id = sw.user_id AND u.first_name = 'Dylan Woods'
SET sw.scheduled_date = CURDATE()
WHERE sw.workout_id = (SELECT workout_id FROM workout WHERE workout_name = 'Chest, Shoulders and Triceps')
  AND sw.scheduled_date = '2026-09-04';

UPDATE scheduled_workout sw
JOIN user u ON u.user_id = sw.user_id AND u.first_name = 'Dylan Woods'
SET sw.scheduled_date = DATE_ADD(CURDATE(), INTERVAL 3 DAY)
WHERE sw.workout_id = (SELECT workout_id FROM workout WHERE workout_name = 'Chest, Shoulders and Triceps')
  AND sw.scheduled_date = '2026-09-11';

-- ─── Item 18: move the test goal deadlines to be relative to CURDATE() ───
-- So they stay in the future for a demo.
UPDATE goal g
JOIN user u ON u.user_id = g.user_id AND u.first_name = 'Dylan Woods'
SET g.deadline = DATE_ADD(CURDATE(), INTERVAL 14 DAY)
WHERE g.deadline = '2026-09-15';

UPDATE goal g
JOIN user u ON u.user_id = g.user_id AND u.first_name = 'Dylan Woods'
SET g.deadline = DATE_ADD(CURDATE(), INTERVAL 30 DAY)
WHERE g.deadline = '2026-09-30';

UPDATE goal g
JOIN user u ON u.user_id = g.user_id AND u.first_name = 'Sarah Fisher'
SET g.deadline = DATE_ADD(CURDATE(), INTERVAL 21 DAY)
WHERE g.deadline = '2026-10-01';
