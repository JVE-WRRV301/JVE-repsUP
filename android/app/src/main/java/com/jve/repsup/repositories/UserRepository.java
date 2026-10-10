package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Rank;
import com.jve.repsup.models.User;
import com.jve.repsup.utils.PasswordHasher;

/**
 * Repository handling User table CRUD operations.
 * All methods execute on background threads via ConnectionManager.
 */
public class UserRepository {

    /**
     * Authenticates a user by username and password.
     *
     * The user is fetched by username only, then the supplied password is
     * verified in Java against the stored PBKDF2 "salt:hash" value. This keeps
     * the password out of the SQL comparison and makes the check case-sensitive
     * (unlike MySQL's default collation).
     *
     * Returns the User object on success, null if the username is unknown or the
     * password does not match.
     */
    public void authenticate(String username, String password, ConnectionManager.DatabaseCallback<User> callback) {
        String sql = "SELECT user_id, first_name, email, username, set_password, xp, coins, " +
                "current_level, rank_id, current_streak, avatar_id, measurment_system, set_language " +
                "FROM user WHERE username = ?";

        ConnectionManager.querySingle(sql, new Object[]{username}, rs -> {
            String storedPassword = rs.getString("set_password");
            // Verify in Java; return null for a non-match so the UI treats it
            // exactly like "no such user" (invalid credentials).
            if (!PasswordHasher.verify(password, storedPassword)) {
                return null;
            }
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setEmail(rs.getString("email"));
            user.setUsername(rs.getString("username"));
            user.setPassword(storedPassword);
            user.setXp(rs.getInt("xp"));
            user.setCoins(rs.getInt("coins"));
            user.setCurrentLevel(rs.getInt("current_level"));
            user.setRankId(rs.getInt("rank_id"));
            user.setCurrentStreak(rs.getInt("current_streak"));
            user.setAvatarId(rs.getInt("avatar_id"));
            user.setMeasurementSystem(rs.getString("measurment_system"));
            user.setLanguage(rs.getString("set_language"));
            return user;
        }, callback);
    }

    /**
     * Registers a new user with default values (xp=0, coins=0, level=1, rank_id=1, streak=0).
     * The password is hashed (PBKDF2 "salt:hash") before being stored.
     * Returns the generated user_id on success.
     */
    public void register(User user, ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO user (first_name, email, username, set_password, xp, coins, " +
                "current_level, rank_id, current_streak) VALUES (?, ?, ?, ?, 0, 0, 1, 1, 0)";

        ConnectionManager.executeInsert(sql, new Object[]{
                user.getFirstName(),
                user.getEmail(),
                user.getUsername(),
                PasswordHasher.hash(user.getPassword())
        }, callback);
    }

    /**
     * Checks if a username already exists in the User table.
     * Returns true if taken, false if available.
     */
    public void isUsernameTaken(String username, ConnectionManager.DatabaseCallback<Boolean> callback) {
        String sql = "SELECT COUNT(*) AS cnt FROM user WHERE username = ?";

        ConnectionManager.querySingle(sql, new Object[]{username}, rs -> rs.getInt("cnt") > 0, callback);
    }

    /**
     * Checks whether the given email is already registered to a user.
     * The email column is UNIQUE in the schema; this lets the UI report a
     * duplicate-email conflict specifically instead of a generic failure.
     * Returns true if taken, false if available.
     */
    public void isEmailTaken(String email, ConnectionManager.DatabaseCallback<Boolean> callback) {
        String sql = "SELECT COUNT(*) AS cnt FROM user WHERE email = ?";

        ConnectionManager.querySingle(sql, new Object[]{email}, rs -> rs.getInt("cnt") > 0, callback);
    }

    /**
     * Updates the user's XP by adding the specified delta amount.
     * Returns the number of affected rows.
     */
    public void updateXp(int userId, int xpDelta, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET xp = xp + ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{xpDelta, userId}, callback);
    }

