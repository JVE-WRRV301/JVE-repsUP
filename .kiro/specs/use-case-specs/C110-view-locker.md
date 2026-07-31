# Spec: C110 — View Locker

## Overview
The locker screen allows users to browse, purchase, and equip cosmetic items 
for their avatar. Items are organized by category (tops, bottoms, shoes, 
accessories) with skin tone and body type options.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User is on the avatar page (C100)

## Post-Conditions
- The system displays the locker screen with all cosmetic items

## Screen
Activity: LockerActivity.java

**Header:**
- Back arrow (top left) → returns to avatar page
- Tick/save button (top right) → saves outfit changes

**Avatar Preview:**
- Full-body avatar at the top showing current outfit
- Down arrow to expand/collapse avatar view

**Coin Balance:**
- Coin icon + current coin count (e.g. "100")

**Category Tabs (icon row):**
- Tops icon (shirt)
- Bottoms icon (shorts)
- Shoes icon (sneaker)
- Accessories icon (hat/watch)
- Skin Tone icon
- Body Type icon

**Item Grid:**
- Grid of cosmetic items for the selected category
- Unlocked items: full color, tappable → equip (C112)
- Locked items: greyed out with coin price overlay, tappable → buy dialog (C111)

**Skin Tone Section:**
- Row of skin tone color circles to select

**Body Type Section:**
- Row of body type silhouettes to select

## UI Requirements
- Dark grey background (#393944)
- Coin count in yellow (#FAD942)
- Locked items have a semi-transparent overlay with price tag
- Selected/equipped items have light blue (#B2D6FD) border
- Category icons in white, selected icon highlighted

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/LockerActivity.java`
- `android/app/src/main/res/layout/activity_locker.xml`
- `android/app/src/main/java/com/jve/repsup/adapters/CosmeticAdapter.java`
- `android/app/src/main/res/layout/item_cosmetic.xml`

## Database Operations
1. SELECT ci.*, uc.is_unlocked, uc.is_equipped FROM cosmetic_items ci LEFT JOIN user_cosmetics uc ON ci.cos_id = uc.cos_id AND uc.user_id = ? WHERE ci.cos_type = ?
2. SELECT u.coins FROM users u WHERE u.user_id = ?
3. SELECT os.* FROM outfit_slots os WHERE os.user_id = ?

## Navigation
- Entry point: locker button on Avatar Page (C100)
- Tapping locked item → C111 (Unlock Cosmetic) buy dialog
- Tapping unlocked item → C112 (Customize Avatar) equip
- Back arrow → returns to Avatar Page (C100)
- Tick button → saves changes and returns to Avatar Page

## Reference Designs
See designs/ folder — locker screen with avatar preview, category tabs, and item grid.
