-- REPS UP - Seed Data


USE reps_up;


-- RANKS (required) 

INSERT INTO ranks (tier, subtier, min_lvl_required) VALUES
    ('Bronze',   'I',   1),
    ('Bronze',   'II',  5),
    ('Bronze',   'III', 10),
    ('Silver',   'I',   15),
    ('Silver',   'II',  20),
    ('Silver',   'III', 25),
    ('Gold',     'I',   30),
    ('Gold',     'II',  35),
    ('Gold',     'III', 40),
    ('Platinum', 'I',   45),
    ('Platinum', 'II',  50),
    ('Platinum', 'III', 55),
    ('Diamond',  'I',   60),
    ('Diamond',  'II',  70),
    ('Diamond',  'III', 80);


-- EXERCISES (required) 

INSERT INTO exercise (exercise_name, muscle_group, default_sets, default_reps, difficulty, base_xp) VALUES
    -- Chest
    -- Muscle group names match utils/MuscleGroups.java (Chest, Back, Shoulders,
    -- Biceps, Triceps, Quads, Hamstrings, Glutes, Abs, Calves, Forearms).
    --
    -- IMPORTANT: exercise IDs 1-23 below must keep their original order, the
    -- seed Workout_Exercise rows reference these exercises by numeric id. Any
    -- NEW exercises are appended at the end (ids 24+) so existing ids don't shift.
    --
    -- Chest (ids 1-3)
    ('Bench Press',           'Chest',       3, 10, 'medium', 30),
    ('Dumbbell Chest Press',  'Chest',       3, 12, 'medium', 25),
    ('Push Ups',              'Chest',       3, 15, 'easy',   15),
    -- Shoulders (ids 4-5)
    ('Dumbbell Shoulder Press', 'Shoulders', 3, 12, 'medium', 25),
    ('Dumbbell Lateral Raises', 'Shoulders', 3, 12, 'easy',   20),
    -- Triceps (ids 6-7)
    ('Tricep Pushdown',       'Triceps',     3, 12, 'easy',   20),
    ('Overhead Tricep Extension', 'Triceps', 3, 12, 'medium', 25),
    -- Biceps (ids 8-9)
    ('Bicep Curls',           'Biceps',      3, 12, 'easy',   20),
    ('Hammer Curls',          'Biceps',      3, 12, 'easy',   20),
    -- Back (ids 10-11)
    ('Lat Pulldown',          'Back',        3, 10, 'medium', 25),
    ('Seated Row',            'Back',        3, 10, 'medium', 25),
    -- Glutes (ids 12-17)
    ('Barbell Hip Thrusts',   'Glutes',      3, 10, 'medium', 30),
    ('Sumo Squats',           'Glutes',      3, 12, 'easy',   20),
    ('Bulgarian Split Squats','Quads',       3, 10, 'hard',   40),
    ('Walking Lunges',        'Quads',       3, 12, 'easy',   20),
    ('Cable Kickbacks',       'Glutes',      3, 15, 'easy',   15),
    ('Fire Hydrants',         'Glutes',      3, 15, 'easy',   15),
    -- Hamstrings and Quads (ids 18-21; Legs is split into these two groups)
    ('Barbell RDLs',          'Hamstrings',  3, 10, 'medium', 30),
    ('Hamstring Extension',   'Hamstrings',  3, 12, 'easy',   20),
    ('Squats',                'Quads',       3, 10, 'medium', 30),
    ('Leg Press',             'Quads',       3, 12, 'medium', 25),
    -- Abs (ids 22-23)
    ('Plank',                 'Abs',         3, 30, 'easy',   15),
    ('Crunches',              'Abs',         3, 20, 'easy',   15),
    -- Back, additional (id 24)
    ('Pull Ups',              'Back',        3, 8,  'hard',   40),
    -- Calves (ids 25-26)
    ('Standing Calf Raises',  'Calves',      3, 15, 'easy',   15),
    ('Seated Calf Raises',    'Calves',      3, 15, 'easy',   15),
    -- Forearms (ids 27-28)
    ('Wrist Curls',           'Forearms',    3, 15, 'easy',   15),
    ('Farmer''s Carry',       'Forearms',    3, 12, 'medium', 25),
    -- Added exercises (ids 29+), grouped by muscle group
    -- Chest
    ('Incline Dumbbell Press', 'Chest', 3, 10, 'medium', 25),
    ('Incline Barbell Press', 'Chest', 3, 8, 'hard', 35),
    ('Decline Barbell Press', 'Chest', 3, 8, 'hard', 35),
    ('Decline Dumbbell Press', 'Chest', 3, 10, 'medium', 25),
    ('Dumbbell Flys', 'Chest', 3, 12, 'easy', 15),
    ('Incline Dumbbell Flys', 'Chest', 3, 12, 'medium', 25),
    ('Cable Crossover', 'Chest', 3, 12, 'medium', 25),
    ('Low-to-High Cable Fly', 'Chest', 3, 12, 'medium', 25),
    ('Pec Deck Fly', 'Chest', 3, 12, 'easy', 15),
    ('Machine Chest Press', 'Chest', 3, 12, 'easy', 15),
    ('Smith Machine Incline Press', 'Chest', 3, 10, 'medium', 25),
    ('Incline Push Ups', 'Chest', 3, 12, 'easy', 15),
    ('Decline Push Ups', 'Chest', 3, 12, 'hard', 35),
    ('Wide Push Ups', 'Chest', 3, 15, 'easy', 15),
    ('Chest Dips', 'Chest', 3, 10, 'hard', 35),
    ('Svend Press', 'Chest', 3, 12, 'easy', 15),
    ('Dumbbell Pullover', 'Chest', 3, 12, 'medium', 25),
    -- Back
    ('Bent-Over Barbell Row', 'Back', 3, 10, 'hard', 35),
    ('One-Arm Dumbbell Row', 'Back', 3, 10, 'medium', 25),
    ('Deadlift', 'Back', 3, 6, 'hard', 35),
    ('Face Pulls', 'Back', 3, 15, 'easy', 15),
    ('Back Extensions', 'Back', 3, 12, 'easy', 15),
    ('T-Bar Row', 'Back', 3, 10, 'medium', 25),
    ('Machine Row', 'Back', 3, 12, 'easy', 15),
    ('Chest-Supported Row', 'Back', 3, 12, 'medium', 25),
    ('Inverted Row', 'Back', 3, 10, 'medium', 25),
    ('Straight-Arm Pulldown', 'Back', 3, 12, 'easy', 15),
    ('Close-Grip Lat Pulldown', 'Back', 3, 10, 'medium', 25),
    ('Reverse-Grip Lat Pulldown', 'Back', 3, 10, 'medium', 25),
    ('Wide-Grip Pull Ups', 'Back', 3, 8, 'hard', 35),
    ('Neutral-Grip Pull Ups', 'Back', 3, 8, 'hard', 35),
    ('Rack Pulls', 'Back', 3, 6, 'hard', 35),
    ('Pendlay Row', 'Back', 3, 8, 'hard', 35),
    ('Barbell Shrugs', 'Back', 3, 12, 'easy', 15),
    -- Shoulders
    ('Barbell Overhead Press', 'Shoulders', 3, 8, 'hard', 35),
    ('Arnold Press', 'Shoulders', 3, 10, 'medium', 25),
    ('Front Raises', 'Shoulders', 3, 12, 'easy', 15),
    ('Rear Delt Fly', 'Shoulders', 3, 12, 'medium', 25),
    ('Upright Row', 'Shoulders', 3, 12, 'medium', 25),
    ('Dumbbell Shrugs', 'Shoulders', 3, 15, 'easy', 15),
    ('Seated Dumbbell Overhead Press', 'Shoulders', 3, 10, 'medium', 25),
    ('Machine Overhead Press', 'Shoulders', 3, 12, 'easy', 15),
    ('Landmine Press', 'Shoulders', 3, 10, 'medium', 25),
    ('Push Press', 'Shoulders', 3, 8, 'hard', 35),
    ('Cable Rear Delt Fly', 'Shoulders', 3, 12, 'medium', 25),
    ('Reverse Pec Deck', 'Shoulders', 3, 12, 'easy', 15),
    ('Plate Front Raises', 'Shoulders', 3, 12, 'easy', 15),
    ('Cable Front Raises', 'Shoulders', 3, 12, 'easy', 15),
    ('Cable Side Raises', 'Shoulders', 3, 12, 'medium', 25),
    ('Cable Upright Row', 'Shoulders', 3, 12, 'medium', 25),
    ('Pike Push Ups', 'Shoulders', 3, 10, 'hard', 35),
    ('Band Pull-Aparts', 'Shoulders', 3, 15, 'easy', 15),
    -- Triceps
    ('Skull Crushers', 'Triceps', 3, 10, 'medium', 25),
    ('Tricep Dips', 'Triceps', 3, 10, 'medium', 25),
    ('Close-Grip Press', 'Triceps', 3, 8, 'hard', 35),
    ('Tricep Kickbacks', 'Triceps', 3, 12, 'easy', 15),
    ('Diamond Push Ups', 'Triceps', 3, 10, 'medium', 25),
    ('Rope Pushdown', 'Triceps', 3, 12, 'easy', 15),
    ('Straight-Bar Pushdown', 'Triceps', 3, 12, 'easy', 15),
    ('Reverse-Grip Pushdown', 'Triceps', 3, 12, 'medium', 25),
    ('Single-Arm Cable Pushdown', 'Triceps', 3, 12, 'easy', 15),
    ('Overhead Rope Extension', 'Triceps', 3, 12, 'medium', 25),
    ('Dumbbell Overhead Extension', 'Triceps', 3, 12, 'medium', 25),
    ('Seated EZ-Bar Extension', 'Triceps', 3, 10, 'medium', 25),
    ('Bench Dips', 'Triceps', 3, 12, 'easy', 15),
    ('Cable Skull Crushers', 'Triceps', 3, 12, 'medium', 25),
    ('Dumbbell Skull Crushers', 'Triceps', 3, 10, 'medium', 25),
    ('Close-Grip Push Ups', 'Triceps', 3, 12, 'easy', 15),
    ('Machine Dips', 'Triceps', 3, 12, 'easy', 15),
    ('JM Press', 'Triceps', 3, 8, 'hard', 35),
    -- Biceps
    ('Barbell Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Concentration Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Incline Dumbbell Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Preacher Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Cable Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Chin Ups', 'Biceps', 3, 8, 'hard', 35),
    ('EZ-Bar Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Spider Curls', 'Biceps', 3, 12, 'medium', 25),
    ('Drag Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Machine Preacher Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Dumbbell Preacher Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Alternating Dumbbell Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Seated Dumbbell Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Rope Hammer Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Cross-Body Hammer Curls', 'Biceps', 3, 12, 'easy', 15),
    ('Bayesian Curls', 'Biceps', 3, 12, 'medium', 25),
    ('Wide-Grip Barbell Curls', 'Biceps', 3, 10, 'medium', 25),
    ('Single-Arm Cable Curls', 'Biceps', 3, 12, 'easy', 15),
    -- Quads
    ('Goblet Squats', 'Quads', 3, 12, 'easy', 15),
    ('Front Squats', 'Quads', 3, 8, 'hard', 35),
    ('Leg Extensions', 'Quads', 3, 12, 'easy', 15),
    ('Step Ups', 'Quads', 3, 10, 'medium', 25),
    ('Reverse Lunges', 'Quads', 3, 12, 'easy', 15),
    ('Hack Squat', 'Quads', 3, 10, 'medium', 25),
    ('Smith Machine Squats', 'Quads', 3, 10, 'medium', 25),
    ('Sissy Squats', 'Quads', 3, 12, 'hard', 35),
    ('Box Squats', 'Quads', 3, 8, 'hard', 35),
    ('Pause Squats', 'Quads', 3, 6, 'hard', 35),
    ('Lateral Lunges', 'Quads', 3, 10, 'medium', 25),
    ('Split Squats', 'Quads', 3, 10, 'medium', 25),
    ('Single-Leg Press', 'Quads', 3, 10, 'medium', 25),
    ('Assisted Pistol Squats', 'Quads', 3, 8, 'hard', 35),
    ('Jump Squats', 'Quads', 3, 12, 'medium', 25),
    ('Dumbbell Squats', 'Quads', 3, 12, 'easy', 15),
    -- Hamstrings
    ('Lying Leg Curls', 'Hamstrings', 3, 12, 'medium', 25),
    ('Seated Leg Curls', 'Hamstrings', 3, 12, 'easy', 15),
    ('Dumbbell RDLs', 'Hamstrings', 3, 12, 'easy', 15),
    ('Single-Leg RDLs', 'Hamstrings', 3, 10, 'medium', 25),
    ('Good Mornings', 'Hamstrings', 3, 10, 'hard', 35),
    ('Nordic Hamstring Curls', 'Hamstrings', 3, 6, 'hard', 35),
    ('Stiff-Leg Deadlift', 'Hamstrings', 3, 8, 'hard', 35),
    ('Standing Leg Curls', 'Hamstrings', 3, 12, 'easy', 15),
    ('Cable Leg Curls', 'Hamstrings', 3, 12, 'easy', 15),
    ('Swiss Ball Leg Curls', 'Hamstrings', 3, 12, 'medium', 25),
    ('Slider Leg Curls', 'Hamstrings', 3, 10, 'medium', 25),
    ('Glute-Ham Raises', 'Hamstrings', 3, 8, 'hard', 35),
    ('Sumo Deadlift', 'Hamstrings', 3, 6, 'hard', 35),
    ('Smith Machine RDLs', 'Hamstrings', 3, 10, 'medium', 25),
    ('Kettlebell Swings', 'Hamstrings', 3, 15, 'medium', 25),
    ('Hamstring Walkouts', 'Hamstrings', 3, 8, 'medium', 25),
    ('Single-Leg Hamstring Bridge', 'Hamstrings', 3, 12, 'medium', 25),
    ('Landmine RDLs', 'Hamstrings', 3, 10, 'medium', 25),
    -- Glutes
    ('Glute Bridges', 'Glutes', 3, 15, 'easy', 15),
    ('Donkey Kicks', 'Glutes', 3, 15, 'easy', 15),
    ('Cable Pull-Throughs', 'Glutes', 3, 12, 'medium', 25),
    ('Single-Leg Hip Thrusts', 'Glutes', 3, 10, 'hard', 35),
    ('Curtsy Lunges', 'Glutes', 3, 10, 'medium', 25),
    ('Dumbbell Hip Thrusts', 'Glutes', 3, 12, 'easy', 15),
    ('Smith Machine Hip Thrusts', 'Glutes', 3, 10, 'medium', 25),
    ('Banded Lateral Walks', 'Glutes', 3, 15, 'easy', 15),
    ('Hip Abduction Machine', 'Glutes', 3, 15, 'easy', 15),
    ('Cable Hip Abduction', 'Glutes', 3, 15, 'easy', 15),
    ('Single-Leg Glute Bridges', 'Glutes', 3, 12, 'medium', 25),
    ('Frog Pumps', 'Glutes', 3, 20, 'easy', 15),
    ('Banded Clamshells', 'Glutes', 3, 15, 'easy', 15),
    ('Banded Glute Kickbacks', 'Glutes', 3, 15, 'easy', 15),
    ('Dumbbell Sumo Deadlift', 'Glutes', 3, 10, 'medium', 25),
    ('Elevated Glute Bridges', 'Glutes', 3, 12, 'medium', 25),
    -- Abs
    ('Leg Raises', 'Abs', 3, 12, 'medium', 25),
    ('Russian Twists', 'Abs', 3, 20, 'medium', 25),
    ('Bicycle Crunches', 'Abs', 3, 20, 'easy', 15),
    ('Mountain Climbers', 'Abs', 3, 20, 'medium', 25),
    ('Dead Bug', 'Abs', 3, 12, 'easy', 15),
    ('Hanging Knee Raises', 'Abs', 3, 10, 'hard', 35),
    ('Ab Wheel Rollouts', 'Abs', 3, 8, 'hard', 35),
    ('Cable Crunches', 'Abs', 3, 15, 'medium', 25),
    ('Hanging Leg Raises', 'Abs', 3, 10, 'hard', 35),
    ('V-Ups', 'Abs', 3, 12, 'medium', 25),
    ('Reverse Crunches', 'Abs', 3, 15, 'easy', 15),
    ('Flutter Kicks', 'Abs', 3, 20, 'easy', 15),
    ('Toe Touches', 'Abs', 3, 15, 'easy', 15),
    ('Sit Ups', 'Abs', 3, 15, 'easy', 15),
    ('Decline Sit Ups', 'Abs', 3, 12, 'medium', 25),
    ('Oblique Crunches', 'Abs', 3, 15, 'easy', 15),
    ('Cable Woodchoppers', 'Abs', 3, 12, 'medium', 25),
    ('Heel Taps', 'Abs', 3, 20, 'easy', 15),
    -- Calves
    ('Donkey Calf Raises', 'Calves', 3, 15, 'medium', 25),
    ('Single-Leg Calf Raises', 'Calves', 3, 12, 'medium', 25),
    ('Leg Press Calf Raises', 'Calves', 3, 15, 'easy', 15),
    ('Smith Machine Calf Raises', 'Calves', 3, 15, 'easy', 15),
    -- Forearms
    ('Reverse Curls', 'Forearms', 3, 12, 'easy', 15),
    ('Reverse Wrist Curls', 'Forearms', 3, 15, 'easy', 15),
    ('Zottman Curls', 'Forearms', 3, 10, 'medium', 25),
    ('Behind-the-Back Wrist Curls', 'Forearms', 3, 15, 'easy', 15),
    ('Wrist Roller', 'Forearms', 3, 8, 'medium', 25);


