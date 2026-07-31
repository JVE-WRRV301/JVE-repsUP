# Spec: A100 — Register User

## Overview
A registration screen where new users enter their personal details to create 
an account. Upon successful registration, the user is directed to the fitness 
assessment (A110).

## Owner
James Ford

## Pre-Conditions
- Reps Up is installed and opened on the user's device

## Post-Conditions
- A new row is inserted into the users table with hashed password
- User is logged in and their user_id is stored in SessionManager
- User is navigated to A110 (Assess Fitness)

## Screens
Single Activity: RegisterActivity.java

**Welcome/Splash Screen:**
- "Welcome to REPS UP." heading
- Background image with gym aesthetic
- "Sign Up" button → navigates to registration form
- "Have an account already? Login" link → navigates to LoginActivity

**Registration Form:**
- "Sign Up" heading
- Input fields: Name, Username, Email, Password, Confirm Password
- "Get Started" button → validates and submits
- "Have an account? Login" link

## UI Requirements
- Dark grey background (#393944)
- Light blue accent buttons (#B2D6FD)
- Orbitron font for headings
- Inter font for input fields and body text
- Input fields with icons (person, grid, email, lock)

## Validation Rules
- Name: required, non-empty
- Username: required, unique (check against database)
- Email: required, valid email format, unique
- Password: required, minimum 8 characters
- Confirm Password: must match Password

## Files to Create
- `android/app/src/main/java/com/jve/repsup/activities/RegisterActivity.java`
- `android/app/src/main/res/layout/activity_register.xml`

## Database Operations
1. CHECK username uniqueness: SELECT user_id FROM users WHERE username = ?
2. CHECK email uniqueness: SELECT user_id FROM users WHERE email = ?
3. INSERT into users (name, email, username, password, xp, coins, level, rank_id, curr_streak, meas_system, language) VALUES (?, ?, ?, [hashed], 0, 0, 1, 1, 0, 'metric', 'en')

## Navigation
- Entry point: app launch → splash screen → "Sign Up" button
- Exit point: "Get Started" button (on success) → FitnessAssessmentActivity (A110)
- Alternative: "Login" link → LoginActivity

## Reference Designs
See designs/ folder — screens labelled A100 for wireframes.
