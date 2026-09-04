-- Add socks_id column to Outfit_Slot table
-- Socks are an independent layer that can be worn together with shoes (socks under shoes)
-- or on their own. New users default to wearing socks with no shoes.
-- Nullable + ON DELETE SET NULL to match the other outfit slot foreign keys.
ALTER TABLE Outfit_Slot
    ADD COLUMN socks_id INT NULL AFTER shoes_id,
    ADD CONSTRAINT fk_outfit_socks
        FOREIGN KEY (socks_id) REFERENCES Cosmetic_Item(cosmetic_id) ON DELETE SET NULL;
