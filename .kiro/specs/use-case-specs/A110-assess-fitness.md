# Spec: A110 — Assess Fitness

## Overview
A multi-step fitness assessment wizard that collects user profile data 
after registration. Data is saved to the user_profiles table on completion.

## Owner
Emma Martin

## Pre-Conditions
- User has completed registration (A100)
- User is logged in and their user_id is stored in SessionManager

## Post-Conditions
- A new row is inserted into the user_profiles table for this user
- User is navigated to A111 (Generate Avatar)

## Screens & Steps
The assessment is a single Activity (FitnessAssessmentActivity.java) that 
cycles through the following steps using fragment replacement or ViewPager2:

1. **Welcome Screen** — "WELCOME [NAME]! Complete your fitness assessment" + "Let's Go" button
2. **Gender** — Two options: Male / Female (circular selection buttons)
3. **Age** — Number input or scroll picker
4. **Weight** — Slider with kg/lbs toggle
5. **Height** — Slider with cm/ft toggle
6. **Body Type** — 6 body type images in a 2x3 grid, user selects one
7. **Workout Level** — 4 options: Never worked out / Beginner / Intermediate / Advanced
8. **Workout Schedule** — Options: 1-7 days/week
9. **Goal** — Multi-select: Lose weight, Stay Consistent, Build muscle, Get fitter, Improve endurance

## UI Requirements
- Dark grey background (#393944)
- Segmented progress bar at the top (light blue #B2D6FD for completed steps)
- Section label above progress bar ("About your body." or "Goals and focus.")
- Back and Next buttons at the bottom
- Next button disabled until current step has a valid selection
- Orbitron font for question headings
- Inter font for everything else

## Validation Rules
- Gender: must select one option
- Age: must be between 13 and 100
- Weight: must be between 30 and 300 (kg) or equivalent in lbs
- Height: must be between 100 and 250 (cm) or equivalent in ft
- Body Type: must select one
- Workout Level: must select one
- Schedule: must select one
- Goal: must select at least one

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/FitnessAssessmentActivity.java`
- `android/app/src/main/res/layout/activity_fitness_assessment.xml`
- `android/app/src/main/res/layout/step_gender.xml`
- `android/app/src/main/res/layout/step_age.xml`
- `android/app/src/main/res/layout/step_weight.xml`
- `android/app/src/main/res/layout/step_height.xml`
- `android/app/src/main/res/layout/step_body_type.xml`
- `android/app/src/main/res/layout/step_workout_level.xml`
- `android/app/src/main/res/layout/step_schedule.xml`
- `android/app/src/main/res/layout/step_goal.xml`
- `android/app/src/main/res/layout/step_welcome.xml`

## Database
Insert into `user_profiles` on final step completion:
- user_id (from SessionManager)
- gender
- age
- weight (stored in kg always, convert if user selected lbs)
- height (stored in cm always, convert if user selected ft)
- body_type
- w_lvl (workout level)
- nr_w_per_week
- fitness_goal

## Reference Designs
See `designs/` folder — screens labelled A110 for wireframes.

## Navigation
- Entry point: called from RegisterActivity after successful registration
- Exit point: on completion navigate to FitnessAssessmentActivity → A111 
  (GenerateAvatarActivity) — placeholder for now if A111 not built yet