# Reps Up — Pre-Cross-Check Test Checklist (James)

A personal run-through to do today before the crew cross check. Work top to bottom.
Keep Logcat open filtered to `ConnectionManager` and `AndroidRuntime` so DB errors and
crashes are captured as you go. For anything that breaks, note: the screen, what you did,
what happened vs expected, and the Logcat line.

## 0. Pre-flight
- [ ] MySQL is running (service `MySQL80`).
- [ ] DB reloaded from the latest `schema.sql` + `seed.sql` (matches the team's current data).
- [ ] Latest app code pulled and rebuilt (so Emma's avatar assets are present).
- [ ] Logged out, then logged back in with a seed user (fresh session — avoids the blank-panel issue).

## 1. Auth & onboarding
- [ ] Log out → Welcome → Login with a seed user → lands on Home.
- [ ] Register a brand-new user → forced into fitness assessment → completes → avatar generated → reaches Home.
- [ ] Error dialog (not a toast) appears if login/registration fails.

## 2. Home tab
- [ ] Today's workout card shows correctly (or rest-day state if nothing scheduled).
- [ ] Next goal shows correctly.
- [ ] XP / level / rank panel populates (the panel that was previously blank).
- [ ] After scheduling a workout or creating a goal on another tab, switch back to Home → it updates live (no need to complete anything first).

## 3. Workout lifecycle (primary demo)
- [ ] Create a workout: pick muscle groups → select exercises → name → save.
- [ ] Max-8-exercises limit is enforced (validation toast appears).
- [ ] New workout appears in the Saved list.
- [ ] Schedule the workout for today on the Calendar.
- [ ] Switch to Home → scheduled workout appears live.
- [ ] Start the workout (only allowed if scheduled today) → tick all sets → complete.
- [ ] XP awarded, streak/level update, completion screen shows.
- [ ] Edit a workout → changes persist.
- [ ] Duplicate a workout → appears as a copy.
- [ ] Delete a workout → removed; error shows as a dialog if it fails.
- [ ] Reorder exercises → order persists.

## 4. Goals
- [ ] Create a goal with deadline + priority → shows on Home and in goals list.
- [ ] Complete a goal → XP awarded, UI updates.
- [ ] Delete a goal → removed (success toast), failure shows as a dialog.

## 5. Avatar / locker (Emma's area — verify synced data)
- [ ] Locker opens and cosmetics load WITH images (blank items = missing PNG assets).
- [ ] Equip top / bottom / shoes → avatar preview updates.
- [ ] Equipped outfit persists after leaving and returning.
- [ ] Buy a cosmetic with coins → coin balance deducts, item unlocks.

## 6. Profile / Settings (your area)
- [ ] Settings → Personal Details → edit name/email/username → save → reopen to confirm it persisted.
- [ ] Settings → Account → change password → log out → log in with the new password.
- [ ] Measurement system toggle persists.
- [ ] Delete Account — test on a THROWAWAY user, not your demo user. Confirmation dialog appears first.
- [ ] All error cases on these screens show as dialogs (validation hints stay as toasts).

## 7. Admin
- [ ] Admin login works.
- [ ] Create an exercise → success toast, appears in library.
- [ ] Create a default workout.
- [ ] Manage workouts (edit / delete) → delete failure shows as a dialog.

## 8. Navigation & polish
- [ ] Back buttons on every screen respond (Create Workout, Schedule, Create Goal, admin screens, Settings sub-screens).
- [ ] Rotate device / background then resume → no crash, data reloads.
- [ ] Level-up / rank-up / streak-milestone dialogs trigger at the right thresholds.
- [ ] No raw error toasts flashing by — failures are dialogs you must dismiss.

## 9. Demo dry run
- [ ] Have a workout pre-scheduled for TODAY so the "complete workout" demo step is guaranteed to work.
- [ ] Run the full workout-lifecycle demo path start to finish once, uninterrupted.
- [ ] Run the avatar customisation path once.
- [ ] Confirm the app is already launched + logged in before presenting (emulator cold start is slow).

---
Found issues (jot down here as you go):

1.
2.
3.
