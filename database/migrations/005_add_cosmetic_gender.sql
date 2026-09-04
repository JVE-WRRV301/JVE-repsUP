-- Add gender column to Cosmetic_Item table
-- Clothing is gender-specific (a male body can't wear female-cut clothing, since
-- the female garments won't sit correctly on the larger male silhouette).
-- Values: 'male', 'female', or 'unisex'. Defaults to 'unisex' for any existing rows.
-- The locker filters items by the user's gender (showing their gender + unisex).
ALTER TABLE Cosmetic_Item
    ADD COLUMN gender VARCHAR(10) NOT NULL DEFAULT 'unisex' AFTER cosmetic_type;
