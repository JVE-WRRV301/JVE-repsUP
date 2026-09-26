-- Reconcile the Cosmetic_Item catalogue with the current asset files, and wire up
-- female hair (which had no DB rows, so female avatars showed no hair).
--
-- What changed in assets:
--   * Some male/female items were removed (files deleted).
--   * Many new male/female tops/bottoms were added.
--   * Female hair art was added (a default + several set-colour styles).
--
-- Idempotent: inserts are guarded by NOT EXISTS on (gender, cosmetic_img_data);
-- deletes clear outfit/ownership references first so nothing dangles.

-- ---------------------------------------------------------------------------
-- 1) Remove stale wearable items whose art no longer exists.
--    Male:   grey_sweatpants, red_nike_shorts, brown_sandels, blue_tank, red_nike_hoodie
--    Female: grey_sweatpants, purple_flare_pants
-- ---------------------------------------------------------------------------
-- Unequip any stale item from outfit slots (so equipped outfits don't point at deleted rows).
UPDATE Outfit_Slot os
    LEFT JOIN Cosmetic_Item t  ON t.cosmetic_id  = os.top_id
    LEFT JOIN Cosmetic_Item b  ON b.cosmetic_id  = os.bottom_id
    LEFT JOIN Cosmetic_Item sh ON sh.cosmetic_id = os.shoes_id
    SET
        os.top_id    = CASE WHEN t.cosmetic_img_data IN ('blue_tank','red_nike_hoodie') THEN NULL ELSE os.top_id END,
        os.bottom_id = CASE WHEN b.cosmetic_img_data IN ('grey_sweatpants','red_nike_shorts','purple_flare_pants') THEN NULL ELSE os.bottom_id END,
        os.shoes_id  = CASE WHEN sh.cosmetic_img_data IN ('brown_sandels') THEN NULL ELSE os.shoes_id END;

-- Delete the stale rows (User_Cosmetic ownership rows cascade away via FK).
DELETE FROM Cosmetic_Item
    WHERE (gender = 'male'   AND cosmetic_img_data IN ('grey_sweatpants','red_nike_shorts','brown_sandels','blue_tank','red_nike_hoodie'))
       OR (gender = 'female' AND cosmetic_img_data IN ('grey_sweatpants','purple_flare_pants'));

-- ---------------------------------------------------------------------------
-- 2) Female DEFAULT hair (colourable). Female avatars need this so hair renders
--    and shows in the female locker Hair tab.
-- ---------------------------------------------------------------------------
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Default Hair', 'hair', 'female', TRUE, 'default', 0, 0, 'default_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_type='hair' AND cosmetic_img_data='default_hairstyle');

-- Re-point existing female avatars that were wearing the MALE default hair (the
-- old shared id) to the female default hair, so their hair actually renders.
UPDATE Outfit_Slot os
    JOIN User_Profile up ON up.user_id = os.user_id
    JOIN Cosmetic_Item fem ON fem.gender='female' AND fem.cosmetic_type='hair' AND fem.cosmetic_img_data='default_hairstyle'
    LEFT JOIN Cosmetic_Item cur ON cur.cosmetic_id = os.hair_id
    SET os.hair_id = fem.cosmetic_id
    WHERE LOWER(up.user_gender) = 'female'
      AND (os.hair_id IS NULL OR cur.gender = 'male');

-- ---------------------------------------------------------------------------
-- 3) New MALE wearables (free). Guarded so re-running is safe.
-- ---------------------------------------------------------------------------
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Black Tee', 'top', 'male', FALSE, 'default', 0, 0, 'black_tee'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='black_tee');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Grey Zip Hoodie', 'top', 'male', FALSE, 'default', 0, 0, 'grey_zip_hoodie'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='grey_zip_hoodie');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Navy Tank', 'top', 'male', FALSE, 'default', 0, 0, 'navy_tank'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='navy_tank');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Hoodie', 'top', 'male', FALSE, 'default', 0, 0, 'red_hoodie'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='red_hoodie');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Black Board Shorts', 'bottom', 'male', FALSE, 'default', 0, 0, 'black_board_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='black_board_shorts');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Shorts', 'bottom', 'male', FALSE, 'default', 0, 0, 'red_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='red_shorts');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'White Board Shorts', 'bottom', 'male', FALSE, 'default', 0, 0, 'white_board_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='white_board_shorts');

-- ---------------------------------------------------------------------------
-- 4) New FEMALE wearables (free).
-- ---------------------------------------------------------------------------
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Black Jacket', 'top', 'female', FALSE, 'default', 0, 0, 'black_jacket'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='black_jacket');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Grey Layered Top', 'top', 'female', FALSE, 'default', 0, 0, 'grey_layered_top'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='grey_layered_top');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'White Nike Top', 'top', 'female', FALSE, 'default', 0, 0, 'white_nike_top'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='white_nike_top');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Black Jorts', 'bottom', 'female', FALSE, 'default', 0, 0, 'black_jorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='black_jorts');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Grey Capri Pants', 'bottom', 'female', FALSE, 'default', 0, 0, 'grey_capri_pants'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='grey_capri_pants');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Grey Tights', 'bottom', 'female', FALSE, 'default', 0, 0, 'grey_tights'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='grey_tights');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'White Nike Shorts', 'bottom', 'female', FALSE, 'default', 0, 0, 'white_nike_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='white_nike_shorts');

-- ---------------------------------------------------------------------------
-- 5) FEMALE set-colour hair styles (is_colourable=FALSE; full filename stems).
--    Note: brown_messy_bun has NO _hairstyle suffix (matches the file on disk).
-- ---------------------------------------------------------------------------
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Bantu Knots', 'hair', 'female', FALSE, 'default', 0, 0, 'black_bantu_knots_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='black_bantu_knots_hairstyle');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Cornrows Bun', 'hair', 'female', FALSE, 'default', 0, 0, 'black_cornrows_bun_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='black_cornrows_bun_hairstyle');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Silk Press', 'hair', 'female', FALSE, 'default', 0, 0, 'black_slik_press_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='black_slik_press_hairstyle');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Beach Waves', 'hair', 'female', FALSE, 'default', 0, 0, 'blonde_beach_wave_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='blonde_beach_wave_hairstyle');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Double Braid', 'hair', 'female', FALSE, 'default', 0, 0, 'blonde_double_braid_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='blonde_double_braid_hairstyle');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Straight', 'hair', 'female', FALSE, 'default', 0, 0, 'blonde_straight_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='blonde_straight_hairstyle');
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Messy Bun', 'hair', 'female', FALSE, 'default', 0, 0, 'brown_messy_bun'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='brown_messy_bun');