-- COSMETIC ITEMS (required) - starter wardrobe for the locker

-- IMPORTANT: rows 1-6 are the gendered default items equipped on registration,
-- and row 7 is the default hair. AvatarGenerationActivity.java references these
-- IDs as constants:
--   Male defaults:   top=1, bottom=2, socks=3, hair=7
--   Female defaults: top=4, bottom=5, socks=6, hair=7
-- Keep these IDs/order in sync with that file.
--
-- cosmetic_img_data holds the item KEY only. The compositor builds the full path:
--   clothing: assets/{type}/{gender}-{bodytype}/{key}.png  (falls back to -average)
--   hair:     assets/hair/{gender}-hair/{colour}_{key}.png (falls back to grey)
-- Every clothing item is gender-specific (no unisex garments in this wardrobe).
INSERT INTO cosmetic_item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data) VALUES
    -- MALE default items (worn on registration): id 1-3
    ('White Tank',    'top',    'male',   FALSE, 'default', 0, 0, 'white_tank_default'),   -- id 1
    ('Black Shorts',  'bottom', 'male',   FALSE, 'default', 0, 0, 'black_shorts_default'), -- id 2
    ('White Socks',   'socks',  'male',   FALSE, 'default', 0, 0, 'white_socks_default'),  -- id 3
    -- FEMALE default items (worn on registration): id 4-6. Keys use the new
    -- suffix-free naming. NOTE: the female base body already has black shorts
    -- painted on, so id 5 has NO bottoms overlay file - the compositor draws
    -- nothing for it (its locker thumbnail comes from the placeholder .jpeg).
    ('White Tank',    'top',    'female', FALSE, 'default', 0, 0, 'white_tank_default'),   -- id 4
    ('Black Shorts',  'bottom', 'female', FALSE, 'default', 0, 0, 'black_shorts_default'), -- id 5
    ('White Socks',   'socks',  'female', FALSE, 'default', 0, 0, 'white_socks_default'),  -- id 6
    -- MALE DEFAULT HAIR (worn on registration): id 7. Colourable (grey/brown/black/blonde/ginger).
    ('Default Hair',  'hair',   'male',   TRUE,  'default', 0, 0, 'default_hairstyle'),    -- id 7
    -- FEMALE DEFAULT HAIR (worn on registration by female users): id 8. Colourable.
    -- Female hair HAS a grey variant (assets/hair/female-hair/grey_default_hairstyle.png),
    -- so the compositor uses grey by default; AvatarGenerationActivity equips this
    -- id (8) for female users.
    ('Default Hair',  'hair',   'female', TRUE,  'default', 0, 0, 'default_hairstyle'),    -- id 8
    -- MALE wearables (all free for testing so items can be tried on freely).
    ('White T-Shirt', 'top',    'male',   FALSE, 'default', 0, 0, 'white_tshirt'),
    ('Black Tee',     'top',    'male',   FALSE, 'default', 0, 0, 'black_tee'),
    ('Grey Zip Hoodie','top',   'male',   FALSE, 'default', 0, 0, 'grey_zip_hoodie'),
    ('Navy Tank',     'top',    'male',   FALSE, 'default', 0, 0, 'navy_tank'),
    ('Red Hoodie',    'top',    'male',   FALSE, 'default', 0, 0, 'red_hoodie'),
    ('Black Board Shorts','bottom','male',FALSE, 'default', 0, 0, 'black_board_shorts'),
    ('Red Shorts',    'bottom', 'male',   FALSE, 'default', 0, 0, 'red_shorts'),
    ('White Board Shorts','bottom','male',FALSE, 'default', 0, 0, 'white_board_shorts'),
    ('Red Nike Shorts','bottom','male',   FALSE, 'default', 0, 0, 'red_nike_shorts'),
    ('White Sneakers','shoes',  'male',   FALSE, 'default', 0, 0, 'white_sneakers'),
    ('Nike Blazers',  'shoes',  'male',   FALSE, 'default', 0, 0, 'white_nike_blazers'),
    ('Converse',      'shoes',  'male',   FALSE, 'default', 0, 0, 'black_converse_sneakers'),
    ('Running Shoes', 'shoes',  'male',   FALSE, 'default', 0, 0, 'blue_running_shoes'),
    ('Pink Trainers', 'shoes',  'male',   FALSE, 'default', 0, 0, 'pink_trainers'),
    -- MALE set-colour hair (is_colourable=FALSE, colour baked into the key; free).
    -- imageData is the FULL filename stem.
    ('Cornrows',      'hair',   'male',   FALSE, 'default', 0, 0, 'black_cornrows_hairstyle'),
    ('Flynn',         'hair',   'male',   FALSE, 'default', 0, 0, 'black_flynn_hairstyle'),
    ('Short Afro',    'hair',   'male',   FALSE, 'default', 0, 0, 'black_short_afro_hairstyle'),
    ('Surfer Curls',  'hair',   'male',   FALSE, 'default', 0, 0, 'blonde_surfer_curly_hairstyle'),
    ('Buzz Cut',      'hair',   'male',   FALSE, 'default', 0, 0, 'brown_buzz_hairstyle'),
    ('Short Dreads',  'hair',   'male',   FALSE, 'default', 0, 0, 'brown_short_dreads_hairstyle'),
    -- FEMALE wearables (all free for testing). Art exists for female-average/built/chubby.
    ('Blue Jacket',    'top',    'female', FALSE, 'default', 0, 0, 'blue_jacket'),
    ('Blue Nike Tank', 'top',    'female', FALSE, 'default', 0, 0, 'blue_nike_tank'),
    ('Pink T-Shirt',   'top',    'female', FALSE, 'default', 0, 0, 'pink_tshirt'),
    ('Black Jacket',   'top',    'female', FALSE, 'default', 0, 0, 'black_jacket'),
    ('Grey Layered Top','top',   'female', FALSE, 'default', 0, 0, 'grey_layered_top'),
    ('White Nike Top', 'top',    'female', FALSE, 'default', 0, 0, 'white_nike_top'),
    ('Blue Tights',    'bottom', 'female', FALSE, 'default', 0, 0, 'blue_tights'),
    ('Pink Nike Shorts','bottom','female', FALSE, 'default', 0, 0, 'pink_nike_shorts'),
    ('Black Jorts',    'bottom', 'female', FALSE, 'default', 0, 0, 'black_jorts'),
    ('Grey Capri Pants','bottom','female', FALSE, 'default', 0, 0, 'grey_capri_pants'),
    ('Grey Tights',    'bottom', 'female', FALSE, 'default', 0, 0, 'grey_tights'),
    ('White Nike Shorts','bottom','female',FALSE, 'default', 0, 0, 'white_nike_shorts'),
    ('White Sneakers', 'shoes',  'female', FALSE, 'default', 0, 0, 'white_sneakers'),
    ('Nike Blazers',   'shoes',  'female', FALSE, 'default', 0, 0, 'white_nike_blazers'),
    ('Pink Trainers',  'shoes',  'female', FALSE, 'default', 0, 0, 'pink_trainers'),
    ('Converse',       'shoes',  'female', FALSE, 'default', 0, 0, 'black_converse'),
    ('Blue Trainers',  'shoes',  'female', FALSE, 'default', 0, 0, 'blue_trainers'),
    -- FEMALE set-colour hair (is_colourable=FALSE; colour baked into the key; free).
    -- Keys are the FULL filename stems (note brown_messy_bun has no _hairstyle suffix).
    ('Bantu Knots',   'hair',   'female', FALSE, 'default', 0, 0, 'black_bantu_knots_hairstyle'),
    ('Cornrows Bun',  'hair',   'female', FALSE, 'default', 0, 0, 'black_cornrows_bun_hairstyle'),
    ('Silk Press',    'hair',   'female', FALSE, 'default', 0, 0, 'black_slik_press_hairstyle'),
    ('Beach Waves',   'hair',   'female', FALSE, 'default', 0, 0, 'blonde_beach_wave_hairstyle'),
    ('Double Braid',  'hair',   'female', FALSE, 'default', 0, 0, 'blonde_double_braid_hairstyle'),
    ('Straight',      'hair',   'female', FALSE, 'default', 0, 0, 'blonde_straight_hairstyle'),
    ('Messy Bun',     'hair',   'female', FALSE, 'default', 0, 0, 'brown_messy_bun'),
    -- PLACEHOLDER (non-wearable) filler items: unlock_type='placeholder'. They sit
    -- LOCKED in the locker as "coming soon". imageData is the .jpeg filename stem in
    -- assets/{type}/male-placeholders/. Display price shown but not purchasable yet.
    ('Adidas Hoodie',        'top',    'male', FALSE, 'placeholder', 0, 100, 'black_adidas_hoodie'),
    ('Compression Shirt',    'top',    'male', FALSE, 'placeholder', 0, 100, 'black_compression_shirt'),
    ('Blue T-Shirt',         'top',    'male', FALSE, 'placeholder', 0, 100, 'blue_tshirt'),
    ('Under Armour Shirt',   'top',    'male', FALSE, 'placeholder', 0, 100, 'navy_under_armour_shirt'),
    ('Red T-Shirt',          'top',    'male', FALSE, 'placeholder', 0, 100, 'red_tshirt'),
    ('Adidas Shorts',        'bottom', 'male', FALSE, 'placeholder', 0, 100, 'black_adidas_shorts'),
    ('Black Sweatpants',     'bottom', 'male', FALSE, 'placeholder', 0, 100, 'black_sweatpants'),
    ('Grey Shorts',          'bottom', 'male', FALSE, 'placeholder', 0, 100, 'grey_shorts'),
    ('Under Armour Shorts',  'bottom', 'male', FALSE, 'placeholder', 0, 100, 'navy_under_armour_shorts'),
    ('Green Jordans',        'shoes',  'male', FALSE, 'placeholder', 0, 100, 'green_jordan_sneakers_placeholder'),
    ('Pink Running Shoes',   'shoes',  'male', FALSE, 'placeholder', 0, 100, 'pink_running_shoes_placeholder'),
    ('Red Jordans',          'shoes',  'male', FALSE, 'placeholder', 0, 100, 'red_jordan_sneakers_placeholder'),
    ('Adidas Sambas',        'shoes',  'male', FALSE, 'placeholder', 0, 100, 'white_adidas_sambas_placeholder'),
    -- FEMALE placeholder (non-wearable) filler items: unlock_type='placeholder'.
    -- imageData is the .jpeg stem in assets/{type}/female-placeholders/.
    ('Green Adidas Bra',     'top',    'female', FALSE, 'placeholder', 0, 100, 'green_adidas_sports_bra'),
    ('Green Nike Jacket',    'top',    'female', FALSE, 'placeholder', 0, 100, 'green_nike_jacket'),
    ('Under Armour Tank',    'top',    'female', FALSE, 'placeholder', 0, 100, 'navy_under_armour_tank'),
    ('Pink Sports Bra',      'top',    'female', FALSE, 'placeholder', 0, 100, 'pink_sports_bra'),
    ('Red Nike Bra',         'top',    'female', FALSE, 'placeholder', 0, 100, 'red_nike_sports_bra'),
    ('Blue Shorts',          'bottom', 'female', FALSE, 'placeholder', 0, 100, 'blue_shorts'),
    ('Under Armour Shorts',  'bottom', 'female', FALSE, 'placeholder', 0, 100, 'navy_under_armour_shorts'),
    ('Under Armour Sweats',  'bottom', 'female', FALSE, 'placeholder', 0, 100, 'navy_under_armour_sweatpants'),
    ('Pink Sweatpants',      'bottom', 'female', FALSE, 'placeholder', 0, 100, 'pink_sweatpants'),
    ('Red Nike Shorts',      'bottom', 'female', FALSE, 'placeholder', 0, 100, 'red_nike_shorts'),
    ('Green Jordans',        'shoes',  'female', FALSE, 'placeholder', 0, 100, 'green_jordan_sneakers_placeholder'),
    ('Pink Running Shoes',   'shoes',  'female', FALSE, 'placeholder', 0, 100, 'pink_running_shoes_placeholder'),
    ('Red Jordans',          'shoes',  'female', FALSE, 'placeholder', 0, 100, 'red_jordan_sneakers_placeholder'),
    ('Adidas Sambas',        'shoes',  'female', FALSE, 'placeholder', 0, 100, 'white_adidas_sambas_placeholder');


