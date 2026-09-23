-- Add newly authored cosmetics and make all wearable items free for testing.
--
-- Context: new art was added (extra male shoes, several set-colour male hairs,
-- and non-wearable placeholder filler items). For now every WEARABLE item is
-- free so outfits can be tried on without spending coins. PLACEHOLDER items stay
-- locked ("coming soon") and are not wearable.
--
-- Idempotent: new rows are inserted only when their cosmetic_img_data key isn't
-- already present, so this migration is safe to run more than once.

-- 1) Make all existing wearable male items free (undoes the earlier pricing from
--    007). Placeholder rows are excluded so they remain locked.
UPDATE Cosmetic_Item
    SET unlock_type = 'default', coin_cost = 0
    WHERE gender = 'male'
      AND unlock_type = 'coins'
      AND cosmetic_img_data IN
          ('red_nike_shorts', 'white_nike_blazers', 'brown_sandels', 'red_nike_hoodie');

-- Also make any female items free (they were seeded as 'coins' with cost 0).
UPDATE Cosmetic_Item
    SET unlock_type = 'default', coin_cost = 0
    WHERE gender = 'female' AND unlock_type = 'coins';

-- 2) New wearable MALE shoes (free).
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Converse', 'shoes', 'male', FALSE, 'default', 0, 0, 'black_converse_sneakers'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_converse_sneakers');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Running Shoes', 'shoes', 'male', FALSE, 'default', 0, 0, 'blue_running_shoes'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'blue_running_shoes');

-- 3) New MALE set-colour hairs (is_colourable=FALSE; colour baked into the key;
--    free). imageData is the FULL filename stem (no separate colour prefix).
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Cornrows', 'hair', 'male', FALSE, 'default', 0, 0, 'black_cornrows_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_cornrows_hairstyle');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Flynn', 'hair', 'male', FALSE, 'default', 0, 0, 'black_flynn_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_flynn_hairstyle');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Short Afro', 'hair', 'male', FALSE, 'default', 0, 0, 'black_short_afro_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_short_afro_hairstyle');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Surfer Curls', 'hair', 'male', FALSE, 'default', 0, 0, 'blonde_surfer_curly_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'blonde_surfer_curly_hairstyle');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Buzz Cut', 'hair', 'male', FALSE, 'default', 0, 0, 'brown_buzz_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'brown_buzz_hairstyle');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Short Dreads', 'hair', 'male', FALSE, 'default', 0, 0, 'brown_short_dreads_hairstyle'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'brown_short_dreads_hairstyle');

-- 4) PLACEHOLDER (non-wearable) filler items. unlock_type='placeholder' keeps
--    them locked and non-equippable in the locker. imageData is the .jpeg stem in
--    assets/{type}/male-placeholders/. Nominal display price of 100 each.
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Adidas Hoodie', 'top', 'male', FALSE, 'placeholder', 0, 100, 'black_adidas_hoodie'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_adidas_hoodie');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Compression Shirt', 'top', 'male', FALSE, 'placeholder', 0, 100, 'black_compression_shirt'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_compression_shirt');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Blue T-Shirt', 'top', 'male', FALSE, 'placeholder', 0, 100, 'blue_tshirt'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'blue_tshirt');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Under Armour Shirt', 'top', 'male', FALSE, 'placeholder', 0, 100, 'navy_under_armour_shirt'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'navy_under_armour_shirt');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red T-Shirt', 'top', 'male', FALSE, 'placeholder', 0, 100, 'red_tshirt'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'red_tshirt');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Adidas Shorts', 'bottom', 'male', FALSE, 'placeholder', 0, 100, 'black_adidas_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_adidas_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Black Sweatpants', 'bottom', 'male', FALSE, 'placeholder', 0, 100, 'black_sweatpants'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'black_sweatpants');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Grey Shorts', 'bottom', 'male', FALSE, 'placeholder', 0, 100, 'grey_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'grey_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Under Armour Shorts', 'bottom', 'male', FALSE, 'placeholder', 0, 100, 'navy_under_armour_shorts'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'navy_under_armour_shorts');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Green Jordans', 'shoes', 'male', FALSE, 'placeholder', 0, 100, 'green_jordan_sneakers_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'green_jordan_sneakers_placeholder');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Running Shoes', 'shoes', 'male', FALSE, 'placeholder', 0, 100, 'pink_running_shoes_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'pink_running_shoes_placeholder');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Jordans', 'shoes', 'male', FALSE, 'placeholder', 0, 100, 'red_jordan_sneakers_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'red_jordan_sneakers_placeholder');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Adidas Sambas', 'shoes', 'male', FALSE, 'placeholder', 0, 100, 'white_adidas_sambas_placeholder'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE cosmetic_img_data = 'white_adidas_sambas_placeholder');
