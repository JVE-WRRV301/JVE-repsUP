
-- REPS UP - Database Schema


DROP DATABASE IF EXISTS reps_up;
CREATE DATABASE reps_up;
USE reps_up;


-- RANKS: tier system (Bronze I, Bronze II, Silver I, etc.)

CREATE TABLE ranks (
    rank_id INT AUTO_INCREMENT PRIMARY KEY,
    tier VARCHAR(20) NOT NULL,
    sub_tier VARCHAR(5) NOT NULL,
    min_lvl_req INT NOT NULL,
    UNIQUE KEY unique_rank (tier, sub_tier)
);


-- COSMETIC ITEMS: items available in the locker

CREATE TABLE cosmetic_items (
    cos_id INT AUTO_INCREMENT PRIMARY KEY,
    cos_name VARCHAR(100) NOT NULL,
    cos_type VARCHAR(30) NOT NULL,
    unlock_type VARCHAR(20) NOT NULL,
    lvl_req INT DEFAULT 0,
    coin_cost INT DEFAULT 0,
    cos_img_data VARCHAR(255)
);


-- EXERCISES: pre-populated exercise library

CREATE TABLE exercises (
    ex_id INT AUTO_INCREMENT PRIMARY KEY,
    ex_name VARCHAR(100) NOT NULL,
    muscle_group VARCHAR(50) NOT NULL,
    default_sets INT DEFAULT 3,
    default_reps INT DEFAULT 12,
    diff VARCHAR(20) NOT NULL,
    base_xp INT NOT NULL DEFAULT 10
);


-- ADMINS: app administrators

CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_username VARCHAR(50) NOT NULL UNIQUE,
    admin_password VARCHAR(255) NOT NULL
);


-- USERS: app users

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    xp INT NOT NULL DEFAULT 0,
    coins INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    rank_id INT,
    curr_streak INT NOT NULL DEFAULT 0,
    av_id INT,
    meas_system VARCHAR(10) DEFAULT 'metric',
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rank_id) REFERENCES ranks(rank_id) ON DELETE SET NULL
);


-- AVATARS: each user's avatar

CREATE TABLE avatars (
    av_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    skin_tone VARCHAR(20) NOT NULL,
    body_shape VARCHAR(20) NOT NULL,
    facial_details VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Now that both tables exist, link users.av_id back to avatars
ALTER TABLE users
    ADD CONSTRAINT fk_user_avatar
    FOREIGN KEY (av_id) REFERENCES avatars(av_id) ON DELETE SET NULL;


-- USER PROFILES: fitness assessment data

CREATE TABLE user_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    gender VARCHAR(20),
    age INT,
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    body_type VARCHAR(20),
    w_lvl VARCHAR(20),
    nr_w_per_week INT,
    fitness_goal VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- USER COSMETICS: which items each user owns/has equipped

CREATE TABLE user_cosmetics (
    user_cos_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    cos_id INT NOT NULL,
    is_unlocked BOOLEAN DEFAULT FALSE,
    is_equipped BOOLEAN DEFAULT FALSE,
    UNIQUE KEY unique_user_cosmetic (user_id, cos_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (cos_id) REFERENCES cosmetic_items(cos_id) ON DELETE CASCADE
);


-- OUTFIT SLOTS: a user's currently equipped outfit

CREATE TABLE outfit_slots (
    out_slot_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    top_id INT,
    bottom_id INT,
    shoes_id INT,
    acc_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (top_id) REFERENCES cosmetic_items(cos_id) ON DELETE SET NULL,
    FOREIGN KEY (bottom_id) REFERENCES cosmetic_items(cos_id) ON DELETE SET NULL,
    FOREIGN KEY (shoes_id) REFERENCES cosmetic_items(cos_id) ON DELETE SET NULL,
    FOREIGN KEY (acc_id) REFERENCES cosmetic_items(cos_id) ON DELETE SET NULL
);

 
-- WORKOUTS: saved workouts (user-created OR admin defaults)

CREATE TABLE workouts (
    w_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    w_name VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    w_order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- WORKOUT EXERCISES: which exercises are in each workout

CREATE TABLE workout_exercises (
    w_id INT NOT NULL,
    ex_id INT NOT NULL,
    sets INT NOT NULL DEFAULT 3,
    reps INT NOT NULL DEFAULT 12,
    w_ex_order_index INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (w_id, ex_id),
    FOREIGN KEY (w_id) REFERENCES workouts(w_id) ON DELETE CASCADE,
    FOREIGN KEY (ex_id) REFERENCES exercises(ex_id) ON DELETE CASCADE
);


-- SCHEDULED WORKOUTS: workouts placed on a user's calendar

CREATE TABLE scheduled_workouts (
    sched_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    w_id INT NOT NULL,
    sched_date DATE NOT NULL,
    is_comp BOOLEAN DEFAULT FALSE,
    comp_date TIMESTAMP NULL,
    earned_xp INT DEFAULT 0,
    comp_sets INT DEFAULT 0,
    comp_reps INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (w_id) REFERENCES workouts(w_id) ON DELETE CASCADE
);


-- GOALS: user-defined fitness goals

CREATE TABLE goals (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    description TEXT NOT NULL,
    deadline DATE,
    priority VARCHAR(20) NOT NULL,
    is_comp BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- XP LOGS: history of XP earned (from workouts or goals)

CREATE TABLE xp_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amt INT NOT NULL,
    src_type VARCHAR(20) NOT NULL,
    src_id INT NOT NULL,
    xp_earned INT NOT NULL,
    coins_earned INT DEFAULT 0,
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- COIN TRANSACTIONS: history of coin spending

CREATE TABLE coin_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amt INT NOT NULL,
    cos_id INT,
    time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (cos_id) REFERENCES cosmetic_items(cos_id) ON DELETE SET NULL
);


-- STREAKS: weekly streak tracking and XP multipliers

CREATE TABLE streaks (
    streak_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    week_st_date DATE NOT NULL,
    num_w_comp INT DEFAULT 0,
    met_goal BOOLEAN DEFAULT FALSE,
    multiplier DECIMAL(3,2) DEFAULT 1.00,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- INDEXES: speed up the most common queries

CREATE INDEX idx_workouts_user ON workouts(user_id);
CREATE INDEX idx_scheduled_user_date ON scheduled_workouts(user_id, sched_date);
CREATE INDEX idx_goals_user ON goals(user_id);
CREATE INDEX idx_xp_logs_user ON xp_logs(user_id);
CREATE INDEX idx_user_cosmetics_user ON user_cosmetics(user_id);
