# INCREMENT 06: Database, Schema and Transactions
**Status:** ★ CRITICAL · **Owner:** Sanskriti · **Dependencies:** 04 · **Requirements:** FR-35, FR-36, FR-37, NFR-01, NFR-02, foundation for FR-12

**CRITICAL: `Database.inTransaction` is the ONLY way to write. It must serialize writers (ReentrantLock + `BEGIN IMMEDIATE`), COMMIT on success, ROLLBACK on ANY exception, and re-throw. The schema is exactly master context §4 with `PRAGMA foreign_keys=ON`.**

---

### Why this is critical
Atomicity (FR-37), crash consistency (NFR-01) and the concurrency guarantee (FR-12) all rest on this class.
- **Anticipated failure modes:** SQLite "database is locked" errors; foreign keys silently off (that's SQLite's default!); partial writes when an exception is thrown halfway; deadlock if `inTransaction` is re-entered from inside a transaction.
- **Mitigations:** one shared `Connection`, guarded by a fair `ReentrantLock`. `busy_timeout=5000`. Re-entrant calls on the same thread **join** the outer transaction (track depth) instead of issuing a nested BEGIN. Wrap `SQLException` in `PersistenceException`; let `ReservationSystemException` pass through after the rollback.
- **Isolation test protocol:** (1) open and close an empty DB; (2) a single insert commits; (3) an exception after the first of two inserts → zero rows; (4) 20 threads × 50 increments of a counter row → exactly 1,000.
- **Rollback:** revert to the skeleton stub; services can't be implemented until this is green.

### Capability
- `Database(Path)`: `DriverManager.getConnection("jdbc:sqlite:" + file)`, autoCommit false, pragmas (`foreign_keys=ON`, `busy_timeout=5000`, `journal_mode=WAL`), runs the schema DDL from §4 and inserts the `pickup_window_minutes=30` setting if missing.
- `inTransaction(TxWork<T>)`: lock → `BEGIN IMMEDIATE` (outermost only) → `work.run(c)` → COMMIT → unlock; on Throwable: ROLLBACK, unlock, rethrow.
- `read(TxWork<T>)`: lock, run, unlock (consistent snapshot).
- `close()`: idempotent.

### Tests to write FIRST (`test/DatabaseTest.java`, using `@TempDir`)
- The schema exists: all 7 tables (query `sqlite_master`).
- `PRAGMA foreign_keys` returns 1; inserting a reservation for an unknown user fails.
- Rollback on `ReservationSystemException` and on `RuntimeException`; the exception propagates unchanged.
- Data persists after `close()` and reopening the same file (FR-36).
- The concurrency counter test (above) is exact.
- A nested `inTransaction` inside `inTransaction` works and commits once.

### Verification
- [ ] All tests green 10 runs in a row (`./gradlew test --rerun-tasks`)
- [ ] No `Connection` escapes the class except inside `TxWork`

---
**CRITICAL: Lock + BEGIN IMMEDIATE + rollback on anything + foreign keys ON. Every later increment trusts this class.**
