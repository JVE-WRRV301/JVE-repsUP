-- REPS UP - Seed Data


USE reps_up;


-- RANKS (required) 

INSERT INTO Ranks (tier, subtier, min_lvl_required) VALUES
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

INSERT INTO Exercise (exercise_name, muscle_group, default_sets, default_reps, difficulty, base_xp) VALUES
    -- Chest
    ('Bench Press',           'chest',       3, 10, 'medium', 30),
    ('Dumbbell Chest Press',  'chest',       3, 12, 'medium', 25),
    ('Push Ups',              'chest',       3, 15, 'easy',   15),
    -- Shoulders
    ('Dumbbell Shoulder Press', 'shoulders', 3, 12, 'medium', 25),
    ('Dumbbell Lateral Raises', 'shoulders', 3, 12, 'easy',   20),
    -- Triceps
    ('Tricep Pushdown',       'triceps',     3, 12, 'easy',   20),
    ('Cable Tricep Pushdown', 'triceps',     3, 12, 'easy',   20),
    -- Biceps
    ('Bicep Curls',           'biceps',      3, 12, 'easy',   20),
    ('Hammer Curls',          'biceps',      3, 12, 'easy',   20),
    -- Back
    ('Lat Pulldown',          'lats',        3, 10, 'medium', 25),
    ('Seated Row',            'lats',        3, 10, 'medium', 25),
    -- Glutes
    ('Barbell Hip Thrusts',   'glutes',      3, 10, 'medium', 30),
    ('Sumo Squats',           'glutes',      3, 12, 'easy',   20),
    ('Bulgarian Split Squats','glutes',      3, 10, 'hard',   40),
    ('Walking Lunges',        'glutes',      3, 12, 'easy',   20),
    ('Cable Kickbacks',       'glutes',      3, 15, 'easy',   15),
    ('Fire Hydrants',         'glutes',      3, 15, 'easy',   15),
    -- Hamstrings
    ('Barbell RDLs',          'hamstrings',  3, 10, 'medium', 30),
    ('Hamstring Extension',   'hamstrings',  3, 12, 'easy',   20),
    -- Quads
    ('Squats',                'quads',       3, 10, 'medium', 30),
    ('Leg Press',             'quads',       3, 12, 'medium', 25),
    -- Abs
    ('Plank',                 'abdominals',  3, 30, 'easy',   15),
    ('Crunches',              'abdominals',  3, 20, 'easy',   15);


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
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data) VALUES
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
    -- Female hair has no grey variant, so the compositor falls through to another
    -- colour; AvatarGenerationActivity equips this id (8) for female users.
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

INSERT INTO Admins (admin_username, admin_password) VALUES
    ('Admin', 'admin123');


-- TEST USERS (development only)
-- NOTE: passwords are placeholders. 

INSERT INTO User (first_name, email, username, set_password, xp, coins, current_level, rank_id, current_streak) VALUES
    ('Dylan Woods',  'dylan@example.com',  'dyllan12345', 'placeholder_1', 450, 100, 10, 3, 14),
    ('Sarah Fisher',  'sarah@example.com',  'sarahj',      'placeholder_2', 120, 50,  3,  1, 2),
    ('Jason Kruger',   'jason@example.com',   'jaskrug',       'placeholder_3', 800, 200, 18, 4, 21);


-- USER PROFILES (test data) - fitness assessment results

-- body_type matches the base_* body art: skinny, average, toned, built, chubby, overweight
INSERT INTO User_Profile (user_id, user_gender, user_age, user_weight, user_height, body_type, workout_lvl, num_workouts_per_week, fitness_goal) VALUES
    (1, 'Male',   25, 80.00,  185.00, 'toned',   'intermediate', 5, 'build_muscle'),
    (2, 'Female', 28, 62.50,  168.00, 'average', 'beginner',     3, 'get_fitter'),
    (3, 'Male',   31, 75.00,  178.00, 'built',   'advanced',     6, 'build_muscle');


-- AVATARS (test data) - one per user

-- skin_tone is one of: grey (default, pre-selection), fair, beige, tan, dark
-- body_shape matches the base_* body art: skinny, average, toned, built, chubby, overweight
INSERT INTO Avatar (user_id, skin_tone, body_shape, facial_details) VALUES
    (1, 'beige', 'toned',   'short_brown_hair'),
    (2, 'fair',  'average', 'long_blonde_hair'),
    (3, 'tan',   'built',   'short_black_hair');

