# Spec: A111 — Generate Avatar

## Overview
After completing the fitness assessment, the system automatically generates 
a default avatar based on the user's profile data and displays it on a 
"Meet Your Avatar" screen.

## Owner
Emma Martin

## Pre-Conditions
- User has completed A110 (Assess Fitness)
- user_profiles row exists for this user in the database

## Post-Conditions
- A new row is inserted into the avatars table
- Default cosmetic items (1, 2, 3) are inserted into user_cosmetics as unlocked and equipped
- A row is inserted into outfit_slots with default items
- User.av_id is updated to link to the new avatar
- User is navigated to MainActivity (Home)

## Screen
Single screen: "MEET YOUR AVATAR!"
- Subtitle: "We'll personalise him later."
- Large avatar image in the centre (generated from profile data)
- "Continue" button at the bottom → navigates to MainActivity

## Avatar Generation Logic
Map the user's gender + body_type from user_profiles to a base avatar image:
- Use pre-rendered PNG images stored in res/drawable/avatars/
- Naming convention: avatar_[gender]_[body_type].png
  - e.g. avatar_male_athletic.png, avatar_female_slim.png
- Default outfit overlays applied on top:
  - Top: white_tank.png
  - Bottom: black_shorts.png
  - Shoes: white_sneakers.png
- Use AvatarCompositor.java to layer the images using Android Canvas

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/GenerateAvatarActivity.java`
- `android/app/src/main/res/layout/activity_generate_avatar.xml`
- `android/app/src/main/java/com/jve/repsup/utils/AvatarCompositor.java`

## Database Operations
1. INSERT into avatars (user_id, skin_tone, body_shape, facial_details)
2. INSERT into user_cosmetics for cos_id 1, 2, 3 (is_unlocked=true, is_equipped=true)
3. INSERT into outfit_slots (user_id, top_id=1, bottom_id=2, shoes_id=3)
4. UPDATE users SET av_id = [new avatar id] WHERE user_id = [current user]

## Navigation
- Entry point: called from FitnessAssessmentActivity on final step completion
- Exit point: "Continue" button → MainActivity

## Reference Designs
See designs/ folder — screens labelled A111 for wireframes.