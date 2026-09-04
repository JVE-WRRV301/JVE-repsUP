-- Add target_weight column to workout_exercise table
-- Stores the user's desired weight for the exercise (set during workout creation/editing)
-- Defaults to 0.00 for existing rows (shows empty weight field during execution)
ALTER TABLE workout_exercise ADD COLUMN target_weight DECIMAL(6,2) NOT NULL DEFAULT 0.00 AFTER workout_exercise_order_index;
