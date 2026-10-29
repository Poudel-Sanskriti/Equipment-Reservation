# INCREMENT 08: ReservationSystem Facade, Session and Authentication (+ shared test fixture)
**Status:** Standard · **Owner:** Thomas · **Dependencies:** 05, 06, 07 · **Requirements:** FR-01, FR-02, NFR-08

**CRITICAL: Users NEVER pick their role. It comes from the database. Every service method must call `session.requireRole(...)` or `requireSignedIn()` first (NFR-08), so role checks hold even without the GUI.**

---

### Capability
1. `ReservationSystem(Database, Clock)`: constructs the Session and all services, passing in itself (for `fireChanged`), the Database, and the Clock. `now()` truncates to minutes. `fireChanged(what)` calls `notifyChanged(new ModelEvent(this, what))`. `shutdown()` stops expiration and closes the DB.
2. `Session.signIn(username, password)`:
   - blank username or password → `ValidationException` (field `username` / `password`)
   - unknown user or wrong password → `AuthenticationException("Invalid username or password. Please try again.")`. Use the same message for both, so it doesn't reveal which usernames exist.
   - success: store the `User`, fire `SESSION`, return the user.
3. `signOut()` clears the user and fires `SESSION`. `requireSignedIn` / `requireRole` throw `AuthorizationException("You must be signed in as an administrator to do this.")` and similar.
4. **Shared test support** (`src/test/java/test/support/`), used by EVERY later test:
   - `MutableClock extends Clock`: `set(LocalDateTime)`, `advanceMinutes(long)`; default zone.
   - `TestSystem`: creates a `@TempDir` DB, seeds the default users, builds `ReservationSystem` on a `MutableClock` fixed at `2026-11-10 08:00`, and offers `asAdmin()`, `asBorrower("jsmith")`, `addItem("EQ-1", "Camera")`, `at("2026-11-10 09:00")`.

### Tests to write FIRST (`test/SessionTest.java`)
- valid admin and borrower sign-in returns the stored role; wrong password, unknown user, and blank fields are rejected with the right exception.
- after `signOut`, `requireSignedIn` throws.
- a borrower calling `requireRole(ADMINISTRATOR)` → `AuthorizationException`.
- a listener receives `SESSION` events on sign-in and sign-out.

### Verification
- [ ] Tests green; teammates can use `TestSystem` in their branches (merge this early!)

---
**CRITICAL: Role from DB only, identical message for unknown user and bad password, role check in the model, shared TestSystem + MutableClock merged early.**
