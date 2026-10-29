# INCREMENT 18: Persistence, Atomicity and Recovery Tests
**Status:** Standard · **Owner:** Theo · **Dependencies:** 12–17 · **Requirements:** FR-35, FR-36, FR-37, FR-38, NFR-01, NFR-02

**CRITICAL: This increment adds TESTS (and fixes any bugs they reveal). Prove that committed data survives a restart, that a failure mid-operation leaves no partial change, and that invalid input never crashes or corrupts.**

---

### Capability: tests in `test/PersistenceRecoveryTest.java`
1. **FR-35/36:** create users, equipment, a reservation, a checkout, a return, and maintenance; `shutdown()`; construct a new `ReservationSystem` on the same file; every record is identical.
2. **FR-37:** inject a failure in the middle of a multi-step operation. Use a test-only `TxWork` that inserts a checkout and then throws, through `Database.inTransaction`; assert no checkout row and the reservation still ACTIVE. Do the same for return (3 updates).
3. **NFR-01 (simulated forced shutdown):** start a transaction on a raw connection, write, and close the connection *without commit* (simulates the process dying); reopen → the previous state.
4. **FR-38:** see increment 14 (add it here if not present).
5. **NFR-02 robustness:** call every service method with nulls, blanks, huge strings (10,000 chars), and SQL-looking text (`'; DROP TABLE users; --`). Expect only `ReservationSystemException` subclasses, never NPE, never PersistenceException; the tables are still intact afterwards.

### Verification
- [ ] All green; any production bug found here gets fixed with a regression test
- [ ] Update the traceability table in `docs/acceptance/traceability.md`

---
**CRITICAL: Restart preserves data, failures roll back completely, bad input gives clean errors only.**
