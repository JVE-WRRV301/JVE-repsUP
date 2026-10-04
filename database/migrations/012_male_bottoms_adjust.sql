-- Male bottoms adjustment: add Red Nike Shorts, remove Blue Sweatpants.
-- Scoped to gender='male' (red_nike_shorts also exists as a FEMALE placeholder,
-- which must not be touched). Idempotent.

-- Remove the male Blue Sweatpants (art deleted). Unequip it first so no outfit
-- points at the deleted row.
UPDATE Outfit_Slot os
    JOIN Cosmetic_Item b ON b.cosmetic_id = os.bottom_id
    SET os.bottom_id = NULL
    WHERE b.gender = 'male' AND b.cosmetic_img_data = 'blue_sweatpants';

DELETE FROM Cosmetic_Item
    WHERE gender = 'male' AND cosmetic_img_data = 'blue_sweatpants';

-- Add the male Red Nike Shorts (free), if not already present.
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Red Nike Shorts', 'bottom', 'male', FALSE, 'default', 0, 0, 'red_nike_shorts'
WHERE NOT EXISTS (
    SELECT 1 FROM Cosmetic_Item WHERE gender = 'male' AND cosmetic_img_data = 'red_nike_shorts'
);
