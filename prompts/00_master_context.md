# MASTER CONTEXT DOCUMENT (Prompt 0): Equipment Reservation and Checkout System

**CRITICAL: Load this document at the start of EVERY agent session, before any increment prompt. It is the single source of truth for the domain, the architecture, the public API contract, and the rules. If an increment prompt seems to contradict this document, STOP and ask a human. Do not guess.**

Team: CS 4398.001 Group 9 (Sanskriti Poudel, Thomas Platt, Theo Kliewer). Final submission is due **2026-12-08**.
Requirements source: `ctxt/SRS_Group9.pdf` (requirement IDs FR-01…FR-39 and NFR-01…NFR-09). A digest is in `ctxt/PROJECT_REFERENCE.md` §3.

---

### 1. Domain Background

- A Java Swing **desktop** application for **one organization on one computer**. It manages shared equipment such as laptops, cameras, projectors, and calculators.
- Two roles: **BORROWER** searches equipment, reserves it, cancels own reservations, and views own history. **ADMINISTRATOR** manages equipment, processes checkout and return, handles maintenance, and changes settings.
- **Reservation lifecycle.** `ACTIVE` can move to `CHECKED_OUT` and then `COMPLETED`. An `ACTIVE` reservation can instead become `CANCELLED` (by its borrower) or `EXPIRED` (not picked up in time). `COMPLETED`, `CANCELLED`, and `EXPIRED` are final.
- **Pickup window:** a positive number of minutes (default **30**). **Pickup deadline = min(end, start + pickupWindow).** An `ACTIVE` reservation whose pickup deadline is ≤ now becomes `EXPIRED`.
- **Two periods overlap** iff `a.start < b.end && b.start < a.end`. Periods are half-open, so an end equal to another start does **not** overlap.
- **Blocking reservations:** statuses `ACTIVE` and `CHECKED_OUT`. `CANCELLED`, `EXPIRED`, and `COMPLETED` never block availability.
- **Records are never deleted.** History is kept through status changes (SRS §4.10).
- Out of scope: payments, fines, notifications, QR codes, mobile, network, multiple organizations, self-registration, password recovery.

---

### 2. System Architecture

**MANDATORY: push-MVC following the John Hunt Calculator pattern.** The professor's original is in `ctxt/reference/calcProj/`. Flow:
`User → View → Controller.operation(cmd) → model service → ReservationSystem.notifyChanged(ModelEvent) → every registered View.modelChanged() → the View pulls fresh data through getters.`

```
src/main/java/equipmentReservation/
├── Main.java
├── model/              business rules, entities, exceptions, services (NO Swing, NO view/controller imports)
│   └── persistence/    SQLite access: Database, repositories, seeding
├── controller/         one controller class per view, one controller instance per view instance
├── view/               Swing frames, all extending JFrameView
└── util/               DateTimes, PasswordHasher (pure helpers; depend on nothing in the project)
src/test/java/test/     JUnit 5 tests (package `test`, as in the professor's example)
```

**Dependency rules (NFR-05). CRITICAL:**
| Package | May import |
|---|---|
| `util` | JDK only |
| `model`, `model.persistence` | `util`, JDK (`java.sql`, `java.time`, `java.util.concurrent`). **Never** `javax.swing`, `java.awt` (except `java.awt.event.ActionEvent` for ModelEvent), `view`, `controller` |
| `controller` | `model`, `view`, `util` |
| `view` | `model` (getters, ModelListener), `controller` (Controller interface + the concrete controller to call `operation`), `util` |

**Notification design:** `ReservationSystem` is the **single Model** (it extends `AbstractModel`). Every view registers with it. Services do not extend AbstractModel. Each one receives the `ReservationSystem` and calls `system.fireChanged(String what)` **after the transaction commits, never while holding a lock**. `ModelEvent.getActionCommand()` says what changed: one of `ModelEvent.EQUIPMENT`, `RESERVATIONS`, `CHECKOUTS`, `MAINTENANCE`, `SETTINGS`, `SESSION`.

**Threading:**
- Swing must only be touched on the EDT. `JFrameView.modelChanged()` must re-dispatch with `SwingUtilities.invokeLater` when it isn't already on the EDT, because the expiration scheduler fires events from a background thread.
- `AbstractModel` keeps its listeners in a `CopyOnWriteArrayList`.

---

### 3. PUBLIC API CONTRACT

**CRITICAL: All three teammates code against these signatures in parallel. Do not rename or remove anything listed here. Adding private helpers is fine. Changing the contract needs a team decision and an edit to this file in the same commit.**

