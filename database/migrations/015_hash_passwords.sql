-- Migration 015: hash stored passwords (Reps Up review item 7)
--
-- Passwords were stored in plain text and compared in SQL with
-- "WHERE username = ? AND set_password = ?", which (a) exposed every password
-- and (b) matched case-insensitively under MySQL's default collation. The app
-- now hashes passwords with PBKDF2WithHmacSHA256 and a random 16-byte salt,
-- storing them as a Base64 "salt:hash" string (see PasswordHasher.java), and
-- verifies them in Java after fetching the row by username only.
--
-- This migration:
--   1. Widens set_password (and admin_password, if not already wide) to hold the
--      longer "salt:hash" value.
--   2. Replaces the seed/test plain-text passwords with their hashed equivalents
--      so the existing demo accounts keep working after the switch to hashing.
--
-- The plain passwords behind these hashes (development only) are:
--   admin / Admin ....... admin123
--   dyllan12345 ......... Password1
--   sarahj .............. Password2
--   jaskrug ............. Password3
-- They are also listed in the README.
--
-- NOTE: real user accounts created on the OLD plain-text build will no longer be
-- able to log in after this migration, because their stored value isn't a
-- "salt:hash". That only affects locally-registered dev accounts; the seed/test
-- accounts below are updated here. Any such account should be re-registered.

USE reps_up;

-- 1. Widen the columns to hold the Base64 "salt:hash" value.
--    admin_password is already VARCHAR(255) in schema.sql; this MODIFY is a
--    harmless no-op there and corrects any older database still at a smaller size.
ALTER TABLE user   MODIFY COLUMN set_password   VARCHAR(255) NOT NULL;
ALTER TABLE admins MODIFY COLUMN admin_password VARCHAR(255) NOT NULL;

-- 2. Replace plain-text passwords with their PBKDF2 "salt:hash" equivalents.
--    Accounts are matched by username so no id numbers are relied on.
UPDATE admins SET admin_password = '5TiM4gPGPyDz7/ioNoCm1g==:a5lJJEC1iET8NRxBB/1IuNjoAzx/HmFq8GYpXPeMo3w='
    WHERE admin_username = 'Admin';

UPDATE user SET set_password = 'hpNFcW/IRXmzMCIgArGJkg==:3GzlcUiT+0iZfFSAEr8jOi7fQ07mmD9aJ71To27z4Ro='
    WHERE username = 'dyllan12345';
UPDATE user SET set_password = 'vthPYIU70FmDQmkJHEk4Lw==:vseAyU2qxplWT8ggyYsmbJ9IHpM8QL5RcXEp2RxYge0='
    WHERE username = 'sarahj';
UPDATE user SET set_password = 'ZKoWh3LUmuUxFkanuLWZRw==:1Fb77qBPqcSajwiFuwRl7ahZDtEglGEGzR/RDyQkDEU='
    WHERE username = 'jaskrug';
