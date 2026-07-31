# Spec: A321 — Share Rank Up

## Overview
After ranking up, the user can share their new rank achievement to external 
platforms (WhatsApp, Instagram, etc.) using Android's native share intent.

## Owner
James Ford

## Pre-Conditions
- User is registered and logged into the system
- User's rank has just increased (A320 completed)

## Post-Conditions
- The system exports the rank-up pop-up as a shareable image/text
- Android share sheet is displayed with platform options

## Share Content
- Image: screenshot/render of the rank-up dialog showing the new rank badge
- Text: "I just reached [RANK NAME] on Reps Up! 🏆"
- Link: RepsUp.com (placeholder)

## Basic Flow
1. User presses the share button on the rank-up dialog
2. System captures the rank-up card as a shareable image
3. System triggers Android's ACTION_SEND intent with image + text
4. User selects their preferred platform (WhatsApp, Instagram, etc.)
5. Platform handles the share externally

## Files to Create
- Share logic in `RankUpDialog.java` (share button click handler)
- Utility method in `android/app/src/main/java/com/jve/repsup/utils/ShareHelper.java`

## Database Operations
- None (sharing is external, no database writes needed)

## Navigation
- Entry point: share button on RankUpDialog (A320)
- Exit point: Android share sheet opens → user returns to app after sharing

## Reference Designs
See designs/ folder — share sheet showing AirDrop, WhatsApp, Notes, Instagram options.
