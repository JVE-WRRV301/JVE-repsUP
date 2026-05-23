-- REPS UP - Seed Data


USE reps_up;


-- RANKS (required) - the full progression ladder

INSERT INTO ranks (tier, sub_tier, min_lvl_req) VALUES
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


-- EXERCISES (required) - matches the muscle groups shown in your UI

INSERT INTO exercises (ex_name, muscle_group, default_sets, default_reps, diff, base_xp) VALUES
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

INSERT INTO cosmetic_items (cos_name, cos_type, unlock_type, lvl_req, coin_cost, cos_img_data) VALUES
    -- Default items (free, equipped on registration)
    ('Basic White Tank',  'top',      'default', 0,  0,  'assets/tops/white_tank.png'),
    ('Basic Black Shorts','bottom',   'default', 0,  0,  'assets/bottoms/black_shorts.png'),
    ('White Sneakers',    'shoes',    'default', 0,  0,  'assets/shoes/white_sneakers.png'),
    -- Coin-unlocked items
    ('Pink T-Shirt',      'top',      'coins',   0,  50, 'assets/tops/pink_tee.png'),
    ('Navy Hoodie',       'top',      'coins',   0,  50, 'assets/tops/navy_hoodie.png'),
    ('Grey Hoodie',       'top',      'coins',   0,  50, 'assets/tops/grey_hoodie.png'),
    ('Long Sleeve White', 'top',      'coins',   0,  50, 'assets/tops/longsleeve_white.png'),
    ('Red Headband',      'accessory','coins',   0,  30, 'assets/accessories/red_headband.png'),
    -- Level-locked items
    ('Champion Tank',     'top',      'level',   20, 0,  'assets/tops/champion_tank.png'),
    ('Diamond Sneakers',  'shoes',    'level',   60, 0,  'assets/shoes/diamond_sneakers.png');


-- ADMIN ACCOUNT (required) - so admins can create default workouts
-- NOTE: password is "admin123" hashed with bcrypt. Replace with a real hash in production.

INSERT INTO admins (admin_username, admin_password) VALUES
    ('dean', '$2a$10$placeholder_hash_replace_in_real_app');


-- TEST USERS (development only)
-- NOTE: passwords are placeholders. Real passwords must be hashed.

INSERT INTO users (name, email, username, password, xp, coins, level, rank_id, curr_streak) VALUES
    ('Dylan Smith',  'dylan@example.com',  'dyllan12345', 'hash_placeholder_1', 450, 100, 10, 3, 14),
    ('Sarah Jones',  'sarah@example.com',  'sarahj',      'hash_placeholder_2', 120, 50,  3,  1, 2),
    ('Mike Brown',   'mike@example.com',   'mikeb',       'hash_placeholder_3', 800, 200, 18, 4, 21);


-- USER PROFILES (test data) - fitness assessment results

INSERT INTO user_profiles (user_id, gender, age, weight, height, body_type, w_lvl, nr_w_per_week, fitness_goal) VALUES
    (1, 'Male',   25, 80.00,  185.00, 'athletic', 'intermediate', 5, 'build_muscle'),
    (2, 'Female', 28, 62.50,  168.00, 'slim',     'beginner',     3, 'get_fitter'),
    (3, 'Male',   31, 75.00,  178.00, 'athletic', 'advanced',     6, 'build_muscle');


-- AVATARS (test data) - one per user

INSERT INTO avatars (user_id, skin_tone, body_shape, facial_details) VALUES
    (1, 'medium', 'athletic', 'short_brown_hair'),
    (2, 'light',  'slim',     'long_blonde_hair'),
    (3, 'tan',    'athletic', 'short_black_hair');

-- Link each user back to their avatar
UPDATE users SET av_id = 1 WHERE user_id = 1;
UPDATE users SET av_id = 2 WHERE user_id = 2;
UPDATE users SET av_id = 3 WHERE user_id = 3;


-- WORKOUTS (mix of admin defaults + user-created)

-- Default workouts (admin-created, user_id is NULL)
INSERT INTO workouts (user_id, w_name, is_default) VALUES
    (NULL, 'Push Day',                 TRUE),
    (NULL, 'Pull Day',                 TRUE),
    (NULL, 'Leg Day',                  TRUE),
    (NULL, 'Core Burner',              TRUE);

-- User-created workouts
INSERT INTO workouts (user_id, w_name, is_default) VALUES
    (1, 'Chest, Shoulders and Triceps', FALSE),
    (1, 'Back and Biceps',              FALSE),
    (1, 'Glutes and Hamstrings',        FALSE),
    (2, 'Beginner Full Body',           FALSE);


-- WORKOUT EXERCISES - what's in each workout

-- Push Day (workout 1)
INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index) VALUES
    (1, 1, 3, 10, 1),  -- Bench Press
    (1, 4, 3, 12, 2),  -- Dumbbell Shoulder Press
    (1, 6, 3, 12, 3);  -- Tricep Pushdown

-- Dylan's "Chest, Shoulders and Triceps" workout (workout 5)
INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index) VALUES
    (5, 1, 3, 10, 1),  -- Bench Press
    (5, 4, 3, 12, 2),  -- Dumbbell Shoulder Press
    (5, 6, 3, 12, 3);  -- Tricep Pushdown

-- Dylan's "Glutes and Hamstrings" workout (workout 7)
INSERT INTO workout_exercises (w_id, ex_id, sets, reps, w_ex_order_index) VALUES
    (7, 12, 3, 10, 1), -- Barbell Hip Thrusts
    (7, 18, 3, 10, 2), -- Barbell RDLs
    (7, 19, 3, 12, 3); -- Hamstring Extension


-- SCHEDULED WORKOUTS (test data) - calendar entries for Dylan

INSERT INTO scheduled_workouts (user_id, w_id, sched_date, is_comp, earned_xp) VALUES
    (1, 5, '2026-09-04', FALSE, 0),
    (1, 7, '2026-09-02', TRUE,  90),
    (1, 5, '2026-09-11', FALSE, 0);


-- GOALS (test data)

INSERT INTO goals (user_id, description, deadline, priority, is_comp) VALUES
    (1, 'Move up a weight in bicep curls. I want to move up from 18kgs to 20kgs within 2 weeks.', '2026-09-15', 'medium', FALSE),
    (1, 'Bench press my body weight',                                                              '2026-09-30', 'top',    FALSE),
    (2, 'Complete 3 workouts a week for a month',                                                  '2026-10-01', 'medium', FALSE);


-- USER COSMETICS (test data) - Dylan owns the defaults + has unlocked the pink tee

INSERT INTO user_cosmetics (user_id, cos_id, is_unlocked, is_equipped) VALUES
    (1, 1, TRUE, TRUE),   -- Basic White Tank (equipped)
    (1, 2, TRUE, TRUE),   -- Basic Black Shorts (equipped)
    (1, 3, TRUE, TRUE),   -- White Sneakers (equipped)
    (1, 4, TRUE, FALSE);  -- Pink T-Shirt (unlocked but not equipped)


-- OUTFIT SLOTS (test data) - Dylan's currently equipped outfit

INSERT INTO outfit_slots (user_id, top_id, bottom_id, shoes_id, acc_id) VALUES
    (1, 1, 2, 3, NULL);


-- STREAKS (test data) - Dylan's 2-week streak with XP multiplier

INSERT INTO streaks (user_id, week_st_date, num_w_comp, met_goal, multiplier) VALUES
    (1, '2026-08-25', 5, TRUE, 1.50);
