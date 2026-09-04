-- Lock paid cosmetics and set their prices (male wardrobe).
--
-- During testing every clothing item was free (coin_cost = 0, unlock_type =
-- 'coins'). This migration restores the intended pricing so paid items show as
-- locked in the locker until purchased. Items are matched by cosmetic_img_data
-- (the stable item key), not by id, so it's safe regardless of insert order.
--
-- Pricing:
--   Tops:    White Tank / Blue Tank / White T-Shirt = free; Red Nike Hoodie = 100
--   Bottoms: Black Shorts / Grey Sweatpants / Blue Sweatpants = free; Red Nike Shorts = 50
--   Shoes:   White Sneakers = free; Nike Blazers = 100; Brown Sandals = 50
--
-- The app treats coin_cost <= 0 as free/owned, so free items must have cost 0.

-- Paid items: set unlock_type = 'coins' and their price.
UPDATE Cosmetic_Item SET unlock_type = 'coins', coin_cost = 100 WHERE cosmetic_img_data = 'red_nike_hoodie';
UPDATE Cosmetic_Item SET unlock_type = 'coins', coin_cost = 50  WHERE cosmetic_img_data = 'red_nike_shorts';
UPDATE Cosmetic_Item SET unlock_type = 'coins', coin_cost = 100 WHERE cosmetic_img_data = 'white_nike_blazers';
UPDATE Cosmetic_Item SET unlock_type = 'coins', coin_cost = 50  WHERE cosmetic_img_data = 'brown_sandels';

-- Free items: make sure they stay free (cost 0, unlocked by default).
UPDATE Cosmetic_Item SET unlock_type = 'default', coin_cost = 0
    WHERE cosmetic_img_data IN ('blue_tank', 'white_tshirt', 'grey_sweatpants', 'blue_sweatpants');

-- Remove any test-time unlocks of the now-paid items so they re-lock in the
-- locker (users must buy them again). Free/default items are left untouched.
DELETE uc FROM User_Cosmetic uc
    INNER JOIN Cosmetic_Item ci ON ci.cosmetic_id = uc.cosmetic_id
    WHERE ci.cosmetic_img_data IN
        ('red_nike_hoodie', 'red_nike_shorts', 'white_nike_blazers', 'brown_sandels');

-- Unequip any now-locked paid item that a test user was wearing, so the outfit
-- doesn't render a garment the user no longer owns.
UPDATE Outfit_Slot os
    LEFT JOIN Cosmetic_Item t  ON t.cosmetic_id  = os.top_id
    LEFT JOIN Cosmetic_Item b  ON b.cosmetic_id  = os.bottom_id
    LEFT JOIN Cosmetic_Item sh ON sh.cosmetic_id = os.shoes_id
    SET
        os.top_id    = CASE WHEN t.cosmetic_img_data  = 'red_nike_hoodie'    THEN NULL ELSE os.top_id    END,
        os.bottom_id = CASE WHEN b.cosmetic_img_data  = 'red_nike_shorts'    THEN NULL ELSE os.bottom_id END,
        os.shoes_id  = CASE WHEN sh.cosmetic_img_data IN ('white_nike_blazers', 'brown_sandels') THEN NULL ELSE os.shoes_id END;
