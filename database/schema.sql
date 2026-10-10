
-- REPS UP - Database Schema


DROP DATABASE IF EXISTS reps_up;
CREATE DATABASE reps_up;
USE reps_up;


-- RANK: tier system (Bronze I, Bronze II, Silver I, etc.)

CREATE TABLE ranks (
    rank_id INT AUTO_INCREMENT PRIMARY KEY,
    tier VARCHAR(20) NOT NULL,
    subtier VARCHAR(5) NOT NULL,
    min_lvl_required INT NOT NULL,
    UNIQUE KEY unique_rank (tier, subtier)
);


-- COSMETIC ITEM: items available in the locker

CREATE TABLE cosmetic_item (
    cosmetic_id INT AUTO_INCREMENT PRIMARY KEY,
    cosmetic_name VARCHAR(100) NOT NULL,
    cosmetic_type VARCHAR(30) NOT NULL,
    gender VARCHAR(10) NOT NULL DEFAULT 'unisex',
    is_colourable BOOLEAN NOT NULL DEFAULT FALSE,
    unlock_type VARCHAR(20) NOT NULL,
    lvl_required INT DEFAULT 0,
    coin_cost INT DEFAULT 0,
    cosmetic_img_data VARCHAR(250)
);


-- EXERCISE: pre-populated exercise library

CREATE TABLE exercise (
    exercise_id INT AUTO_INCREMENT PRIMARY KEY,
    exercise_name VARCHAR(100) NOT NULL,
    muscle_group VARCHAR(50) NOT NULL,
    default_sets INT DEFAULT 3,
    default_reps INT DEFAULT 12,
    difficulty VARCHAR(20) NOT NULL,
    base_xp INT NOT NULL DEFAULT 10
);


-- ADMIN: app administrators

CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_username VARCHAR(50) NOT NULL UNIQUE,
    admin_password VARCHAR(255) NOT NULL
);


-- USER: app users

CREATE TABLE user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    -- Stores a PBKDF2 "salt:hash" value (see PasswordHasher.java), not plain text.
    set_password VARCHAR(255) NOT NULL,
    xp INT NOT NULL DEFAULT 0,
    coins INT NOT NULL DEFAULT 0,
    current_level INT NOT NULL DEFAULT 1,
    rank_id INT,
    current_streak INT NOT NULL DEFAULT 0,
    avatar_id INT,
    measurment_system VARCHAR(10) DEFAULT 'metric',
    set_language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rank_id) REFERENCES ranks(rank_id) ON DELETE SET NULL
);


-- AVATAR: each user's avatar

CREATE TABLE avatar (
    avatar_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    skin_tone VARCHAR(20) NOT NULL,
    hair_colour VARCHAR(20) NOT NULL DEFAULT 'grey',
    body_shape VARCHAR(20) NOT NULL,
    facial_details VARCHAR(250),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);

-- Link user.avatar_id back to avatars

ALTER TABLE user
    ADD CONSTRAINT fk_user_avatar
    FOREIGN KEY (avatar_id) REFERENCES avatar(avatar_id) ON DELETE SET NULL;


-- USER PROFILE: fitness assessment data

CREATE TABLE user_profile (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    user_gender VARCHAR(20),
    user_age INT,
    user_weight DECIMAL(5,2),
    user_height DECIMAL(5,2),
    body_type VARCHAR(20),
    workout_lvl VARCHAR(20),
    num_workouts_per_week INT,
    fitness_goal VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


-- USER COSMETICS: which items each user owns/has equipped

CREATE TABLE user_cosmetic (
    user_cosmetic_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    cosmetic_id INT NOT NULL,
    is_unlocked BOOLEAN DEFAULT FALSE,
    is_equipped BOOLEAN DEFAULT FALSE,
    UNIQUE KEY unique_user_cosmetic (user_id, cosmetic_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (cosmetic_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE CASCADE
);


-- OUTFIT SLOTS: a user's currently equipped outfit

CREATE TABLE outfit_slot (
    outfit_slot_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    top_id INT,
    bottom_id INT,
    shoes_id INT,
    socks_id INT,
    hair_id INT,
    accessories_id INT,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (top_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL,
    FOREIGN KEY (bottom_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL,
    FOREIGN KEY (shoes_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL,
    FOREIGN KEY (socks_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL,
    FOREIGN KEY (hair_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL,
    FOREIGN KEY (accessories_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL
);

 
-- WORKOUTS: saved workouts (user-created OR admin defaults)

CREATE TABLE workout (
    workout_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    workout_name VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    workout_order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


-- WORKOUT EXERCISES: which exercises are in each workout

CREATE TABLE workout_exercise (
    workout_id INT NOT NULL,
    exercise_id INT NOT NULL,
    num_sets INT NOT NULL DEFAULT 3,
    num_reps INT NOT NULL DEFAULT 12,
    workout_exercise_order_index INT NOT NULL DEFAULT 0,
    target_weight DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (workout_id, exercise_id),
    FOREIGN KEY (workout_id) REFERENCES workout(workout_id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercise(exercise_id) ON DELETE CASCADE
);


-- SCHEDULED WORKOUTS: workouts placed on a user's calendar

CREATE TABLE scheduled_workout (
    scheduled_workout_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    workout_id INT NOT NULL,
    scheduled_date DATE NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_date TIMESTAMP NULL,
    earned_xp INT DEFAULT 0,
    completed_sets INT DEFAULT 0,
    completed_reps INT DEFAULT 0,
    series_id INT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workout(workout_id) ON DELETE CASCADE
);


-- GOALS: user-defined fitness goals

CREATE TABLE goal (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    goal_title VARCHAR(255) NOT NULL,
    goal_description TEXT,
    deadline DATE,
    goal_priority VARCHAR(20) NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


-- XP LOGS: history of XP earned (from workouts or goals)

CREATE TABLE xp_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount INT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_id INT NOT NULL,
    xp_earned INT NOT NULL,
    coins_earned INT DEFAULT 0,
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


-- COIN TRANSACTIONS: history of coin spending

CREATE TABLE coin_transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount INT NOT NULL,
    cosmetic_id INT,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (cosmetic_id) REFERENCES cosmetic_item(cosmetic_id) ON DELETE SET NULL
);


-- STREAKS: weekly streak tracking and XP multipliers

CREATE TABLE streak (
    streak_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    week_st_date DATE NOT NULL,
    num_workouts_completed INT DEFAULT 0,
    met_goal BOOLEAN DEFAULT FALSE,
    multiplier DECIMAL(3,2) DEFAULT 1.00,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


-- INDEXES: speed up the most common queries

CREATE INDEX idx_workouts_user ON workout(user_id);
CREATE INDEX idx_scheduled_user_date ON scheduled_workout(user_id, scheduled_date);
CREATE INDEX idx_scheduled_series ON scheduled_workout(series_id);
CREATE INDEX idx_goals_user ON goal(user_id);
CREATE INDEX idx_xp_logs_user ON xp_log(user_id);
CREATE INDEX idx_user_cosmetics_user ON user_cosmetic(user_id);
