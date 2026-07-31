# Reps Up — Product Context

## What Reps Up is

Reps Up is a gamified mobile fitness application for Android. It helps users schedule, track, and execute workouts while keeping them motivated through XP, levels, ranks, avatar customisation, and streak-based rewards. The app is inspired by gaming aesthetics — dark theme, blocky fonts, ranked progression.

## Target user

- Fitness beginners who feel overwhelmed by traditional fitness apps
- Gym-goers who need motivation to stay consistent
- Users who enjoy game-like feedback loops (XP, coins, ranks)
- Primarily used on personal Android phones during and between gym sessions

## Core features

- **Fitness assessment on signup** — gender, age, weight, height, body type, workout level, weekly frequency, goal
- **Workouts** — user-created OR admin-created "default" workouts. Each workout contains up to 8 exercises across muscle groups.
- **Scheduled workouts** — placed on a calendar. Users can only start workouts scheduled for today.
- **Goals** — user-defined fitness targets with deadlines and priority levels.
- **Gamification** — XP earned from workouts and goals; XP builds levels; levels unlock ranks (Bronze → Diamond, three sub-tiers each); ranks and levels reward coins.
- **Streaks** — completed scheduled workouts extend streak; missing a scheduled workout resets it. Rest days (no workout scheduled) do not break streaks. 2-week streaks unlock XP multipliers.
- **Avatar & locker** — every user gets an avatar generated from their fitness assessment. Coins buy cosmetics from a locker (tops, bottoms, shoes, accessories).
- **Sharing** — rank ups, streak milestones, and completed goals can be exported to social platforms.

## Key business rules

- Every new user must complete the fitness assessment before accessing the app.
- Users earn XP only from completing workouts or achieving goals.
- Users earn coins only from reaching new levels (with bonus coins on rank-up levels).
- A workout is only "complete" when every set has been ticked off and the required data logged.
- Users may not start a workout that isn't scheduled for today.
- A user can select a maximum of 8 exercises per workout.
- Users can only equip cosmetics they've unlocked.

## Constraints

- Android mobile only (no iOS, no web)
- Requires internet connection for workout generation, avatar updates, and workout logging
- The exercise database is pre-populated by admins; users cannot add custom exercises
- No smartwatch connectivity
- Profile pictures are always the user's avatar (not user-uploaded)

## The team

- James Ford, Vincent van Niekerk, Emma Martin (team JVE)
- WRRV301 module, 2026