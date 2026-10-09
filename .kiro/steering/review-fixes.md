# Rules for review fixes

- The full review is in docs/reps-up-review.md. Items are numbered. Each task names the item numbers to fix.
- Fix ONLY the items named in the task. Do not refactor, rename or "improve" anything else.
  If you notice other problems, list them at the end of your reply instead of fixing them.
- Keep the existing architecture: Java, XML layouts, repositories + ConnectionManager, MySQL via JDBC.
- Use British spelling in any new identifiers, strings and comments (colour, customise, personalise).
- Never edit an existing migration file. Schema changes go in a new file in database/migrations/
  (next number in sequence), and schema.sql / seed.sql must be updated to match so a fresh setup works.
- All user-facing text goes in res/values/strings.xml, never hard-coded.
- UI callbacks from ConnectionManager run on a background thread: always use runOnUiThread and check
  isAdded()/isFinishing() before touching views.
- When you finish, reply with: (1) what changed for each item, (2) files touched,
  (3) any migration I must run, (4) step-by-step how to test it.
- Work directly in this chat. Do not use agents, workflows or background tasks.
- Before running a Gradle build or tests, tell me. If it takes longer than 5 minutes, stop it and tell me.
- Table names in the database and in all SQL are lowercase (user, workout, user_cosmetic …). Never write them capitalised.