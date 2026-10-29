# INCREMENT 14: Reservation Expiration (startup, periodic, before operations)
**Status:** ★ CRITICAL · **Owner:** Thomas · **Dependencies:** 10, 12 · **Requirements:** FR-15, FR-16, FR-17, FR-38

**CRITICAL: An ACTIVE reservation becomes EXPIRED when `now >= min(end, start + pickupWindow)`. Run it at startup, every 60 seconds on a daemon scheduler thread, and at the start of the search/create/cancel/checkout transactions. Tests use MutableClock and NEVER sleep.**

---

### Why this is critical
This is the first time-dependent behaviour and the first background thread touching the DB and firing model events (an architectural transition).
- **Failure indicators:** off-by-one at the exact deadline; the scheduler thread dying silently after an exception; Swing updated off the EDT; tests that depend on wall-clock time.
- **Mitigations:** catch and log every exception inside the scheduled task (an uncaught exception cancels a ScheduledExecutorService task forever); fire `RESERVATIONS` only when count > 0; JFrameView already marshals to the EDT.
- **Rollback:** disable `start()` (startup + before-operation expiration still satisfy most of FR-16).

### Capability (`ExpirationService`)
- `expireOverdue(Connection c)`:
  - read the window from SettingsRepository
  - for each ACTIVE reservation with `start_time <= now`: compute the deadline; if `now >= deadline`, run a guarded update ACTIVE→EXPIRED
  - return the count
- `expireOverdue()`: own transaction, then fires.
- `start()`: `Executors.newSingleThreadScheduledExecutor(daemon thread named "expiration")`, `scheduleAtFixedRate(this::expireOverdue, 0, 60, SECONDS)`. `stop()`: shutdownNow.
- Make sure the search (11), create (12), cancel (15), and checkout (16) transactions call `expireOverdue(c)` first.

### Tests to write FIRST (`test/ExpirationTest.java`)
- window 30, start 09:00: at 09:29 still ACTIVE; **at 09:30 EXPIRED** (exact boundary); short reservation 09:00–09:10 expires at 09:10
- a CHECKED_OUT reservation never expires
- the window changed to 60 → the deadline moves
- an expired item becomes reservable again for the same period (FR-17); checkout of an expired reservation is rejected (verify once 16 exists)
- FR-38: create a reservation, close the DB, reopen with the clock moved 2 hours later, construct the system → it's EXPIRED after the startup sweep
- the scheduler: `start()` then `stop()` with no exceptions (don't wait 60 s; call the task method directly for the logic)

---
**CRITICAL: Exact deadline rule (≥), injectable clock, robust daemon scheduler, expire-before-operation, startup sweep.**
