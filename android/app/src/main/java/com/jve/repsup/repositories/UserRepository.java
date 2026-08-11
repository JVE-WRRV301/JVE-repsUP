package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.Rank;
import com.jve.repsup.models.User;

import java.sql.Timestamp;

/**
 * Repository class for User-related database operations.
 * Wraps SQL queries and delivers results via DatabaseCallback.
 */
public class UserRepository {

    /**
     * Authenticates a user by username and password.
     * Returns the User object on success, null if credentials don't match.
     */
    public void authenticate(String username, String password, ConnectionManager.DatabaseCallback<User> callback) {
        String sql = "SELECT * FROM User WHERE username = ? AND set_password = ?";
        ConnectionManager.querySingle(sql, new Object[]{username, password}, rs -> {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setName(rs.getString("first_name"));
            user.setEmail(rs.getString("email"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("set_password"));
            user.setXp(rs.getInt("xp"));
            user.setCoins(rs.getInt("coins"));
            user.setLevel(rs.getInt("current_level"));
            user.setRankId(rs.getObject("rank_id") != null ? rs.getInt("rank_id") : null);
            user.setCurrentStreak(rs.getInt("current_streak"));
            user.setAvatarId(rs.getObject("avatar_id") != null ? rs.getInt("avatar_id") : null);
            user.setMeasurementSystem(rs.getString("measurment_system"));
            user.setLanguage(rs.getString("set_language"));
            Timestamp ts = rs.getTimestamp("created_at");
            user.setCreatedAt(ts);
            return user;
        }, callback);
    }

    /**
     * Retrieves a user by their ID.
     */
    public void getUserById(int userId, ConnectionManager.DatabaseCallback<User> callback) {
        String sql = "SELECT * FROM User WHERE user_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setName(rs.getString("first_name"));
            user.setEmail(rs.getString("email"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("set_password"));
            user.setXp(rs.getInt("xp"));
            user.setCoins(rs.getInt("coins"));
            user.setLevel(rs.getInt("current_level"));
            user.setRankId(rs.getObject("rank_id") != null ? rs.getInt("rank_id") : null);
            user.setCurrentStreak(rs.getInt("current_streak"));
            user.setAvatarId(rs.getObject("avatar_id") != null ? rs.getInt("avatar_id") : null);
            user.setMeasurementSystem(rs.getString("measurment_system"));
            user.setLanguage(rs.getString("set_language"));
            Timestamp ts = rs.getTimestamp("created_at");
            user.setCreatedAt(ts);
            return user;
        }, callback);
    }

    /**
     * Retrieves a rank by its ID.
     */
    public void getRankById(int rankId, ConnectionManager.DatabaseCallback<Rank> callback) {
        String sql = "SELECT * FROM Ranks WHERE rank_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{rankId}, rs -> {
            Rank rank = new Rank();
            rank.setRankId(rs.getInt("rank_id"));
            rank.setTier(rs.getString("tier"));
            rank.setSubTier(rs.getString("subtier"));
            rank.setMinLevelRequired(rs.getInt("min_lvl_required"));
            return rank;
        }, callback);
    }

    /**
     * Retrieves an avatar by its ID.
     */
    public void getAvatarById(int avatarId, ConnectionManager.DatabaseCallback<Avatar> callback) {
        String sql = "SELECT * FROM Avatar WHERE avatar_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{avatarId}, rs -> {
            Avatar avatar = new Avatar();
            avatar.setAvatarId(rs.getInt("avatar_id"));
            avatar.setUserId(rs.getInt("user_id"));
            avatar.setSkinTone(rs.getString("skin_tone"));
            avatar.setBodyShape(rs.getString("body_shape"));
            avatar.setFacialDetails(rs.getString("facial_details"));
            return avatar;
        }, callback);
    }

    /**
     * Updates user XP and coins.
     */
    public void updateXpAndCoins(int userId, int xpDelta, int coinDelta, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE User SET xp = xp + ?, coins = coins + ? WHERE user_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{xpDelta, coinDelta, userId}, callback);
    }

    /**
     * Updates user level.
     */
    public void updateLevel(int userId, int newLevel, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE User SET current_level = ? WHERE user_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{newLevel, userId}, callback);
    }

    /**
     * Updates user rank.
     */
    public void updateRank(int userId, int newRankId, ConnectionManager.DatabaseCallback<Integer> callback) {
        String sql = "UPDATE User SET rank_id = ? WHERE user_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{newRankId, userId}, callback);
    }

    /**
     * Checks if a username is already taken.
     */
    public void isUsernameTaken(String username, ConnectionManager.DatabaseCallback<Boolean> callback) {
        String sql = "SELECT COUNT(*) as cnt FROM User WHERE username = ?";
        ConnectionManager.querySingle(sql, new Object[]{username}, rs -> rs.getInt("cnt") > 0, callback);
    }
}