-- ADMIN ACCOUNT (required) - so admins can create default workouts

-- Passwords are PBKDF2 "salt:hash" values (see PasswordHasher.java), not plain
-- text. The admin plain password (development only) is: admin123
INSERT INTO admins (admin_username, admin_password) VALUES
    ('Admin', '5TiM4gPGPyDz7/ioNoCm1g==:a5lJJEC1iET8NRxBB/1IuNjoAzx/HmFq8GYpXPeMo3w=');


-- TEST USERS (development only)
-- Passwords are PBKDF2 "salt:hash" values (see PasswordHasher.java). The plain
-- passwords (development only) are:
--   dyllan12345 -> Password1
--   sarahj      -> Password2
--   jaskrug     -> Password3

-- XP matches each user's level under the levelling curve (XP for N->N+1 = 400 + 50*N):
--   Dylan level 10  -> needs 5850 XP (xp 6000),  rank Bronze III (rank_id 3)
--   Sarah level 3   -> needs 950 XP  (xp 1000),  rank Bronze I   (rank_id 1)
--   Jason level 18  -> needs 14450 XP (xp 15000), rank Silver I  (rank_id 4)
INSERT INTO user (first_name, email, username, set_password, xp, coins, current_level, rank_id, current_streak) VALUES
    ('Dylan Woods',  'dylan@example.com',  'dyllan12345', 'hpNFcW/IRXmzMCIgArGJkg==:3GzlcUiT+0iZfFSAEr8jOi7fQ07mmD9aJ71To27z4Ro=', 6000, 100, 10, 3, 14),
    ('Sarah Fisher',  'sarah@example.com',  'sarahj',      'vthPYIU70FmDQmkJHEk4Lw==:vseAyU2qxplWT8ggyYsmbJ9IHpM8QL5RcXEp2RxYge0=', 1000, 50,  3,  1, 2),
    ('Jason Kruger',   'jason@example.com',   'jaskrug',       'ZKoWh3LUmuUxFkanuLWZRw==:1Fb77qBPqcSajwiFuwRl7ahZDtEglGEGzR/RDyQkDEU=', 15000, 200, 18, 4, 21);


