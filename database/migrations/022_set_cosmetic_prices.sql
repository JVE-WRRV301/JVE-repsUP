-- Migration 022: real coin prices for the locker cosmetics.
-- Starter items stay free (unlock_type 'default'); priced wearable items become
-- unlock_type 'coins'; placeholder items keep their price but stay 'placeholder' (not buyable yet).

-- The updates match on name, not a key column, so MySQL Workbench's safe-update mode
-- would reject them. Switch it off for this script only.
SET SQL_SAFE_UPDATES = 0;

UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Black Shorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Blue Tights' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Blue Shorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Grey Capri Pants' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Sweatpants' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Black Jorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Grey Tights' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Nike Shorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red Nike Shorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Under Armour Shorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Under Armour Sweats' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'White Nike Shorts' AND cosmetic_type = 'bottom' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Default Hair' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Bantu Knots' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Beach Waves' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Cornrows Bun' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Double Braid' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Messy Bun' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Silk Press' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Straight' AND cosmetic_type = 'hair' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'White Sneakers' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Blue Trainers' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Running Shoes' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Trainers' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Converse' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Nike Blazers' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 300, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Adidas Sambas' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 300, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Green Jordans' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 300, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red Jordans' AND cosmetic_type = 'shoes' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Pink T-Shirt' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'White Tank' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Blue Jacket' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Sports Bra' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Blue Nike Tank' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Green Adidas Bra' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red Nike Bra' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Under Armour Tank' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'White Nike Top' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Black Jacket' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Green Nike Jacket' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Grey Layered Top' AND cosmetic_type = 'top' AND gender = 'female';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Black Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Red Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Black Sweatpants' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Grey Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'White Board Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Black Board Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Adidas Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red Nike Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Under Armour Shorts' AND cosmetic_type = 'bottom' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'Default Hair' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Buzz Cut' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Cornrows' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Flynn' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Short Afro' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Short Dreads' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Surfer Curls' AND cosmetic_type = 'hair' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'White Sneakers' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Running Shoes' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Pink Trainers' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Running Shoes' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Converse' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 200, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Nike Blazers' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 300, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Adidas Sambas' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 300, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Green Jordans' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 300, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red Jordans' AND cosmetic_type = 'shoes' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'White T-Shirt' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 0 WHERE cosmetic_name = 'White Tank' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Blue T-Shirt' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Navy Tank' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 50, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red T-Shirt' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Black Tee' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Compression Shirt' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Red Hoodie' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 100, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Under Armour Shirt' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Adidas Hoodie' AND cosmetic_type = 'top' AND gender = 'male';
UPDATE cosmetic_item SET coin_cost = 150, unlock_type = IF(unlock_type = 'placeholder', 'placeholder', 'coins') WHERE cosmetic_name = 'Grey Zip Hoodie' AND cosmetic_type = 'top' AND gender = 'male';

-- Items that were free are now paid, so anything a user is wearing right now is marked
-- as owned. Without this their current outfit would show as locked in the locker.
INSERT INTO user_cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped)
SELECT os.user_id, ci.cosmetic_id, 1, 1
FROM outfit_slot os
JOIN cosmetic_item ci ON ci.cosmetic_id IN (os.top_id, os.bottom_id, os.shoes_id, os.socks_id, os.hair_id, os.accessories_id)
ON DUPLICATE KEY UPDATE is_unlocked = 1;

SET SQL_SAFE_UPDATES = 1;
