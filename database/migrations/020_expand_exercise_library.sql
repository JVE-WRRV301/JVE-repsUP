-- Item 17: bigger exercise library, and Legs split into Quads and Hamstrings.
--
-- 1. Legs becomes Quads / Hamstrings. Bulgarian Split Squats and Walking Lunges
--    (quad-dominant) move from Glutes to Quads, so every squat/lunge sits with
--    the leg muscles it works.
-- 2. "Cable Tricep Pushdown" was a duplicate of "Tricep Pushdown". Row 7 keeps its
--    id (workouts may use it) but becomes a different exercise.
-- 3. New exercises are added at the end. Re-running this file adds nothing twice.

UPDATE exercise SET muscle_group = 'Hamstrings'
WHERE exercise_id > 0 AND exercise_name IN ('Barbell RDLs', 'Hamstring Extension');

UPDATE exercise SET muscle_group = 'Quads'
WHERE exercise_id > 0 AND exercise_name IN ('Squats', 'Leg Press', 'Bulgarian Split Squats', 'Walking Lunges');

-- Anything still called Legs (e.g. added by an admin) goes to Quads.
UPDATE exercise SET muscle_group = 'Quads' WHERE exercise_id > 0 AND muscle_group = 'Legs';

UPDATE exercise
SET exercise_name = 'Overhead Tricep Extension', difficulty = 'medium', base_xp = 25
WHERE exercise_id > 0 AND exercise_name = 'Cable Tricep Pushdown';