#### 3.1 MVC base (adapted from calcProj)
```java
// model
public interface Model { void notifyChanged(ModelEvent e); }
public class ModelEvent extends java.awt.event.ActionEvent {
    public static final String EQUIPMENT="EQUIPMENT", RESERVATIONS="RESERVATIONS", CHECKOUTS="CHECKOUTS",
                               MAINTENANCE="MAINTENANCE", SETTINGS="SETTINGS", SESSION="SESSION";
    public ModelEvent(Object source, String what)   // id = ActionEvent.ACTION_PERFORMED
}
public interface ModelListener { void modelChanged(ModelEvent event); }
public abstract class AbstractModel implements Model {   // CopyOnWriteArrayList<ModelListener>
    public void notifyChanged(ModelEvent e); public void addModelListener(ModelListener l); public void removeModelListener(ModelListener l);
}
// controller
public interface Controller { void setModel(Model m); Model getModel(); View getView(); void setView(View v); }
public abstract class AbstractController implements Controller { /* fields + accessors */ public abstract void operation(String command); }
// view
public interface View { Controller getController(); void setController(Controller c); Model getModel(); void setModel(Model m); }
public abstract class JFrameView extends JFrame implements View, ModelListener {
    public JFrameView(Model model, Controller controller);          // registers itself with the model
    public final void modelChanged(ModelEvent e);                  // EDT-safe: calls refresh(e) on the EDT
    protected abstract void refresh(ModelEvent e);
    public void showError(String message); public void showInfo(String message);   // JOptionPane
    public void showFieldError(String field, String message);       // default: showError; views may highlight the field
    public void dispose();                                          // also unregisters from the model
}
```

#### 3.2 Enums (`model`)
```java
public enum Role { BORROWER, ADMINISTRATOR }
public enum Condition { GOOD, FAIR, DAMAGED }
public enum OperationalStatus { OPERATIONAL, NON_OPERATIONAL }
public enum ReservationStatus { ACTIVE, CHECKED_OUT, COMPLETED, CANCELLED, EXPIRED }
public enum Availability { AVAILABLE, RESERVED, CHECKED_OUT, UNDER_MAINTENANCE, NON_OPERATIONAL }
```
Each enum has `public String label()`, returning for example "Non-operational" or "Under maintenance", for display.

#### 3.3 Entities (`model`). Immutable value classes: private final fields, constructor, getters, equals/hashCode on the id
```java
public final class User { User(String username, Role role); getUsername(); getRole(); }
public final class Equipment { Equipment(String id, String name, String category, String description,
                               Condition condition, OperationalStatus status); getters; }
public final class Reservation { Reservation(long id, String borrower, String equipmentId, LocalDateTime start,
                               LocalDateTime end, LocalDateTime createdAt, ReservationStatus status);
    getters; String getDisplayId() /* "R-" + id */; LocalDateTime pickupDeadline(int pickupWindowMinutes);
    boolean overlaps(LocalDateTime start, LocalDateTime end); boolean blocksAvailability(); }
public final class Checkout { Checkout(long reservationId, LocalDateTime checkoutTime, String checkoutAdmin,
                               LocalDateTime returnTime /*nullable*/, Condition returnCondition /*nullable*/, String returnAdmin /*nullable*/);
    getters; boolean isOpen(); }
public final class MaintenanceRecord { MaintenanceRecord(long id, String equipmentId, String reason, LocalDateTime openedAt,
                               String openedBy, LocalDateTime closedAt /*nullable*/, String closedBy /*nullable*/); getters; boolean isOpen(); }
public final class CatalogEntry { CatalogEntry(Equipment e, Availability a); getEquipment(); getAvailability(); }
public final class HistoryEntry { HistoryEntry(Reservation r, Checkout c /*nullable*/); getReservation(); getCheckout(); }
public final class OverdueCheckout { OverdueCheckout(Reservation r, Checkout c, Duration overdueBy); getters; }
```

#### 3.4 Exceptions (`model`)
```java
public class ReservationSystemException extends Exception      // checked base; getMessage() is user-facing text (NFR-04)
public class ValidationException extends ReservationSystemException { ValidationException(String field, String message); String getField(); }
public class AuthenticationException extends ReservationSystemException     // bad username/password
public class AuthorizationException extends ReservationSystemException      // not signed in / wrong role (NFR-08)
public class ConflictException extends ReservationSystemException           // overlapping reservation, duplicate ID
public class InvalidOperationException extends ReservationSystemException   // wrong state: e.g. cancel a COMPLETED reservation, second return
public class NotFoundException extends ReservationSystemException
public class PersistenceException extends RuntimeException                  // wraps SQLException; controllers catch it and show a generic message
```
**Messages must say what is wrong and how to fix it**, e.g. `"End time must be later than the start time."` (field `"end"`).

