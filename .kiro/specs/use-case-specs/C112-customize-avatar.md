# Spec: C112 — Customize Avatar

## Overview
The user equips an unlocked cosmetic item to their avatar from the locker. 
The avatar preview updates in real time to show the selected item. Changes 
are saved when the user taps the tick/save button.

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User is on the locker screen on the avatar page
- User taps on a cosmetic item that is unlocked but not currently equipped

## Post-Conditions
- The avatar is displayed wearing the chosen cosmetic item
- The outfit_slots table is updated with the new item
- The previously equipped item in that slot is unequipped

## Basic Flow
1. User taps an unlocked cosmetic item in the locker grid
2. System updates the avatar preview to show the item equipped
3. The previous item in that category slot is visually deselected
4. User can continue selecting items in other categories
5. User can tap the down arrow to view avatar in full screen
6. User taps the tick/save button to confirm all changes
7. System updates outfit_slots and user_cosmetics in the database

## Avatar Preview Behavior
- Real-time preview updates as items are selected
- Down arrow expands avatar to full-screen view
- User can zoom in for a better look
- Selecting a different item in the same category replaces the previous

## Files to Create
- Equip logic handled within `LockerActivity.java`
- Avatar rendering uses `AvatarCompositor.java`

## Database Operations
1. UPDATE user_cosmetics SET is_equipped = false WHERE user_id = ? AND cos_id = [old_item_in_slot]
2. UPDATE user_cosmetics SET is_equipped = true WHERE user_id = ? AND cos_id = ?
3. UPDATE outfit_slots SET [slot_column] = ? WHERE user_id = ?
   - slot_column is top_id, bottom_id, shoes_id, or acc_id depending on cos_type

## Navigation
- Entry point: tapping an unlocked item in the locker grid (C110)
- Exit point: tick/save button → saves changes, returns to Avatar Page (C100)
- Back arrow → discards unsaved changes, returns to Avatar Page

## Reference Designs
See designs/ folder — locker with avatar preview showing equipped items and category selection.
