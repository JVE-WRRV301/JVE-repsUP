-- Migration 021: exercise_log
-- Stores what a user actually did for each exercise when a workout is completed
-- (sets, total reps and the heaviest weight used). The newest row per exercise
-- is the "last weight" shown in the library and pre-filled in the next workout;
-- rows linked to a scheduled workout feed the history on past calendar days.
-- Safe to run more than once. Run in MySQL Workbench against reps_up.

USE reps_up;

CREATE TABLE IF NOT EXISTS exercise_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    exercise_id INT NOT NULL,
    scheduled_workout_id INT NULL,
    sets_completed INT NOT NULL DEFAULT 0,
    total_reps INT NOT NULL DEFAULT 0,
    weight_kg DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exercise_log_user_exercise (user_id, exercise_id),
    INDEX idx_exercise_log_scheduled (scheduled_workout_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercise(exercise_id) ON DELETE CASCADE,
    FOREIGN KEY (scheduled_workout_id) REFERENCES scheduled_workout(scheduled_workout_id) ON DELETE SET NULL
);

-- A few sample logs for the demo account (only when the table is empty).
INSERT INTO exercise_log (user_id, exercise_id, scheduled_workout_id, sets_completed, total_reps, weight_kg, logged_at)
SELECT sw.user_id, v.exercise_id, sw.scheduled_workout_id, 3, v.total_reps, v.weight_kg, TIMESTAMP(sw.scheduled_date)
FROM scheduled_workout sw
JOIN user u ON u.user_id = sw.user_id AND u.username = 'dyllan12345'
JOIN (
    SELECT 12 AS exercise_id, 30 AS total_reps, 40.00 AS weight_kg   -- Barbell Hip Thrusts
    UNION ALL SELECT 18, 30, 35.00                                    -- Barbell RDLs
    UNION ALL SELECT 19, 36, 25.00                                    -- Hamstring Extension
) AS v
WHERE sw.workout_id = 7
  AND sw.is_completed = TRUE
  AND sw.scheduled_workout_id = (SELECT MIN(s2.scheduled_workout_id) FROM scheduled_workout s2
                                 WHERE s2.user_id = sw.user_id AND s2.workout_id = 7 AND s2.is_completed = TRUE)
  AND NOT EXISTS (SELECT 1 FROM exercise_log);
