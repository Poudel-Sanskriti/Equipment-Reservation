# CLAUDE.md: rules for AI agents working in this repo

Project: Equipment Reservation and Checkout System (CS 4398 Group 9). Java 17 · Swing · push-MVC (John Hunt) · SQLite · JUnit 5 · Gradle.

## Before doing anything
1. Read `prompts/00_master_context.md`. It holds the API contract, schema, rules, and constraints.
2. Do **only** the increment prompt you were given (`prompts/NN_*.md`). Don't start other increments.
3. If the prompt conflicts with the master context or the SRS (`ctxt/SRS_Group9.pdf`), stop and ask.

## Hard rules
- Packages: `equipmentReservation.{model, model.persistence, view, controller, util}`; tests in package `test` under `src/test/java/test/`.
- `model` and `util` NEVER import `javax.swing`, `view`, or `controller`. `util` imports only the JDK.
- Never change a public signature from master context §3 without a team decision. If one is agreed, update `00_master_context.md` in the same commit.
- Every database write goes through `Database.inTransaction`. PreparedStatement only. Never DELETE rows.
- Inject `java.time.Clock`. Never call `LocalDateTime.now()` without the clock. Tests use `MutableClock` and never `Thread.sleep` to wait for time.
- **Tests first:** write or adjust the JUnit tests, see them fail, implement, see them pass.
- Javadoc on every class and public/protected member (`@param`, `@return`, `@throws`), naming the FR/NFR ids it implements.
- Swing code runs on the EDT. Controllers turn exceptions into `view.showError` / `showFieldError`; never print stack traces as error handling.
- No new dependencies without asking.

## Commands
- Build and test: `./gradlew build` (must be green before you say you're done)
- Tests only: `./gradlew test` · one class: `./gradlew test --tests 'test.ReservationRulesTest'`
- Run the app: `./gradlew run --args="--demo"` (default users: admin1/admin123, jsmith/password1, mlee/password2)
- Javadoc: `./gradlew javadoc` · runnable jar: `./gradlew fatJar`

## When you finish an increment
1. `./gradlew build` is green.
2. Add a row to `docs/tracking.md` (increment, date, build, tests passed/total, FR ids covered, notes).
3. Summarize what changed and anything left undone. Don't claim untested behaviour works.
4. Don't commit or push unless the human asks. Commits are authored by the human only, with **no AI co-author lines**.
