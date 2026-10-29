# INCREMENT 07: Repositories and Data Seeding
**Status:** Standard · **Owner:** Sanskriti · **Dependencies:** 05, 06 · **Requirements:** FR-35, SRS §4.10

**CRITICAL: Repositories are stateless, take a `Connection`, use `PreparedStatement` ONLY (no string-concatenated SQL), and NEVER delete rows. Times go through `DateTimes.toDb/fromDb`; enums are stored with `name()`.**

---

### Capability
| Repository | Methods (suggested; add what services need) |
|---|---|
| `UserRepository` | `insert(c, username, hash, salt, role)`, `findCredentials(c, username)` → Optional record (hash, salt, role), `count(c)` |
| `EquipmentRepository` | `insert`, `update`, `findById`, `findAll` (ordered by id), `exists`, `findCategories`, `search(c, nameLike, category)` |
| `ReservationRepository` | `insert` → generated id, `findById`, `updateStatus(c, id, from, to)` → rows changed (guarded update: `WHERE id=? AND status=?`), `findOverlappingBlocking(c, equipmentId, start, end)`, `countActiveByBorrower`, `findByBorrower`, `search(c, borrower, equipmentId, status)`, `findActiveByEquipment`, `findActiveStartedBefore(c, now)` (expiration candidates) |
| `CheckoutRepository` | `insert`, `findByReservation`, `findOpenByEquipment`, `markReturned`, `findOpen` |
| `MaintenanceRepository` | `insert` → id, `findById`, `findOpenByEquipment`, `close`, `findOpen`, `findByEquipment` |
| `ServiceLogRepository` | `insert` |
| `SettingsRepository` | `getInt(c, key, default)`, `put(c, key, value)` |
| `DataSeeder` | `seedDefaultUsersIfEmpty(Database)`, `seedDemoEquipment(Database)` (master context §4) |

Overlap SQL: `start_time < :end AND end_time > :start AND status IN ('ACTIVE','CHECKED_OUT')`.

### Tests to write FIRST (`test/RepositoryTest.java`, `@TempDir`)
- Round-trip each entity; null `return_time` stays null.
- `findOverlappingBlocking`: adjacent periods are not returned; CANCELLED/EXPIRED/COMPLETED are ignored.
- The guarded `updateStatus` returns 0 when the current status differs.
- Seeding is idempotent (running it twice still gives 3 users).

### Verification
- [ ] Tests green; `grep -rn "DELETE" src/main/java` → empty

---
**CRITICAL: PreparedStatement only, no DELETE, stateless, Connection passed in, guarded status updates.**
