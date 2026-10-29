# INCREMENT 13: Concurrent Reservation Safety
**Status:** ★ CRITICAL · **Owner:** Thomas · **Dependencies:** 12 · **Requirements:** FR-12, NFR-07

**CRITICAL: When N threads request overlapping periods for the same item at the same moment, EXACTLY ONE succeeds and the others get ConflictException. The test must be repeatable (≥ 50 repetitions) with no flakiness and no Thread.sleep timing tricks.**

---

### Why this is critical
This is the concurrency requirement the SRS calls out. It's a likely professor question and the most impressive part of the demo.
- **Risk:** the check-then-insert race; "database is locked" errors surfacing as PersistenceException instead of a clean conflict; the test passing by luck.
- **Mitigation:** the whole check+insert already runs in `inTransaction` (lock + BEGIN IMMEDIATE, increment 06). This increment **proves** it and fixes any gaps.
- **Isolation protocol:** (1) 2 threads; (2) 10 threads; (3) 10 threads with *different* non-overlapping periods → all succeed (proves we didn't over-serialize into wrong rejections); (4) two separate `Database` instances on the same file (simulates two app windows/processes) → still exactly one.
- **Reference:** `Books/SEbooks/JavaConccurrencyInPractice-1.pdf` ch. 12 (testing concurrent programs); `resources/JavaCodeEx/concurrentTestingGoetz.zip` in the course zip.

### Capability
- The test infrastructure: an `ExecutorService` with N threads, a `CountDownLatch startGate` so all threads start at once, `Future`s collecting a Reservation or an exception.
- Each thread signs in as a different borrower. **Note:** `Session` is a single current user, so for this test create one `ReservationSystem` per borrower over the same `Database` file, or add a package-private `createReservationAs(User, ...)` used only by tests. Document whichever you choose in the class Javadoc.

### Tests to write FIRST (`test/ConcurrentReservationTest.java`)
- `@RepeatedTest(50)` 10 threads, same item, same period → successes == 1, conflicts == 9, DB has exactly 1 ACTIVE row
- 10 threads, same item, non-overlapping periods → 10 successes
- two Database instances on the same file → exactly one success
- no PersistenceException in any run

### Verification
- [ ] 50/50 green, run 3 times (`./gradlew test --tests '*Concurrent*' --rerun-tasks`)
- [ ] Record the results in `docs/tracking.md` (green zone = 50/50)

---
**CRITICAL: Exactly one winner, every time, proven by a repeated latch-started multi-thread test, including across two Database instances.**
