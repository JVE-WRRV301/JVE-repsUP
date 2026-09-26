-- Refresh the female wardrobe: repoint the female default items to the new
-- suffix-free asset keys, remove the old female items whose art no longer exists,
-- and add the re-authored female wearables (free) plus female placeholders (locked).
--
-- Background: the female cosmetic art was re-authored. New keys drop the "_female"
-- suffix and the item set changed. The female base body has black shorts painted
-- on, so the female default bottom (id 5) has NO overlay file and renders nothing
-- when equipped; its locker thumbnail comes from the placeholder .jpeg.
--
-- Idempotent: inserts are guarded by NOT EXISTS on cosmetic_img_data.

-- 1) Repoint the female DEFAULT items (ids 4/5/6) to the new keys. Matched by the
--    old key values so we don't depend on exact ids.
UPDATE Cosmetic_Item SET cosmetic_img_data = 'white_tank_default'
    WHERE gender = 'female' AND cosmetic_type = 'top'    AND cosmetic_img_data = 'white_tank_default_female';
UPDATE Cosmetic_Item SET cosmetic_img_data = 'black_shorts_default'
    WHERE gender = 'female' AND cosmetic_type = 'bottom' AND cosmetic_img_data = 'black_shorts_default_female';
UPDATE Cosmetic_Item SET cosmetic_img_data = 'white_socks_default'
    WHERE gender = 'female' AND cosmetic_type = 'socks'  AND cosmetic_img_data = 'white_socks_default_female';

-- 2) Remove the OLD female non-default items whose art was deleted. First clear any
--    references so foreign keys / equipped outfits don't point at deleted rows.
--    (Outfit_Slot FKs are ON DELETE SET NULL and User_Cosmetic is ON DELETE CASCADE,
--     but we clear explicitly to also unequip cleanly.)
SET @stale := 'white_sneakers_female,blue_jacket_female,blue_tank_female,pink_nike_bra_female,pink_nike_shorts_female,purple_pants_female';

-- Unequip any stale female item from outfits.
UPDATE Outfit_Slot os
    LEFT JOIN Cosmetic_Item t  ON t.cosmetic_id  = os.top_id
    LEFT JOIN Cosmetic_Item b  ON b.cosmetic_id  = os.bottom_id
    LEFT JOIN Cosmetic_Item sh ON sh.cosmetic_id = os.shoes_id
    SET
        os.top_id    = CASE WHEN FIND_IN_SET(t.cosmetic_img_data,  @stale) > 0 THEN NULL ELSE os.top_id    END,
        os.bottom_id = CASE WHEN FIND_IN_SET(b.cosmetic_img_data,  @stale) > 0 THEN NULL ELSE os.bottom_id END,
        os.shoes_id  = CASE WHEN FIND_IN_SET(sh.cosmetic_img_data, @stale) > 0 THEN NULL ELSE os.shoes_id  END;

-- Delete the stale rows (User_Cosmetic rows cascade away via FK).
DELETE FROM Cosmetic_Item
    WHERE gender = 'female'
      AND FIND_IN_SET(cosmetic_img_data, @stale) > 0;

-- 3) New female WEARABLES (free). Guarded so re-running is safe.
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Blue Jacket', 'top', 'female', FALSE, 'default', 0, 0, 'blue_jacket'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'blue_jacket');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Blue Nike Tank', 'top', 'female', FALSE, 'default', 0, 0, 'blue_nike_tank'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'blue_nike_tank');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink T-Shirt', 'top', 'female', FALSE, 'default', 0, 0, 'pink_tshirt'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'pink_tshirt');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Blue Tights', 'bottom', 'female', FALSE, 'default', 0, 0, 'blue_tights'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'blue_tights');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Grey Sweatpants', 'bottom', 'female', FALSE, 'default', 0, 0, 'grey_sweatpants'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'grey_sweatpants');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Nike Shorts', 'bottom', 'female', FALSE, 'default', 0, 0, 'pink_nike_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'pink_nike_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Purple Flare Pants', 'bottom', 'female', FALSE, 'default', 0, 0, 'purple_flare_pants'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'purple_flare_pants');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'White Sneakers', 'shoes', 'female', FALSE, 'default', 0, 0, 'white_sneakers'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'white_sneakers');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Nike Blazers', 'shoes', 'female', FALSE, 'default', 0, 0, 'white_nike_blazers'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'white_nike_blazers');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Trainers', 'shoes', 'female', FALSE, 'default', 0, 0, 'pink_trainers'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'pink_trainers');

-- 4) Female PLACEHOLDER (non-wearable) filler items. unlock_type='placeholder'.
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Green Adidas Bra', 'top', 'female', FALSE, 'placeholder', 0, 100, 'green_adidas_sports_bra'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'green_adidas_sports_bra');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Green Nike Jacket', 'top', 'female', FALSE, 'placeholder', 0, 100, 'green_nike_jacket'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'green_nike_jacket');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Under Armour Tank', 'top', 'female', FALSE, 'placeholder', 0, 100, 'navy_under_armour_tank'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'navy_under_armour_tank');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Sports Bra', 'top', 'female', FALSE, 'placeholder', 0, 100, 'pink_sports_bra'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'pink_sports_bra');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Nike Bra', 'top', 'female', FALSE, 'placeholder', 0, 100, 'red_nike_sports_bra'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'red_nike_sports_bra');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Blue Shorts', 'bottom', 'female', FALSE, 'placeholder', 0, 100, 'blue_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'blue_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Under Armour Shorts', 'bottom', 'female', FALSE, 'placeholder', 0, 100, 'navy_under_armour_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'navy_under_armour_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Under Armour Sweats', 'bottom', 'female', FALSE, 'placeholder', 0, 100, 'navy_under_armour_sweatpants'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'navy_under_armour_sweatpants');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Sweatpants', 'bottom', 'female', FALSE, 'placeholder', 0, 100, 'pink_sweatpants'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'pink_sweatpants');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Nike Shorts', 'bottom', 'female', FALSE, 'placeholder', 0, 100, 'red_nike_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'red_nike_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Green Jordans', 'shoes', 'female', FALSE, 'placeholder', 0, 100, 'green_jordan_sneakers_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'green_jordan_sneakers_placeholder');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Running Shoes', 'shoes', 'female', FALSE, 'placeholder', 0, 100, 'pink_running_shoes_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'pink_running_shoes_placeholder');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Jordans', 'shoes', 'female', FALSE, 'placeholder', 0, 100, 'red_jordan_sneakers_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'red_jordan_sneakers_placeholder');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Adidas Sambas', 'shoes', 'female', FALSE, 'placeholder', 0, 100, 'white_adidas_sambas_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data = 'white_adidas_sambas_placeholder');
