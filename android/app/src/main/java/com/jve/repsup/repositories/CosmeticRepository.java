package com.jve.repsup.repositories;

import com.jve.repsup.database.ConnectionManager;
import com.jve.repsup.database.ConnectionManager.DatabaseCallback;
import com.jve.repsup.models.Avatar;
import com.jve.repsup.models.CosmeticItem;
import com.jve.repsup.models.OutfitSlot;
import com.jve.repsup.models.UserCosmetic;

import java.util.List;

/**
 * Repository for cosmetic item operations.
 * Wraps SQL queries against Cosmetic_Item, User_Cosmetic, and Outfit_Slot tables.
 */
public class CosmeticRepository {

    /**
     * Gets the user's avatar record from the database.
     */
    public void getAvatar(int userId, DatabaseCallback<Avatar> callback) {
        String sql = "SELECT avatar_id, user_id, skin_tone, body_shape, facial_details " +
                "FROM avatar WHERE user_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
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
     * Gets all cosmetic items (both owned and locked) for the user.
     */
    public void getUserCosmetics(int userId, DatabaseCallback<List<CosmeticItem>> callback) {
        String sql = "SELECT ci.cosmetic_id, ci.cosmetic_name, ci.cosmetic_type, " +
                "ci.unlock_type, ci.lvl_required, ci.coin_cost, ci.cosmetic_img_data " +
                "FROM Cosmetic_Item ci";
        ConnectionManager.queryList(sql, null, rs -> {
            CosmeticItem item = new CosmeticItem();
            item.setCosmeticId(rs.getInt("cosmetic_id"));
            item.setName(rs.getString("cosmetic_name"));
            item.setType(rs.getString("cosmetic_type"));
            item.setUnlockType(rs.getString("unlock_type"));
            item.setLevelRequired(rs.getInt("lvl_required"));
            item.setCoinCost(rs.getInt("coin_cost"));
            item.setImageData(rs.getString("cosmetic_img_data"));
            return item;
        }, callback);
    }

    /**
     * Gets only cosmetics the user has unlocked (owned).
     */
    public void getOwnedCosmetics(int userId, DatabaseCallback<List<CosmeticItem>> callback) {
        String sql = "SELECT ci.cosmetic_id, ci.cosmetic_name, ci.cosmetic_type, " +
                "ci.unlock_type, ci.lvl_required, ci.coin_cost, ci.cosmetic_img_data " +
                "FROM Cosmetic_Item ci " +
                "INNER JOIN User_Cosmetic uc ON ci.cosmetic_id = uc.cosmetic_id " +
                "WHERE uc.user_id = ? AND uc.is_unlocked = 1";
        ConnectionManager.queryList(sql, new Object[]{userId}, rs -> {
            CosmeticItem item = new CosmeticItem();
            item.setCosmeticId(rs.getInt("cosmetic_id"));
            item.setName(rs.getString("cosmetic_name"));
            item.setType(rs.getString("cosmetic_type"));
            item.setUnlockType(rs.getString("unlock_type"));
            item.setLevelRequired(rs.getInt("lvl_required"));
            item.setCoinCost(rs.getInt("coin_cost"));
            item.setImageData(rs.getString("cosmetic_img_data"));
            return item;
        }, callback);
    }

    /**
     * Gets cosmetics the user does NOT own (locked).
     */
    public void getLockedCosmetics(int userId, DatabaseCallback<List<CosmeticItem>> callback) {
        String sql = "SELECT ci.cosmetic_id, ci.cosmetic_name, ci.cosmetic_type, " +
                "ci.unlock_type, ci.lvl_required, ci.coin_cost, ci.cosmetic_img_data " +
                "FROM Cosmetic_Item ci " +
                "WHERE ci.cosmetic_id NOT IN " +
                "(SELECT uc.cosmetic_id FROM User_Cosmetic uc WHERE uc.user_id = ? AND uc.is_unlocked = 1)";
        ConnectionManager.queryList(sql, new Object[]{userId}, rs -> {
            CosmeticItem item = new CosmeticItem();
            item.setCosmeticId(rs.getInt("cosmetic_id"));
            item.setName(rs.getString("cosmetic_name"));
            item.setType(rs.getString("cosmetic_type"));
            item.setUnlockType(rs.getString("unlock_type"));
            item.setLevelRequired(rs.getInt("lvl_required"));
            item.setCoinCost(rs.getInt("coin_cost"));
            item.setImageData(rs.getString("cosmetic_img_data"));
            return item;
        }, callback);
    }

    /**
     * Purchases a cosmetic item for the user.
     * Executes sequentially: deduct coins → insert Coin_Transaction → unlock cosmetic.
     * If the user has insufficient coins, the UPDATE affects 0 rows and the callback receives an error.
     *
     * @param userId     the user's ID
     * @param cosmeticId the cosmetic item's ID
     * @param coinCost   the cost of the item in coins
     * @param callback   receives 1 on success or error if insufficient coins or DB failure
     */
    public void purchaseCosmetic(int userId, int cosmeticId, int coinCost,
                                 DatabaseCallback<Integer> callback) {
        // Step 1: Deduct coins (only if sufficient balance)
        String deductSql = "UPDATE User SET coins = coins - ? WHERE user_id = ? AND coins >= ?";
        ConnectionManager.executeUpdate(deductSql, new Object[]{coinCost, userId, coinCost},
                new DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer rowsAffected) {
                        if (rowsAffected == null || rowsAffected == 0) {
                            callback.onError(new Exception("Insufficient coins"));
                            return;
                        }
                        // Step 2: Insert Coin_Transaction record
                        insertCoinTransaction(userId, cosmeticId, coinCost, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    private void insertCoinTransaction(int userId, int cosmeticId, int coinCost,
                                       DatabaseCallback<Integer> callback) {
        String insertSql = "INSERT INTO Coin_Transaction (user_id, amount, cosmetic_id) VALUES (?, ?, ?)";
        ConnectionManager.executeInsert(insertSql, new Object[]{userId, -coinCost, cosmeticId},
                new DatabaseCallback<Long>() {
                    @Override
                    public void onSuccess(Long generatedId) {
                        // Step 3: Mark cosmetic as unlocked
                        unlockCosmetic(userId, cosmeticId, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Unlocks a cosmetic for the user by inserting/updating User_Cosmetic row.
     */
    public void unlockCosmetic(int userId, int cosmeticId, DatabaseCallback<Integer> callback) {
        String sql = "INSERT INTO User_Cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped) " +
                "VALUES (?, ?, 1, 0) " +
                "ON DUPLICATE KEY UPDATE is_unlocked = 1";
        ConnectionManager.executeUpdate(sql, new Object[]{userId, cosmeticId}, callback);
    }

    /**
     * Equips a cosmetic for the user (sets is_equipped = 1).
     */
    public void equipCosmetic(int userId, int cosmeticId, DatabaseCallback<Integer> callback) {
        String sql = "UPDATE User_Cosmetic SET is_equipped = 1 " +
                "WHERE user_id = ? AND cosmetic_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{userId, cosmeticId}, callback);
    }

    /**
     * Unequips all cosmetics of a given type for the user.
     */
    public void unequipByType(int userId, String cosmeticType, DatabaseCallback<Integer> callback) {
        String sql = "UPDATE User_Cosmetic uc " +
                "INNER JOIN Cosmetic_Item ci ON uc.cosmetic_id = ci.cosmetic_id " +
                "SET uc.is_equipped = 0 " +
                "WHERE uc.user_id = ? AND ci.cosmetic_type = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{userId, cosmeticType}, callback);
    }

    /**
     * Gets the user's current outfit slot.
     */
    public void getOutfitSlot(int userId, DatabaseCallback<OutfitSlot> callback) {
        String sql = "SELECT outfit_slot_id, user_id, top_id, bottom_id, shoes_id, socks_id, accessories_id " +
                "FROM Outfit_Slot WHERE user_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> {
            OutfitSlot slot = new OutfitSlot();
            slot.setOutfitSlotId(rs.getInt("outfit_slot_id"));
            slot.setUserId(rs.getInt("user_id"));
            slot.setTopId(rs.getObject("top_id") != null ? rs.getInt("top_id") : null);
            slot.setBottomId(rs.getObject("bottom_id") != null ? rs.getInt("bottom_id") : null);
            slot.setShoesId(rs.getObject("shoes_id") != null ? rs.getInt("shoes_id") : null);
            slot.setSocksId(rs.getObject("socks_id") != null ? rs.getInt("socks_id") : null);
            slot.setAccessoriesId(rs.getObject("accessories_id") != null ? rs.getInt("accessories_id") : null);
            return slot;
        }, callback);
    }

    /**
     * Saves (inserts or updates) the outfit slot for the user.
     */
    public void saveOutfitSlot(OutfitSlot slot, DatabaseCallback<Integer> callback) {
        String sql = "INSERT INTO Outfit_Slot (user_id, top_id, bottom_id, shoes_id, socks_id, accessories_id) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE top_id = VALUES(top_id), bottom_id = VALUES(bottom_id), " +
                "shoes_id = VALUES(shoes_id), socks_id = VALUES(socks_id), " +
                "accessories_id = VALUES(accessories_id)";
        ConnectionManager.executeUpdate(sql, new Object[]{
                slot.getUserId(), slot.getTopId(), slot.getBottomId(),
                slot.getShoesId(), slot.getSocksId(), slot.getAccessoriesId()
        }, callback);
    }

    /**
     * Gets the list of equipped cosmetic IDs for the user.
     */
    public void getEquippedCosmetics(int userId, DatabaseCallback<List<UserCosmetic>> callback) {
        String sql = "SELECT user_cosmetic_id, user_id, cosmetic_id, is_unlocked, is_equipped " +
                "FROM User_Cosmetic WHERE user_id = ? AND is_equipped = 1";
        ConnectionManager.queryList(sql, new Object[]{userId}, rs -> {
            UserCosmetic uc = new UserCosmetic();
            uc.setUserCosmeticId(rs.getInt("user_cosmetic_id"));
            uc.setUserId(rs.getInt("user_id"));
            uc.setCosmeticId(rs.getInt("cosmetic_id"));
            uc.setUnlocked(rs.getBoolean("is_unlocked"));
            uc.setEquipped(rs.getBoolean("is_equipped"));
            return uc;
        }, callback);
    }

    /**
     * Saves all equipped items: unequips all, then equips selected ones, then saves outfit slot.
     */
    public void saveAllEquipped(int userId, List<CosmeticItem> equippedItems,
                                DatabaseCallback<Integer> callback) {
        // First unequip all items for this user
        String unequipSql = "UPDATE User_Cosmetic SET is_equipped = 0 WHERE user_id = ?";
        ConnectionManager.executeUpdate(unequipSql, new Object[]{userId},
                new DatabaseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        // Now equip selected items
                        equipSelectedItems(userId, equippedItems, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    private void equipSelectedItems(int userId, List<CosmeticItem> equippedItems,
                                    DatabaseCallback<Integer> callback) {
        if (equippedItems == null || equippedItems.isEmpty()) {
            // Save empty outfit slot
            OutfitSlot slot = new OutfitSlot();
            slot.setUserId(userId);
            saveOutfitSlot(slot, callback);
            return;
        }

        // Build outfit slot from equipped items
        OutfitSlot slot = new OutfitSlot();
        slot.setUserId(userId);

        for (CosmeticItem item : equippedItems) {
            if (item.getType() == null) {
                continue;
            }
            switch (item.getType().toLowerCase()) {
                case "top":
                    slot.setTopId(item.getCosmeticId());
                    break;
                case "bottom":
                    slot.setBottomId(item.getCosmeticId());
                    break;
                case "shoes":
                    slot.setShoesId(item.getCosmeticId());
                    break;
                case "socks":
                    slot.setSocksId(item.getCosmeticId());
                    break;
                case "accessory":
                case "accessories":
                    slot.setAccessoriesId(item.getCosmeticId());
                    break;
                default:
                    // hair, face, and other non-outfit-slot types are tracked
                    // in User_Cosmetic (is_equipped) but not in Outfit_Slot.
                    break;
            }
        }

        // Equip each item in User_Cosmetic table
        final int[] remaining = {equippedItems.size()};
        for (CosmeticItem item : equippedItems) {
            equipCosmetic(userId, item.getCosmeticId(), new DatabaseCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        // All equipped, now save outfit slot
                        saveOutfitSlot(slot, callback);
                    }
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        }
    }

    /**
     * Resolves the user's equipped outfit slots into full CosmeticItem objects.
     *
     * Joins Outfit_Slot (the single source of truth for what is rendered) against
     * Cosmetic_Item for each of the four slots, so the compositor receives the
     * actual equipped items with their image paths. Slots that are empty simply
     * produce no row for that type.
     *
     * @param userId   the user's ID
     * @param callback receives the list of equipped CosmeticItems (may be empty) or error
     */
    public void getEquippedItemsForOutfit(int userId, DatabaseCallback<List<CosmeticItem>> callback) {
        String sql = "SELECT ci.cosmetic_id, ci.cosmetic_name, ci.cosmetic_type, " +
                "ci.unlock_type, ci.lvl_required, ci.coin_cost, ci.cosmetic_img_data " +
                "FROM Outfit_Slot os " +
                "INNER JOIN Cosmetic_Item ci ON ci.cosmetic_id IN " +
                "(os.top_id, os.bottom_id, os.shoes_id, os.socks_id, os.accessories_id) " +
                "WHERE os.user_id = ?";
        ConnectionManager.queryList(sql, new Object[]{userId}, rs -> {
            CosmeticItem item = new CosmeticItem();
            item.setCosmeticId(rs.getInt("cosmetic_id"));
            item.setName(rs.getString("cosmetic_name"));
            item.setType(rs.getString("cosmetic_type"));
            item.setUnlockType(rs.getString("unlock_type"));
            item.setLevelRequired(rs.getInt("lvl_required"));
            item.setCoinCost(rs.getInt("coin_cost"));
            item.setImageData(rs.getString("cosmetic_img_data"));
            return item;
        }, callback);
    }

    /**
     * Gets the user's current coin balance.
     */
    public void getCoinBalance(int userId, DatabaseCallback<Integer> callback) {
        String sql = "SELECT coins FROM user WHERE user_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{userId}, rs -> rs.getInt("coins"), callback);
    }

    /**
     * Updates the user's avatar skin tone (e.g. "fair", "beige", "tan", "dark").
     */
    public void updateSkinTone(int userId, String skinTone, DatabaseCallback<Integer> callback) {
        String sql = "UPDATE avatar SET skin_tone = ? WHERE user_id = ?";
        ConnectionManager.executeUpdate(sql, new Object[]{skinTone, userId}, callback);
    }

    /**
     * Gets the user's gender from their profile (drives base body art selection).
     * Returns null when no profile row exists.
     */
    public void getUserGender(int userId, DatabaseCallback<String> callback) {
        String sql = "SELECT user_gender FROM user_profile WHERE user_id = ?";
        ConnectionManager.querySingle(sql, new Object[]{userId},
                rs -> rs.getString("user_gender"), callback);
    }
}
