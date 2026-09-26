-- Add newly authored shoes, and repair female avatars that are missing their
-- default socks and/or hair (created before those defaults were wired correctly).
--
-- Idempotent: inserts guarded by NOT EXISTS; repairs only touch rows that are
-- actually wrong, so this is safe to run more than once.

-- ---------------------------------------------------------------------------
-- 1) New shoes (all free).
--    Male:   pink_trainers
--    Female: black_converse, blue_trainers  (note: female converse key differs
--            from the male 'black_converse_sneakers')
-- ---------------------------------------------------------------------------
INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Pink Trainers', 'shoes', 'male', FALSE, 'default', 0, 0, 'pink_trainers'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='male' AND cosmetic_img_data='pink_trainers');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Converse', 'shoes', 'female', FALSE, 'default', 0, 0, 'black_converse'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='black_converse');

INSERT INTO Cosmetic_Item (cosmetic_name, cosmetic_type, gender, is_colourable, unlock_type, lvl_required, coin_cost, cosmetic_img_data)
SELECT 'Blue Trainers', 'shoes', 'female', FALSE, 'default', 0, 0, 'blue_trainers'
WHERE NOT EXISTS (SELECT 1 FROM Cosmetic_Item WHERE gender='female' AND cosmetic_img_data='blue_trainers');

-- ---------------------------------------------------------------------------
-- 2) Repair FEMALE default SOCKS. Female avatars should wear white socks by
--    default. Any female outfit slot missing socks (socks_id IS NULL) or pointing
--    at a non-female sock is set to the female white socks item. Also ensure the
--    ownership row exists so it counts as owned.
-- ---------------------------------------------------------------------------
UPDATE Outfit_Slot os
    JOIN User_Profile up ON up.user_id = os.user_id
    JOIN Cosmetic_Item fs ON fs.gender='female' AND fs.cosmetic_type='socks' AND fs.cosmetic_img_data='white_socks_default'
    LEFT JOIN Cosmetic_Item cur ON cur.cosmetic_id = os.socks_id
    SET os.socks_id = fs.cosmetic_id
    WHERE LOWER(up.user_gender) = 'female'
      AND (os.socks_id IS NULL OR cur.gender <> 'female' OR cur.cosmetic_type <> 'socks');

-- Ensure female users own their default socks (so it isn't treated as locked).
INSERT INTO User_Cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped)
SELECT up.user_id, fs.cosmetic_id, 1, 1
FROM User_Profile up
JOIN Cosmetic_Item fs ON fs.gender='female' AND fs.cosmetic_type='socks' AND fs.cosmetic_img_data='white_socks_default'
WHERE LOWER(up.user_gender) = 'female'
  AND NOT EXISTS (
      SELECT 1 FROM User_Cosmetic uc
      WHERE uc.user_id = up.user_id AND uc.cosmetic_id = fs.cosmetic_id
  );

-- ---------------------------------------------------------------------------
-- 3) Repair FEMALE default HAIR. Any female avatar with no hair equipped, or
--    still wearing the male default hair, is re-pointed to the female default
--    hair so hair renders (full body AND profile picture).
-- ---------------------------------------------------------------------------
UPDATE Outfit_Slot os
    JOIN User_Profile up ON up.user_id = os.user_id
    JOIN Cosmetic_Item fh ON fh.gender='female' AND fh.cosmetic_type='hair' AND fh.cosmetic_img_data='default_hairstyle'
    LEFT JOIN Cosmetic_Item cur ON cur.cosmetic_id = os.hair_id
    SET os.hair_id = fh.cosmetic_id
    WHERE LOWER(up.user_gender) = 'female'
      AND (os.hair_id IS NULL OR cur.gender <> 'female');

-- Ensure female users own their default hair.
INSERT INTO User_Cosmetic (user_id, cosmetic_id, is_unlocked, is_equipped)
SELECT up.user_id, fh.cosmetic_id, 1, 1
FROM User_Profile up
JOIN Cosmetic_Item fh ON fh.gender='female' AND fh.cosmetic_type='hair' AND fh.cosmetic_img_data='default_hairstyle'
WHERE LOWER(up.user_gender) = 'female'
  AND NOT EXISTS (
      SELECT 1 FROM User_Cosmetic uc
      WHERE uc.user_id = up.user_id AND uc.cosmetic_id = fh.cosmetic_id
  );