#### 3.5 Services (`model`). Every public method checks the role through `Session` (NFR-08)
```java
public class ReservationSystem extends AbstractModel {          // facade + the one Model
    public ReservationSystem(Database db, Clock clock);          // builds all services
    public Session getSession(); public EquipmentService getEquipmentService(); public CatalogService getCatalogService();
    public ReservationService getReservationService(); public CheckoutService getCheckoutService();
    public MaintenanceService getMaintenanceService(); public SettingsService getSettingsService(); public ExpirationService getExpirationService();
    public LocalDateTime now();                                  // LocalDateTime.now(clock) truncated to minutes
    public void fireChanged(String what);
    public void shutdown();                                      // stops scheduler, closes DB
}
public class Session {
    User signIn(String username, char[] password) throws AuthenticationException, ValidationException;   // FR-01
    void signOut();                                                                                      // FR-02
    Optional<User> getCurrentUser(); boolean isSignedIn();
    User requireSignedIn() throws AuthorizationException; User requireRole(Role role) throws AuthorizationException;
}
public class SettingsService {
    int getPickupWindowMinutes();                                                   // default 30
    void setPickupWindowMinutes(int minutes) throws ReservationSystemException;     // admin; > 0 (FR-15)
}
public class ExpirationService {
    int expireOverdue();                                         // own transaction; returns count; fires RESERVATIONS if > 0
    int expireOverdue(Connection c) throws SQLException;         // inside caller's transaction (FR-16)
    void start();  void stop();                                  // ScheduledExecutorService, every 60 s (FR-16)
}
public class EquipmentService {
    void addEquipment(Equipment e) throws ReservationSystemException;               // FR-20, FR-21
    void updateEquipment(Equipment e) throws ReservationSystemException;            // id identifies the row; id never changes
    Equipment getEquipment(String id) throws ReservationSystemException;
    List<Equipment> listEquipment() throws ReservationSystemException;              // any signed-in user
    List<String> listCategories() throws ReservationSystemException;
    void returnToService(String id, Condition condition) throws ReservationSystemException;   // FR-23
}
public class CatalogService {
    List<CatalogEntry> search(String nameFilter /*nullable*/, String category /*nullable = all*/,
                              LocalDateTime start /*nullable*/, LocalDateTime end /*nullable*/) throws ReservationSystemException;  // FR-04..07
}
public class ReservationService {
    Reservation createReservation(String equipmentId, LocalDateTime start, LocalDateTime end) throws ReservationSystemException; // borrower; FR-08..14
    void cancelReservation(long reservationId) throws ReservationSystemException;          // borrower, own ACTIVE only; FR-18
    List<HistoryEntry> getMyHistory() throws ReservationSystemException;                   // borrower; FR-19
    List<HistoryEntry> searchReservations(String borrower, String equipmentId, ReservationStatus status) throws ReservationSystemException; // admin; nulls = any; FR-24
    List<Reservation> getActiveReservationsFor(String equipmentId) throws ReservationSystemException;  // admin; used by FR-34
}
public class CheckoutService {
    void checkOut(long reservationId) throws ReservationSystemException;                   // admin; FR-25..27
    void returnItem(long reservationId, Condition condition) throws ReservationSystemException; // admin; FR-28, FR-29
    List<HistoryEntry> listOpenCheckouts() throws ReservationSystemException;              // admin
    List<OverdueCheckout> listOverdue() throws ReservationSystemException;                 // admin; FR-30
}
public class MaintenanceService {
    MaintenanceRecord openMaintenance(String equipmentId, String reason) throws ReservationSystemException; // admin; FR-31..34
    void closeMaintenance(long recordId) throws ReservationSystemException;
    List<MaintenanceRecord> listOpen() throws ReservationSystemException;
    List<MaintenanceRecord> listFor(String equipmentId) throws ReservationSystemException;
}
```