    /**
     * Updates the user's current streak to the specified value.
     * Returns the number of affected rows.
     */
    public void updateStreak(int userId, int newStreak, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET current_streak = ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{newStreak, userId}, callback);
    }

    /**
     * Retrieves the user's current streak from the database.
     * Returns the streak count, or 0 if the user is not found.
     */
    public void getCurrentStreak(int userId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "SELECT current_streak FROM user WHERE user_id = ?";

        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> rs.getInt("current_streak"), callback);
    }

    /**
     * Inserts a new XP_Log record for tracking XP earnings.
     * Returns the generated log_id.
     */
    public void insertXpLog(int userId, int amount, String sourceType, int sourceId,
                            int xpEarned, int coinsEarned,
                            ConnectionManager.DatabaseCallback<Long> callback) {
        String sql = "INSERT INTO xp_log (user_id, amount, source_type, source_id, " +
                "xp_earned, coins_earned, time_stamp) VALUES (?, ?, ?, ?, ?, ?, NOW())";

        ConnectionManager.executeInsert(sql, new Object[]{
                userId, amount, sourceType, sourceId, xpEarned, coinsEarned
        }, callback);
    }

    /**
     * Updates the user's XP and coins simultaneously by adding the specified deltas.
     * Returns the number of affected rows.
     *
     * @param userId    the user's ID
     * @param xpDelta   XP amount to add (can be 0)
     * @param coinDelta coin amount to add (can be 0)
     * @param callback  receives the number of affected rows or error
     */
    public void updateXpAndCoins(int userId, int xpDelta, int coinDelta,
                                 ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET xp = xp + ?, coins = coins + ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{xpDelta, coinDelta, userId}, callback);
    }

    /**
     * Retrieves a full user record by user_id, including the rank display name.
     *
     * @param userId   the user's ID
     * @param callback receives the User object or null if not found
     */
    public void getUserById(int userId, ConnectionManager.DatabaseCallback<User> callback) {
        String sql = "SELECT u.user_id, u.first_name, u.email, u.username, u.set_password, u.xp, u.coins, " +
                "u.current_level, u.rank_id, u.current_streak, u.avatar_id, u.measurment_system, u.set_language, " +
                "CONCAT(r.tier, ' ', r.subtier) AS rank_name " +
                "FROM user u LEFT JOIN ranks r ON u.rank_id = r.rank_id WHERE u.user_id = ?";

        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setEmail(rs.getString("email"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("set_password"));
            user.setXp(rs.getInt("xp"));
            user.setCoins(rs.getInt("coins"));
            user.setCurrentLevel(rs.getInt("current_level"));
            user.setRankId(rs.getInt("rank_id"));
            user.setCurrentStreak(rs.getInt("current_streak"));
            user.setAvatarId(rs.getInt("avatar_id"));
            user.setMeasurementSystem(rs.getString("measurment_system"));
            user.setLanguage(rs.getString("set_language"));
            user.setRankName(rs.getString("rank_name"));
            return user;
        }, callback);
    }

    /**
     * Updates the user's current level to the specified value.
     * Returns the number of affected rows.
     *
     * @param userId   the user's ID
     * @param newLevel the new level to set
     * @param callback receives the number of affected rows or error
     */
    public void updateLevel(int userId, int newLevel, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET current_level = ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{newLevel, userId}, callback);
    }

    /**
     * Updates the user's coins by adding the specified delta amount.
     * Returns the number of affected rows.
     *
     * @param userId    the user's ID
     * @param coinDelta coin amount to add
     * @param callback  receives the number of affected rows or error
     */
    public void updateCoins(int userId, int coinDelta, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET coins = coins + ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{coinDelta, userId}, callback);
    }

    /**
     * Updates the user's rank to the specified rank ID.
     * Returns the number of affected rows.
     *
     * @param userId    the user's ID
     * @param newRankId the new rank ID to set
     * @param callback  receives the number of affected rows or error
     */
    public void updateRank(int userId, int newRankId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET rank_id = ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{newRankId, userId}, callback);
    }

    /**
     * Updates the user's measurement system preference.
     * Returns the number of affected rows.
     *
     * @param userId   the user's ID
     * @param system   the measurement system to set ("Metric" or "Imperial")
     * @param callback receives the number of affected rows or error
     */
    public void updateMeasurementSystem(int userId, String system, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET measurment_system = ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{system, userId}, callback);
    }

    /**
     * Updates the user's personal details (name, email, username).
     * Returns the number of affected rows.
     *
     * @param userId    the user's ID
     * @param firstName the new first name
     * @param email     the new email
     * @param username  the new username
     * @param callback  receives the number of affected rows or error
     */
    public void updatePersonalDetails(int userId, String firstName, String email, String username,
                                      ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE user SET first_name = ?, email = ?, username = ? WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{firstName, email, username, userId}, callback);
    }

    /**
     * Changes the user's password after verifying the current password matches.
     *
     * Passwords are stored as PBKDF2 "salt:hash" values with a per-password
     * random salt, so the current password can't be checked in SQL. Instead the
     * stored hash is fetched, the current password is verified in Java, and only
     * on a match is a freshly hashed new password written.
     *
     * Returns the number of affected rows (1 on success, 0 if the current password
     * did not match so no row was updated).
     *
     * @param userId          the user's ID
     * @param currentPassword the user's current password (for verification)
     * @param newPassword     the new password to set
     * @param callback        receives rows affected (0 means current password wrong)
     */
    public void changePassword(int userId, String currentPassword, String newPassword,
                               ConnectionManager.DatabaseCallback<Integer> callback) {
        String selectSql = "SELECT set_password FROM user WHERE user_id = ?";

        ConnectionManager.querySingle(selectSql, new Object[]{userId}, rs -> rs.getString("set_password"),
                new ConnectionManager.DatabaseCallback<String>() {
                    @Override
                    public void onSuccess(String storedPassword) {
                        if (!PasswordHasher.verify(currentPassword, storedPassword)) {
                            // Current password wrong: report 0 rows affected, same
                            // as the old WHERE-clause behaviour.
                            if (callback != null) {
                                callback.onSuccess(0);
                            }
                            return;
                        }
                        String updateSql = "UPDATE user SET set_password = ? WHERE user_id = ?";
                        ConnectionManager.executeUpdate(updateSql,
                                new Object[]{PasswordHasher.hash(newPassword), userId}, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                });
    }

    /**
     * Permanently deletes the user's account.
     * Returns the number of affected rows (1 on success).
     *
     * WARNING: this is irreversible. Callers must confirm with the user first.
     * Related rows in child tables are expected to be removed via ON DELETE CASCADE
     * foreign keys defined in the schema.
     *
     * @param userId   the user's ID
     * @param callback receives the number of affected rows or error
     */
    public void deleteAccount(int userId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "DELETE FROM user WHERE user_id = ?";

        ConnectionManager.executeUpdate(sql, new Object[]{userId}, callback);
    }

    /**
     * Queries the next eligible rank for the given level and current rank.
     * Returns the next rank where min_lvl_required <= currentLevel and rank_id > currentRankId,
     * or null if no promotion is available.
     *
     * @param currentLevel  the user's current level
     * @param currentRankId the user's current rank ID
     * @param callback      receives the next Rank or null
     */
    public void getNextRank(int currentLevel, int currentRankId, ConnectionManager.DatabaseCallback<Rank> callback) {
        String sql = "SELECT rank_id, tier, subtier, min_lvl_required FROM ranks " +
                "WHERE min_lvl_required <= ? AND rank_id > ? ORDER BY rank_id ASC LIMIT 1";

        ConnectionManager.querySingle(sql, new Object[]{currentLevel, currentRankId}, rs -> {
            Rank rank = new Rank();
            rank.setRankId(rs.getInt("rank_id"));
            rank.setTier(rs.getString("tier"));
            rank.setSubTier(rs.getString("subtier"));
            rank.setMinLevelRequired(rs.getInt("min_lvl_required"));
            return rank;
        }, callback);
    }

    /**
     * Reads the workout level chosen in the fitness assessment ("never",
     * "beginner", "intermediate" or "advanced"). Returns null if the user has
     * no profile row.
     */
    public void getWorkoutLevel(int userId, ConnectionManager.DatabaseCallback<String> callback) {
        ConnectionManager.querySingle(
                "SELECT workout_lvl FROM user_profile WHERE user_id = ?",
                new Object[]{userId},
                rs -> rs.getString("workout_lvl"),
                callback);
    }
}