-- USER PROFILES (test data) - fitness assessment results

-- body_type must be one of the 3 offered per gender (the only ones with art):
--   male   = average, built, chubby
--   female = average, toned, chubby
-- skinny/overweight were removed from the app; 'toned' has no MALE art.
INSERT INTO user_profile (user_id, user_gender, user_age, user_weight, user_height, body_type, workout_lvl, num_workouts_per_week, fitness_goal) VALUES
    (1, 'Male',   25, 80.00,  185.00, 'built',   'intermediate', 5, 'build_muscle'),
    (2, 'Female', 28, 62.50,  168.00, 'average', 'beginner',     3, 'get_fitter'),
    (3, 'Male',   31, 75.00,  178.00, 'built',   'advanced',     6, 'build_muscle');


-- AVATARS (test data) - one per user

-- skin_tone must have art: grey (default), fair, tan, dark. (No 'beige' art.)
-- body_shape must be one of the 3 offered per gender (same as body_type above):
--   male = average/built/chubby, female = average/toned/chubby.
INSERT INTO avatar (user_id, skin_tone, body_shape, facial_details) VALUES
    (1, 'tan',   'built',   'short_brown_hair'),
    (2, 'fair',  'average', 'long_blonde_hair'),
    (3, 'tan',   'built',   'short_black_hair');

