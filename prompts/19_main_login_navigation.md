# INCREMENT 19: Main Entry Point, Login and Navigation
**Status:** ★ CRITICAL · **Owner:** Sanskriti · **Dependencies:** 03, 08, 14 · **Requirements:** FR-01, FR-02, FR-16 (startup), FR-38, NFR-04

**CRITICAL: This is the first GUI-to-model integration. Follow the calcProj startup sequence: the controller creates the view, and the view registers with the model in the JFrameView constructor. Swing runs ONLY on the EDT. The startup expiration sweep runs before the first screen appears.**

---

### Why this is critical
This is the architectural transition from a tested model to a live GUI, and it sets the pattern every other screen copies.
- **Failure indicators:** frozen UI (DB work on the EDT is fine here since it's fast, but no sleeps); exceptions printed to the console instead of dialogs; views that don't refresh; windows left open after sign-out.
- **Mitigation:** every controller `operation` wraps model calls in `try { … } catch (ValidationException e) { view.showFieldError(e.getField(), e.getMessage()); } catch (ReservationSystemException e) { view.showError(e.getMessage()); } catch (PersistenceException e) { view.showError("Could not save your change. Please try again."); }`.

### Capability
- `Main.main(args)`:
  - DB path = the first non-flag arg, or `equipment.db` in the working directory; `--demo` seeds sample equipment
  - `new Database(path)` → `DataSeeder.seedDefaultUsersIfEmpty` → `new ReservationSystem(db, Clock.systemDefaultZone())` (constructor runs the startup sweep, FR-38) → `expiration.start()`
  - `SwingUtilities.invokeLater(() -> new LoginController(system))`
  - a shutdown hook calls `system.shutdown()`
- `LoginView` (SRS Figure 1): Username, Password (`JPasswordField`), Exit, Sign In (the default button), and a red inline message label for invalid credentials.
- `LoginController.operation`:
  - SIGN_IN → `session.signIn` → dispose the login → BORROWER: open `CatalogController`; ADMINISTRATOR: open `AdminHomeController`
  - EXIT → `system.shutdown()`, `System.exit(0)`
- `AdminHomeView` / `AdminHomeController`: buttons Equipment Catalog, Equipment Management, Checkout and Return, Maintenance, Settings, Sign out. A placeholder `showInfo("Coming soon")` is fine for screens not built yet.
- **Common header** (signed-in user + role + Sign out button) as a reusable `JPanel` factory in `view`. Sign out → `session.signOut()` → close all app frames → new LoginController.

### Tests
- Controller logic is testable headlessly if the controller only talks to the `View` interface. Add `test/LoginControllerTest.java` with a fake view (records `showError` calls): bad credentials → error shown, session not signed in.
- Manual check: `./gradlew run --args="--demo"`; sign in as admin1 and jsmith; sign out returns to the login screen.

---
**CRITICAL: calcProj startup order, EDT only, startup expiry sweep, uniform exception-to-dialog handling, sign-out closes everything.**
