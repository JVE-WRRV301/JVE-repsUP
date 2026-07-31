# Reps Up - Project Overview

## 1.1 Problem Description
Many people struggle to maintain consistency in fitness journeys due to a lack of personalized guidance and insufficient motivation. Beginners often feel overwhelmed, not knowing what exercises to do or how to execute them. Traditional fitness applications focus on individual tracking or static plans, leading to disengagement over time.

**Reps Up** aims to solve this by creating a gamified fitness application where users can:
- Schedule, track, and execute workouts.
- Adapt to individual goals (muscle gain, fat loss, general fitness).
- Request AI-generated routines (for beginners) or create custom workouts (for experts).
- Engage with a gamified system (Avatar, XP, Coins, Ranks, Streaks) to sustain long-term motivation.

## 1.2 Business Rules / Assumptions
- **Onboarding:** Every new user must complete a fitness assessment before accessing core features.
- **XP & Progression:** XP is earned by completing workouts and achieving goals. XP determines level progression and rank advancement.
- **Streaks:**
  - A 2-week streak unlocks an XP multiplier (increases every 2 weeks thereafter).
  - Missing a scheduled workout resets the streak to 0 and resets the multiplier.
  - Days with no scheduled workout are rest days (do not break the streak).
- **Workouts:**
  - A workout is only "completed" when the user ticks off every set and logs the weight used.
  - Users may not start a workout that isn't scheduled for that day (they must edit the schedule).
  - A workout can contain exercises from many muscle groups, but a maximum of **8 exercises** per workout.
- **Cosmetics:** Users can only equip cosmetic items they own (purchased with coins).

## 1.3 System Constraints
- **Platform:** Mobile only (Android/iOS) to allow gym usage.
- **Database:** Pre-populated exercise database; users cannot add custom exercises.
- **Profile:** Profile picture is automatically the user's Avatar image; no manual photo uploads.
- **Integration:** No smartwatch connectivity.
- **Connectivity:** Internet connection is required for generating new workouts, updating avatars, and logging workouts.