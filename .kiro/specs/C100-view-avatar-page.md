# Spec: C100 — View Avatar Page

## Overview
The avatar page (accessed via the "Me" tab) displays the user's full avatar, 
their name, streak, coins, gym rank badge, and level. From here users can 
access the locker (C110) or settings (C113).

## Owner
Emma Martin

## Pre-Conditions
- User is registered and logged into the system
- User is not currently on the avatar page

## Post-Conditions
- The system displays the avatar page with all user data

## Screen
Single Fragment: MeFragment.java (inside MainActivity with bottom navigation)

**Header:**
- Username in Orbitron (e.g. "DYLAN")
- Streak count with fire icon
- Coin count with coin icon

**Avatar Display:**
- Full-body avatar image (generated from profile + equipped cosmetics)
- Centered on screen
- Locker button (shop/hanger icon, top right of avatar area) → C110
- Settings button (gear icon) → C113

**Rank Section:**
- "GYM RANK" heading in Orbitron
- Rank badge image (medal icon matching current rank tier)
- Rank name (e.g. "BRONZE II") below badge
- "LEVEL [X]" next to rank

## UI Requirements
- Dark grey background (#393944)
- Orbitron font for username, headings, rank name, level
- Inter font for other text
- Avatar rendered using AvatarCompositor with equipped outfit
- Rank badge uses corresponding medal image

## Files to Create
- `android/app/src/main/java/com/jve/repsup/fragments/MeFragment.java`
- `android/app/src/main/res/layout/fragment_me.xml`

## Database Operations
1. SELECT u.name, u.curr_streak, u.coins, u.level, u.rank_id, u.av_id FROM users u WHERE u.user_id = ?
2. SELECT r.tier, r.sub_tier FROM ranks r WHERE r.rank_id = ?
3. SELECT os.top_id, os.bottom_id, os.shoes_id, os.acc_id FROM outfit_slots os WHERE os.user_id = ?
4. SELECT a.skin_tone, a.body_shape, a.facial_details FROM avatars a WHERE a.user_id = ?

## Navigation
- Entry point: bottom nav "Me" tab
- Locker button → C110 (View Locker)
- Settings button → C113 (Manage User Profile)

## Reference Designs
See designs/ folder — avatar page with full-body avatar, rank badge, and level display.
