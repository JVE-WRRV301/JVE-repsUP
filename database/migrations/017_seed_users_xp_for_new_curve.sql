-- Migration 017: raise the seed users' XP to match the new levelling curve.
--
-- The levelling curve changed to: XP to go from level N to N+1 = 400 + 50*N,
-- so the cumulative XP to reach level L is (L-1) * (400 + 25*L). The three
-- development seed users kept their original levels and ranks, so their stored
-- XP must be raised to a value that is consistent with those levels under the
-- new curve. A fresh setup already gets the right values from seed.sql; this
-- migration brings an EXISTING seeded database up to the same state.
--
-- Users are matched by username. Each one gets the exact xp/level/rank set in
-- seed.sql. Coins and streaks are left untouched.
--
--   dyllan12345 -> xp 6000,  level 10, rank_id 3 (Bronze III, needs 5850 XP)
--   sarahj      -> xp 1000,  level 3,  rank_id 1 (Bronze I,   needs 950 XP)
--   jaskrug     -> xp 15000, level 18, rank_id 4 (Silver I,   needs 14450 XP)

USE reps_up;

UPDATE user SET xp = 6000,  current_level = 10, rank_id = 3 WHERE username = 'dyllan12345';
UPDATE user SET xp = 1000,  current_level = 3,  rank_id = 1 WHERE username = 'sarahj';
UPDATE user SET xp = 15000, current_level = 18, rank_id = 4 WHERE username = 'jaskrug';