-- Link each user back to their avatar
UPDATE User SET avatar_id = 1 WHERE user_id = 1;
UPDATE User SET avatar_id = 2 WHERE user_id = 2;
UPDATE User SET avatar_id = 3 WHERE user_id = 3;


-- WORKOUTS 

-- Default workouts (admin created, user_id is NULL)

INSERT INTO Workout (user_id, workout_name, is_default) VALUES
    (NULL, 'Push Day',                 TRUE),
    (NULL, 'Pull Day',                 TRUE),
    (NULL, 'Leg Day',                  TRUE),
    (NULL, 'Core Burner',              TRUE);

-- User created workouts

INSERT INTO Workout (user_id, workout_name, is_default) VALUES
    (1, 'Chest, Shoulders and Triceps', FALSE),
    (1, 'Back and Biceps',              FALSE),
    (1, 'Glutes and Hamstrings',        FALSE),
    (2, 'Beginner Full Body',           FALSE);


-- WORKOUT EXERCISES - what's in each workout

-- Push Day (workout 1)
INSERT INTO Workout_Exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (1, 1, 3, 10, 1),  -- Bench Press
    (1, 4, 3, 12, 2),  -- Dumbbell Shoulder Press
    (1, 6, 3, 12, 3);  -- Tricep Pushdown

-- Dylan's "Chest, Shoulders and Triceps" workout 
INSERT INTO Workout_Exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (5, 1, 3, 10, 1),  -- Bench Press
    (5, 4, 3, 12, 2),  -- Dumbbell Shoulder Press
    (5, 6, 3, 12, 3);  -- Tricep Pushdown

-- Dylan's "Glutes and Hamstrings" workout 
INSERT INTO Workout_Exercise (workout_id, exercise_id, num_sets, num_reps, workout_exercise_order_index) VALUES
    (7, 12, 3, 10, 1), -- Barbell Hip Thrusts
    (7, 18, 3, 10, 2), -- Barbell RDLs
    (7, 19, 3, 12, 3); -- Hamstring Extension


-- SCHEDULED WORKOUTS (test data) - calendar entries for Dylan

INSERT INTO Scheduled_Workout (user_id, workout_id, scheduled_date, is_completed, earned_xp) VALUES
    (1, 5, '2026-09-04', FALSE, 0),
    (1, 7, '2026-09-02', TRUE,  90),
    (1, 5, '2026-09-11', FALSE, 0);


-- GOALS (test data)

INSERT INTO Goal (user_id, goal_description, deadline, goal_priority, is_completed) VALUES
    (1, 'Move up a weight in bicep curls. I want to move up from 18kgs to 20kgs within 2 weeks.', '2026-09-15', 'medium', FALSE),
    (1, 'Bench press my body weight',                                                              '2026-09-30', 'top',    FALSE),
    (2, 'Complete 3 workouts a week for a month',                                                  '2026-10-01', 'medium', FALSE);


-- USER COSMETICS (test data) 

-- Dylan (user 1) is male; he owns the male default set + hair, plus a couple of extras.
INSERT INTO User_Cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped) VALUES
    (1, 1, TRUE, TRUE),   -- White Tank (male, equipped)
    (1, 2, TRUE, TRUE),   -- Black Shorts (male, equipped)
    (1, 3, TRUE, TRUE),   -- White Socks (male, equipped)
    (1, 7, TRUE, TRUE),   -- Default Hair (equipped)
    (1, 8, TRUE, FALSE),  -- White Sneakers (male, owned, not worn - socks only by default)
    (1, 9, TRUE, FALSE);  -- Grey Sweatpants (male, owned, not equipped)


-- OUTFIT SLOTS (test data) - Dylan's currently equipped outfit
-- Wearing tank + shorts + socks (no shoes) by default.

INSERT INTO Outfit_Slot (user_id, top_id, bottom_id, shoes_id, socks_id, hair_id, accessories_id) VALUES
    (1, 1, 2, NULL, 3, 7, NULL);


-- STREAKS (test data) - Dylan's 2-week streak with XP multiplier

INSERT INTO Streak (user_id, week_st_date, num_workouts_completed, met_goal, multiplier) VALUES
    (1, '2026-08-25', 5, TRUE, 1.50);