-- Link each user back to their avatar
UPDATE user SET avatar_id = 1 WHERE user_id = 1;
UPDATE user SET avatar_id = 2 WHERE user_id = 2;
UPDATE user SET avatar_id = 3 WHERE user_id = 3;


-- WORKOUTS 

-- Default workouts (admin created, user_id is NULL)

INSERT INTO workout (user_id, workout_name, is_default) VALUES
    (NULL, 'Push Day',                 TRUE),
    (NULL, 'Pull Day',                 TRUE),
    (NULL, 'Leg Day',                  TRUE),
    (NULL, 'Core Burner',              TRUE);

-- User created workouts

INSERT INTO workout (user_id, workout_name, is_default) VALUES
    (1, 'Chest, Shoulders and Triceps', FALSE),
    (1, 'Back and Biceps',              FALSE),
    (1, 'Glutes and Hamstrings',        FALSE),
    (2, 'Beginner Full Body',           FALSE);


-- WORKOUT EXERCISES - what's in each workout

-- Push Day (workout 1)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (1, 1, 3, 10, 1),  -- Bench Press
    (1, 4, 3, 12, 2),  -- Dumbbell Shoulder Press
    (1, 6, 3, 12, 3);  -- Tricep Pushdown

-- Pull Day (workout 2)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (2, 10, 3, 10, 1), -- Lat Pulldown
    (2, 11, 3, 10, 2), -- Seated Row
    (2, 24, 3, 8,  3), -- Pull Ups
    (2, 8,  3, 12, 4), -- Bicep Curls
    (2, 9,  3, 12, 5); -- Hammer Curls