#### 3.6 Persistence (`model.persistence`)
```java
public final class Database implements AutoCloseable {
    public Database(Path file);                    // opens jdbc:sqlite:<file>, PRAGMA foreign_keys=ON, busy_timeout=5000, creates schema if absent
    public <T> T inTransaction(TxWork<T> work) throws ReservationSystemException;  // lock + BEGIN IMMEDIATE ... COMMIT / ROLLBACK
    public <T> T read(TxWork<T> work) throws ReservationSystemException;           // lock, no explicit tx
    public void close();
}
@FunctionalInterface public interface TxWork<T> { T run(Connection c) throws SQLException, ReservationSystemException; }
// Stateless repositories, all methods take Connection: UserRepository, EquipmentRepository, ReservationRepository,
// CheckoutRepository, MaintenanceRepository, ServiceLogRepository, SettingsRepository; plus DataSeeder.
```

#### 3.7 Utilities (`util`)
```java
public final class DateTimes {
    public static final String PATTERN = "yyyy-MM-dd HH:mm";     // FR-39 display/input format
    public static String format(LocalDateTime t);               // "" for null
    public static LocalDateTime parse(String text);             // throws IllegalArgumentException with a friendly message
    public static String toDb(LocalDateTime t);                 // "yyyy-MM-dd'T'HH:mm" (lexicographically sortable)
    public static LocalDateTime fromDb(String s);
}
public final class PasswordHasher {                             // PBKDF2WithHmacSHA256, 16-byte random salt, Base64
    public static String newSalt(); public static String hash(char[] password, String salt);
    public static boolean verify(char[] password, String salt, String expectedHash);   // constant-time compare
}
```

---

### 4. Database Schema (SQLite; times stored as TEXT `yyyy-MM-ddTHH:mm`, enums as their `name()`)
```sql
CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password_hash TEXT NOT NULL, salt TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('BORROWER','ADMINISTRATOR')));
CREATE TABLE IF NOT EXISTS equipment (id TEXT PRIMARY KEY, name TEXT NOT NULL, category TEXT NOT NULL, description TEXT NOT NULL DEFAULT '',
  condition TEXT NOT NULL CHECK (condition IN ('GOOD','FAIR','DAMAGED')),
  status TEXT NOT NULL CHECK (status IN ('OPERATIONAL','NON_OPERATIONAL')));
CREATE TABLE IF NOT EXISTS reservations (id INTEGER PRIMARY KEY AUTOINCREMENT, borrower TEXT NOT NULL REFERENCES users(username),
  equipment_id TEXT NOT NULL REFERENCES equipment(id), start_time TEXT NOT NULL, end_time TEXT NOT NULL, created_at TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('ACTIVE','CHECKED_OUT','COMPLETED','CANCELLED','EXPIRED')), CHECK (end_time > start_time));
CREATE INDEX IF NOT EXISTS idx_res_equipment ON reservations(equipment_id, status);
CREATE INDEX IF NOT EXISTS idx_res_borrower ON reservations(borrower, status);
CREATE TABLE IF NOT EXISTS checkouts (reservation_id INTEGER PRIMARY KEY REFERENCES reservations(id), checkout_time TEXT NOT NULL,
  checkout_admin TEXT NOT NULL REFERENCES users(username), return_time TEXT, return_condition TEXT, return_admin TEXT REFERENCES users(username));
CREATE TABLE IF NOT EXISTS maintenance_records (id INTEGER PRIMARY KEY AUTOINCREMENT, equipment_id TEXT NOT NULL REFERENCES equipment(id),
  reason TEXT NOT NULL, opened_at TEXT NOT NULL, opened_by TEXT NOT NULL REFERENCES users(username), closed_at TEXT, closed_by TEXT REFERENCES users(username));
CREATE TABLE IF NOT EXISTS service_log (id INTEGER PRIMARY KEY AUTOINCREMENT, equipment_id TEXT NOT NULL REFERENCES equipment(id),
  admin TEXT NOT NULL REFERENCES users(username), returned_at TEXT NOT NULL, condition TEXT NOT NULL);   -- FR-23
CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT NOT NULL);   -- 'pickup_window_minutes'
```
"Under maintenance" means there is a `maintenance_records` row with `closed_at IS NULL`. "Checked out" means there is a `checkouts` row with `return_time IS NULL`. Neither is stored as a separate flag.

**Seeding (DataSeeder):** if `users` is empty, create `admin1` / `admin123` (ADMINISTRATOR), plus `jsmith` / `password1` and `mlee` / `password2` (BORROWER). The `--demo` command-line flag also inserts about 8 sample items (EQ-104 Canon EOS Camera, EQ-113 Epson Projector, EQ-114 Dell Laptop, EQ-031 TI-84 Calculator, …).

---