INSERT INTO exercise (exercise_name, muscle_group, default_sets, default_reps, difficulty, base_xp)
SELECT v.exercise_name, v.muscle_group, v.default_sets, v.default_reps, v.difficulty, v.base_xp FROM (
    SELECT 'Incline Dumbbell Press' AS exercise_name, 'Chest' AS muscle_group, 3 AS default_sets, 10 AS default_reps, 'medium' AS difficulty, 25 AS base_xp
    UNION ALL SELECT 'Incline Barbell Press', 'Chest', 3, 8, 'hard', 35
    UNION ALL SELECT 'Decline Barbell Press', 'Chest', 3, 8, 'hard', 35
    UNION ALL SELECT 'Decline Dumbbell Press', 'Chest', 3, 10, 'medium', 25
    UNION ALL SELECT 'Dumbbell Flys', 'Chest', 3, 12, 'easy', 15
    UNION ALL SELECT 'Incline Dumbbell Flys', 'Chest', 3, 12, 'medium', 25
    UNION ALL SELECT 'Cable Crossover', 'Chest', 3, 12, 'medium', 25
    UNION ALL SELECT 'Low-to-High Cable Fly', 'Chest', 3, 12, 'medium', 25
    UNION ALL SELECT 'Pec Deck Fly', 'Chest', 3, 12, 'easy', 15
    UNION ALL SELECT 'Machine Chest Press', 'Chest', 3, 12, 'easy', 15
    UNION ALL SELECT 'Smith Machine Incline Press', 'Chest', 3, 10, 'medium', 25
    UNION ALL SELECT 'Incline Push Ups', 'Chest', 3, 12, 'easy', 15
    UNION ALL SELECT 'Decline Push Ups', 'Chest', 3, 12, 'hard', 35
    UNION ALL SELECT 'Wide Push Ups', 'Chest', 3, 15, 'easy', 15
    UNION ALL SELECT 'Chest Dips', 'Chest', 3, 10, 'hard', 35
    UNION ALL SELECT 'Svend Press', 'Chest', 3, 12, 'easy', 15
    UNION ALL SELECT 'Dumbbell Pullover', 'Chest', 3, 12, 'medium', 25
    UNION ALL SELECT 'Bent-Over Barbell Row', 'Back', 3, 10, 'hard', 35
    UNION ALL SELECT 'One-Arm Dumbbell Row', 'Back', 3, 10, 'medium', 25
    UNION ALL SELECT 'Deadlift', 'Back', 3, 6, 'hard', 35
    UNION ALL SELECT 'Face Pulls', 'Back', 3, 15, 'easy', 15
    UNION ALL SELECT 'Back Extensions', 'Back', 3, 12, 'easy', 15
    UNION ALL SELECT 'T-Bar Row', 'Back', 3, 10, 'medium', 25
    UNION ALL SELECT 'Machine Row', 'Back', 3, 12, 'easy', 15
    UNION ALL SELECT 'Chest-Supported Row', 'Back', 3, 12, 'medium', 25
    UNION ALL SELECT 'Inverted Row', 'Back', 3, 10, 'medium', 25
    UNION ALL SELECT 'Straight-Arm Pulldown', 'Back', 3, 12, 'easy', 15
    UNION ALL SELECT 'Close-Grip Lat Pulldown', 'Back', 3, 10, 'medium', 25
    UNION ALL SELECT 'Reverse-Grip Lat Pulldown', 'Back', 3, 10, 'medium', 25
    UNION ALL SELECT 'Wide-Grip Pull Ups', 'Back', 3, 8, 'hard', 35
    UNION ALL SELECT 'Neutral-Grip Pull Ups', 'Back', 3, 8, 'hard', 35
    UNION ALL SELECT 'Rack Pulls', 'Back', 3, 6, 'hard', 35
    UNION ALL SELECT 'Pendlay Row', 'Back', 3, 8, 'hard', 35
    UNION ALL SELECT 'Barbell Shrugs', 'Back', 3, 12, 'easy', 15
    UNION ALL SELECT 'Barbell Overhead Press', 'Shoulders', 3, 8, 'hard', 35
    UNION ALL SELECT 'Arnold Press', 'Shoulders', 3, 10, 'medium', 25
    UNION ALL SELECT 'Front Raises', 'Shoulders', 3, 12, 'easy', 15
    UNION ALL SELECT 'Rear Delt Fly', 'Shoulders', 3, 12, 'medium', 25
    UNION ALL SELECT 'Upright Row', 'Shoulders', 3, 12, 'medium', 25
    UNION ALL SELECT 'Dumbbell Shrugs', 'Shoulders', 3, 15, 'easy', 15
    UNION ALL SELECT 'Seated Dumbbell Overhead Press', 'Shoulders', 3, 10, 'medium', 25
    UNION ALL SELECT 'Machine Overhead Press', 'Shoulders', 3, 12, 'easy', 15
    UNION ALL SELECT 'Landmine Press', 'Shoulders', 3, 10, 'medium', 25
    UNION ALL SELECT 'Push Press', 'Shoulders', 3, 8, 'hard', 35
    UNION ALL SELECT 'Cable Rear Delt Fly', 'Shoulders', 3, 12, 'medium', 25
    UNION ALL SELECT 'Reverse Pec Deck', 'Shoulders', 3, 12, 'easy', 15
    UNION ALL SELECT 'Plate Front Raises', 'Shoulders', 3, 12, 'easy', 15
    UNION ALL SELECT 'Cable Front Raises', 'Shoulders', 3, 12, 'easy', 15
    UNION ALL SELECT 'Cable Side Raises', 'Shoulders', 3, 12, 'medium', 25
    UNION ALL SELECT 'Cable Upright Row', 'Shoulders', 3, 12, 'medium', 25
    UNION ALL SELECT 'Pike Push Ups', 'Shoulders', 3, 10, 'hard', 35
    UNION ALL SELECT 'Band Pull-Aparts', 'Shoulders', 3, 15, 'easy', 15
    UNION ALL SELECT 'Skull Crushers', 'Triceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Tricep Dips', 'Triceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Close-Grip Press', 'Triceps', 3, 8, 'hard', 35
    UNION ALL SELECT 'Tricep Kickbacks', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Diamond Push Ups', 'Triceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Rope Pushdown', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Straight-Bar Pushdown', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Reverse-Grip Pushdown', 'Triceps', 3, 12, 'medium', 25
    UNION ALL SELECT 'Single-Arm Cable Pushdown', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Overhead Rope Extension', 'Triceps', 3, 12, 'medium', 25
    UNION ALL SELECT 'Dumbbell Overhead Extension', 'Triceps', 3, 12, 'medium', 25
    UNION ALL SELECT 'Seated EZ-Bar Extension', 'Triceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Bench Dips', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Cable Skull Crushers', 'Triceps', 3, 12, 'medium', 25
    UNION ALL SELECT 'Dumbbell Skull Crushers', 'Triceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Close-Grip Push Ups', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Machine Dips', 'Triceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'JM Press', 'Triceps', 3, 8, 'hard', 35
    UNION ALL SELECT 'Barbell Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Concentration Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Incline Dumbbell Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Preacher Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Cable Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Chin Ups', 'Biceps', 3, 8, 'hard', 35
    UNION ALL SELECT 'EZ-Bar Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Spider Curls', 'Biceps', 3, 12, 'medium', 25
    UNION ALL SELECT 'Drag Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Machine Preacher Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Dumbbell Preacher Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Alternating Dumbbell Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Seated Dumbbell Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Rope Hammer Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Cross-Body Hammer Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Bayesian Curls', 'Biceps', 3, 12, 'medium', 25
    UNION ALL SELECT 'Wide-Grip Barbell Curls', 'Biceps', 3, 10, 'medium', 25
    UNION ALL SELECT 'Single-Arm Cable Curls', 'Biceps', 3, 12, 'easy', 15
    UNION ALL SELECT 'Goblet Squats', 'Quads', 3, 12, 'easy', 15
    UNION ALL SELECT 'Front Squats', 'Quads', 3, 8, 'hard', 35
    UNION ALL SELECT 'Leg Extensions', 'Quads', 3, 12, 'easy', 15
    UNION ALL SELECT 'Step Ups', 'Quads', 3, 10, 'medium', 25
    UNION ALL SELECT 'Reverse Lunges', 'Quads', 3, 12, 'easy', 15
    UNION ALL SELECT 'Hack Squat', 'Quads', 3, 10, 'medium', 25
    UNION ALL SELECT 'Smith Machine Squats', 'Quads', 3, 10, 'medium', 25
    UNION ALL SELECT 'Sissy Squats', 'Quads', 3, 12, 'hard', 35
    UNION ALL SELECT 'Box Squats', 'Quads', 3, 8, 'hard', 35
    UNION ALL SELECT 'Pause Squats', 'Quads', 3, 6, 'hard', 35
    UNION ALL SELECT 'Lateral Lunges', 'Quads', 3, 10, 'medium', 25
    UNION ALL SELECT 'Split Squats', 'Quads', 3, 10, 'medium', 25
    UNION ALL SELECT 'Single-Leg Press', 'Quads', 3, 10, 'medium', 25
    UNION ALL SELECT 'Assisted Pistol Squats', 'Quads', 3, 8, 'hard', 35
    UNION ALL SELECT 'Jump Squats', 'Quads', 3, 12, 'medium', 25
    UNION ALL SELECT 'Dumbbell Squats', 'Quads', 3, 12, 'easy', 15
    UNION ALL SELECT 'Lying Leg Curls', 'Hamstrings', 3, 12, 'medium', 25
    UNION ALL SELECT 'Seated Leg Curls', 'Hamstrings', 3, 12, 'easy', 15
    UNION ALL SELECT 'Dumbbell RDLs', 'Hamstrings', 3, 12, 'easy', 15
    UNION ALL SELECT 'Single-Leg RDLs', 'Hamstrings', 3, 10, 'medium', 25
    UNION ALL SELECT 'Good Mornings', 'Hamstrings', 3, 10, 'hard', 35
    UNION ALL SELECT 'Nordic Hamstring Curls', 'Hamstrings', 3, 6, 'hard', 35
    UNION ALL SELECT 'Stiff-Leg Deadlift', 'Hamstrings', 3, 8, 'hard', 35
    UNION ALL SELECT 'Standing Leg Curls', 'Hamstrings', 3, 12, 'easy', 15
    UNION ALL SELECT 'Cable Leg Curls', 'Hamstrings', 3, 12, 'easy', 15
    UNION ALL SELECT 'Swiss Ball Leg Curls', 'Hamstrings', 3, 12, 'medium', 25
    UNION ALL SELECT 'Slider Leg Curls', 'Hamstrings', 3, 10, 'medium', 25
    UNION ALL SELECT 'Glute-Ham Raises', 'Hamstrings', 3, 8, 'hard', 35
    UNION ALL SELECT 'Sumo Deadlift', 'Hamstrings', 3, 6, 'hard', 35
    UNION ALL SELECT 'Smith Machine RDLs', 'Hamstrings', 3, 10, 'medium', 25
    UNION ALL SELECT 'Kettlebell Swings', 'Hamstrings', 3, 15, 'medium', 25
    UNION ALL SELECT 'Hamstring Walkouts', 'Hamstrings', 3, 8, 'medium', 25
    UNION ALL SELECT 'Single-Leg Hamstring Bridge', 'Hamstrings', 3, 12, 'medium', 25
    UNION ALL SELECT 'Landmine RDLs', 'Hamstrings', 3, 10, 'medium', 25
    UNION ALL SELECT 'Glute Bridges', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Donkey Kicks', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Cable Pull-Throughs', 'Glutes', 3, 12, 'medium', 25
    UNION ALL SELECT 'Single-Leg Hip Thrusts', 'Glutes', 3, 10, 'hard', 35
    UNION ALL SELECT 'Curtsy Lunges', 'Glutes', 3, 10, 'medium', 25
    UNION ALL SELECT 'Dumbbell Hip Thrusts', 'Glutes', 3, 12, 'easy', 15
    UNION ALL SELECT 'Smith Machine Hip Thrusts', 'Glutes', 3, 10, 'medium', 25
    UNION ALL SELECT 'Banded Lateral Walks', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Hip Abduction Machine', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Cable Hip Abduction', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Single-Leg Glute Bridges', 'Glutes', 3, 12, 'medium', 25
    UNION ALL SELECT 'Frog Pumps', 'Glutes', 3, 20, 'easy', 15
    UNION ALL SELECT 'Banded Clamshells', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Banded Glute Kickbacks', 'Glutes', 3, 15, 'easy', 15
    UNION ALL SELECT 'Dumbbell Sumo Deadlift', 'Glutes', 3, 10, 'medium', 25
    UNION ALL SELECT 'Elevated Glute Bridges', 'Glutes', 3, 12, 'medium', 25
    UNION ALL SELECT 'Leg Raises', 'Abs', 3, 12, 'medium', 25
    UNION ALL SELECT 'Russian Twists', 'Abs', 3, 20, 'medium', 25
    UNION ALL SELECT 'Bicycle Crunches', 'Abs', 3, 20, 'easy', 15
    UNION ALL SELECT 'Mountain Climbers', 'Abs', 3, 20, 'medium', 25
    UNION ALL SELECT 'Dead Bug', 'Abs', 3, 12, 'easy', 15
    UNION ALL SELECT 'Hanging Knee Raises', 'Abs', 3, 10, 'hard', 35
    UNION ALL SELECT 'Ab Wheel Rollouts', 'Abs', 3, 8, 'hard', 35
    UNION ALL SELECT 'Cable Crunches', 'Abs', 3, 15, 'medium', 25
    UNION ALL SELECT 'Hanging Leg Raises', 'Abs', 3, 10, 'hard', 35
    UNION ALL SELECT 'V-Ups', 'Abs', 3, 12, 'medium', 25
    UNION ALL SELECT 'Reverse Crunches', 'Abs', 3, 15, 'easy', 15
    UNION ALL SELECT 'Flutter Kicks', 'Abs', 3, 20, 'easy', 15
    UNION ALL SELECT 'Toe Touches', 'Abs', 3, 15, 'easy', 15
    UNION ALL SELECT 'Sit Ups', 'Abs', 3, 15, 'easy', 15
    UNION ALL SELECT 'Decline Sit Ups', 'Abs', 3, 12, 'medium', 25
    UNION ALL SELECT 'Oblique Crunches', 'Abs', 3, 15, 'easy', 15
    UNION ALL SELECT 'Cable Woodchoppers', 'Abs', 3, 12, 'medium', 25
    UNION ALL SELECT 'Heel Taps', 'Abs', 3, 20, 'easy', 15
    UNION ALL SELECT 'Donkey Calf Raises', 'Calves', 3, 15, 'medium', 25
    UNION ALL SELECT 'Single-Leg Calf Raises', 'Calves', 3, 12, 'medium', 25
    UNION ALL SELECT 'Leg Press Calf Raises', 'Calves', 3, 15, 'easy', 15
    UNION ALL SELECT 'Smith Machine Calf Raises', 'Calves', 3, 15, 'easy', 15
    UNION ALL SELECT 'Reverse Curls', 'Forearms', 3, 12, 'easy', 15
    UNION ALL SELECT 'Reverse Wrist Curls', 'Forearms', 3, 15, 'easy', 15
    UNION ALL SELECT 'Zottman Curls', 'Forearms', 3, 10, 'medium', 25
    UNION ALL SELECT 'Behind-the-Back Wrist Curls', 'Forearms', 3, 15, 'easy', 15
    UNION ALL SELECT 'Wrist Roller', 'Forearms', 3, 8, 'medium', 25
) AS v
WHERE NOT EXISTS (SELECT 1 FROM exercise e WHERE e.exercise_name = v.exercise_name);