-- Leg Day (workout 3)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (3, 20, 3, 10, 1), -- Squats
    (3, 21, 3, 12, 2), -- Leg Press
    (3, 18, 3, 10, 3), -- Barbell RDLs
    (3, 15, 3, 12, 4), -- Walking Lunges
    (3, 25, 3, 15, 5); -- Standing Calf Raises

-- Core Burner (workout 4)
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (4, 22, 3, 30, 1), -- Plank
    (4, 23, 3, 20, 2), -- Crunches
    (4, 16, 3, 15, 3); -- Fire Hydrants

-- Dylan's "Chest, Shoulders and Triceps" workout 
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (5, 1, 3, 10, 1),  -- Bench Press
    (5, 4, 3, 12, 2),  -- Dumbbell Shoulder Press
    (5, 6, 3, 12, 3);  -- Tricep Pushdown

-- Dylan's "Back and Biceps" workout 
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (6, 10, 3, 10, 1), -- Lat Pulldown
    (6, 11, 3, 10, 2), -- Seated Row
    (6, 8,  3, 12, 3), -- Bicep Curls
    (6, 9,  3, 12, 4); -- Hammer Curls

-- Dylan's "Glutes and Hamstrings" workout 
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (7, 12, 3, 10, 1), -- Barbell Hip Thrusts
    (7, 18, 3, 10, 2), -- Barbell RDLs
    (7, 19, 3, 12, 3); -- Hamstring Extension