### 5. Business Rules Checklist (map every test to these IDs)
| ID | Rule |
|---|---|
| FR-01/02 | Verify the password hash, take the role from the DB (the user never chooses it), sign-out clears the session |
| FR-04 | Case-insensitive partial name match. If nothing matches, the UI shows "No equipment matches your search." |
| FR-06/07 | Availability for [start,end): NON_OPERATIONAL → open maintenance → open checkout → overlapping ACTIVE/CHECKED_OUT reservation → AVAILABLE (first match wins). With no period given, check reservations against "now" |
| FR-08 | start and end required; start ≥ now (minute precision); end > start |
| FR-09 | reject if non-operational, under maintenance, or currently checked out |
| FR-10 | end − start ≤ 7 days; borrower has < 3 ACTIVE reservations |
| FR-11 | reject overlapping ACTIVE/CHECKED_OUT for the same item (end == other.start is allowed) |
| FR-12 | the check and the insert run inside ONE `Database.inTransaction` (lock + BEGIN IMMEDIATE), so concurrent conflicting requests → exactly one succeeds |
| FR-13 | new reservation: AUTOINCREMENT id, borrower = session user, createdAt = now, status ACTIVE |
| FR-15/16/17 | expire ACTIVE where pickupDeadline ≤ now: at startup, every 60 s, and at the start of the search / create / cancel / checkout transactions |
| FR-18 | only the owner, only ACTIVE → CANCELLED; anything else → InvalidOperationException/AuthorizationException |
| FR-21 | duplicate id → ConflictException; blank id/name/category → ValidationException; id immutable |
| FR-23 | only NON_OPERATIONAL items, condition must be GOOD or FAIR, no open maintenance; sets OPERATIONAL and writes a service_log row |
| FR-25/26/27 | ACTIVE reservation, start ≤ now < pickupDeadline, item operational, not under maintenance, no open checkout → insert checkout, status CHECKED_OUT |
| FR-28/29 | open checkout only; record time/condition/admin; status COMPLETED; equipment.condition = returned condition; DAMAGED → status NON_OPERATIONAL |
| FR-30 | open checkouts whose end < now, with overdue duration |
| FR-33/34 | open: reject if checked out or already under maintenance; existing reservations are kept; close: set closed_at/closed_by, reservations untouched |
| FR-37 / NFR-01 | every multi-row change happens in one transaction; any exception → ROLLBACK |
| FR-39 | all UI input and output uses `yyyy-MM-dd HH:mm`, local time zone |

---

### 6. Success Metrics (tracked in `docs/tracking.md`)
| Metric | 🟢 Green | 🟡 Yellow | 🔴 Red |
|---|---|---|---|
| `./gradlew build` | passes | warnings only | fails |
| JUnit pass rate | 100% | ≥ 95% (failures understood, issue open) | < 95% |
| Requirements with a passing test | all IDs of the increment | missing ≤ 1 | missing > 1 |
| Concurrency test (FR-12), 50 repetitions | 50/50 exactly one winner | n/a | any double booking |
| NFR-03 search / reserve check (1,000 items, 5,000 reservations) | ≤ 1 s | 1–3 s | > 3 s |

---

### 7. Technical Constraints
- Java **17** (no preview features), Swing only, Gradle wrapper (`./gradlew`), JUnit **5**, `org.xerial:sqlite-jdbc`. No other libraries without a team decision.
- **Inject `java.time.Clock` everywhere time matters.** Tests use a mutable test clock and never `Thread.sleep` to wait for expiration.
- Tests use `@TempDir` database files and never touch `equipment.db`.
- **Javadoc** on every class and every public/protected member: purpose, `@param`, `@return`, `@throws` (NFR-06).
- No `System.exit` in model code. No `printStackTrace` as error handling.
- Runs on Windows 10+ and macOS 13+ (NFR-09). Use `Path`, never hard-coded separators.

---

### 8. Process Rules (professor's model-driven process)
1. Skeleton first (prompt 04): declarations + Javadoc + stubs that `throw new UnsupportedOperationException("TODO")`, and the code compiles.
2. For every increment: **write the JUnit tests first**, see them fail, implement, see them pass, update the Javadoc.
3. The model is fully testable without any view or controller.
4. Keep the build green after every increment. Commit the prompt file together with the code it produced.
5. Reverse-engineer the UML class diagram after each major increment (`docs/uml/`).

---

**CRITICAL (repeat): Follow the API contract in §3 exactly, keep `model` free of Swing/view/controller imports, do every multi-step change inside `Database.inTransaction`, inject `Clock`, write tests first, and keep `./gradlew build` green. When in doubt, stop and ask a human.**
