-- Hair customization + colourable cosmetics support.
--
-- 1) Outfit_Slot.hair_id: hair is now a rendered layer with its own slot, like
--    the other equipped pieces (previously hair equipped state lived only in
--    User_Cosmetic and was never composited).
-- 2) Avatar.hair_colour: the chosen colour for the equipped hairstyle. Defaults
--    to 'grey' (the pre-selection default), same convention as skin_tone.
-- 3) Cosmetic_Item.is_colourable: whether a hairstyle supports colour swapping.
--    Only colourable hairs show the colour picker in the locker.

ALTER TABLE Outfit_Slot
    ADD COLUMN hair_id INT NULL AFTER socks_id,
    ADD CONSTRAINT fk_outfit_hair
        FOREIGN KEY (hair_id) REFERENCES Cosmetic_Item(cosmetic_id) ON DELETE SET NULL;

ALTER TABLE Avatar
    ADD COLUMN hair_colour VARCHAR(20) NOT NULL DEFAULT 'grey' AFTER skin_tone;

ALTER TABLE Cosmetic_Item
    ADD COLUMN is_colourable BOOLEAN NOT NULL DEFAULT FALSE AFTER gender;