-- Sarah's "Beginner Full Body" workout 
INSERT INTO workout_exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (8, 3,  3, 15, 1), -- Push Ups
    (8, 20, 3, 10, 2), -- Squats
    (8, 10, 3, 10, 3), -- Lat Pulldown
    (8, 4,  3, 12, 4), -- Dumbbell Shoulder Press
    (8, 23, 3, 20, 5); -- Crunches


-- SCHEDULED WORKOUTS (test data) - calendar entries for Dylan

-- Dates are relative to CURDATE() so the demo always has something today and
-- this week: one completed yesterday, one scheduled for today, and one later
-- this week.
INSERT INTO scheduled_workout (user_id, workout_id, scheduled_date, is_completed, earned_xp) VALUES
    (1, 7, DATE_ADD(CURDATE(), INTERVAL -1 DAY), TRUE,  90),
    (1, 5, CURDATE(),                            FALSE, 0),
    (1, 5, DATE_ADD(CURDATE(), INTERVAL 3 DAY),  FALSE, 0);


-- EXERCISE LOG (test data): what Dyllan actually lifted in yesterday's workout
-- (workout 7). These are the "last weights" shown in the library and pre-filled
-- the next time he does those exercises.

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


-- GOALS (test data)

-- goal_priority must be exactly 'Top', 'Medium' or 'Low' (the values the app
-- saves and matches on). Deadlines are relative to CURDATE() so they stay in
-- the future for a demo.
-- goal_title holds the short title; goal_description holds the optional notes
-- (NULL when the goal has no notes).
INSERT INTO goal (user_id, goal_title, goal_description, deadline, goal_priority, is_completed, created_at) VALUES
    (1, 'Move up a weight in bicep curls', 'I want to move up from 18kgs to 20kgs within 2 weeks.', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'Medium', FALSE, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, 'Bench press my body weight',       NULL,                                                    DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'Top',    FALSE, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (2, 'Complete 3 workouts a week for a month', NULL,                                              DATE_ADD(CURDATE(), INTERVAL 21 DAY), 'Medium', FALSE, DATE_SUB(NOW(), INTERVAL 3 DAY));


-- USER COSMETICS (test data) 

-- Dylan (user 1) is male; he owns the male default set + hair, plus a couple of extras.
-- NOTE: ids 8 and 9 are NOT male items (8 = female Default Hair, 9 = White
-- T-Shirt), so Dylan now owns genuine male extras: id 18 (White Sneakers, male
-- shoes) and id 11 (Grey Zip Hoodie, male top).
INSERT INTO user_cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped) VALUES
    (1, 1,  TRUE, TRUE),   -- White Tank (male, equipped)
    (1, 2,  TRUE, TRUE),   -- Black Shorts (male, equipped)
    (1, 3,  TRUE, TRUE),   -- White Socks (male, equipped)
    (1, 7,  TRUE, TRUE),   -- Default Hair (male, equipped)
    (1, 18, TRUE, FALSE),  -- White Sneakers (male, owned, not worn - socks only by default)
    (1, 11, TRUE, FALSE);  -- Grey Zip Hoodie (male, owned, not equipped)


-- OUTFIT SLOTS (test data) - Dylan's currently equipped outfit
-- Wearing tank + shorts + socks (no shoes) by default.

INSERT INTO outfit_slot (user_id, top_id, bottom_id, shoes_id, socks_id, hair_id, accessories_id) VALUES
    (1, 1, 2, NULL, 3, 7, NULL);


-- STREAKS (test data) - Dylan's 2-week streak with XP multiplier

INSERT INTO streak (user_id, week_st_date, num_workouts_completed, met_goal, multiplier) VALUES
    (1, '2026-08-25', 5, TRUE, 1.50);
