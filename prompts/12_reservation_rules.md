# INCREMENT 12: Reservation Creation Rules and Conflict Detection
**Status:** ★ CRITICAL · **Owner:** Thomas · **Dependencies:** 08, 11 · **Requirements:** FR-08, FR-09, FR-10, FR-11, FR-13, FR-14

**CRITICAL: ALL checks AND the insert happen inside ONE `db.inTransaction(...)`, starting with `expiration.expireOverdue(c)`. Every rejection throws a ReservationSystemException subclass whose message tells the borrower exactly what is wrong.**

---

### Why this is critical
This is the first complex business operation and the heart of the system. Boundary errors here (≤ vs <) are the most likely bugs.
- **Failure indicators:** adjacent reservations rejected; an equal start/end accepted; an expired reservation still blocking; the check done outside the transaction.
- **Isolation protocol:** test each rule alone with a fresh `TestSystem`, then combinations.
- **Rollback:** revert the branch; the stubs keep the build green.

### Capability (`ReservationService.createReservation(equipmentId, start, end)`): borrower only. Check in this order:
1. start/end null → `ValidationException("start"/"end", "... is required.")` (FR-08)
2. `start < now` → `ValidationException("start", "Start time cannot be in the past.")`. Start == now is allowed.
3. `end <= start` → `ValidationException("end", "End time must be later than the start time.")`
4. `end - start > 7 days` → `ValidationException("end", "A reservation can be at most 7 days long.")` (FR-10). Exactly 7 days is allowed.
5. unknown item → `NotFoundException`
6. NON_OPERATIONAL / open maintenance / open checkout → `InvalidOperationException("EQ-112 is not available: under maintenance.")` (FR-09)
7. the borrower already has 3 ACTIVE → `InvalidOperationException("You already have 3 active reservations (the maximum).")` (FR-10)
8. overlap with ACTIVE/CHECKED_OUT → `ConflictException("EQ-104 is already reserved from 2026-11-12 09:00 to 2026-11-12 17:00.")` (FR-11)
9. insert with status ACTIVE, createdAt = now, borrower = session user (FR-13); after commit fire `RESERVATIONS`; return the Reservation

### Tests to write FIRST (`test/ReservationRulesTest.java`): one test per rule plus these boundaries
- start == now accepted; start = now − 1 min rejected
- end == start rejected; duration exactly 7 days accepted, 7 days + 1 min rejected
- 3 ACTIVE ok, 4th rejected; after cancelling one (direct status update via the repository) a new one is ok
- overlap variants: identical, containing, contained, left/right partial → rejected; adjacent before/after → accepted
- an overlap with a CANCELLED/EXPIRED/COMPLETED reservation → accepted
- a failed request leaves the row count unchanged (FR-37)

### Verification
- [ ] All the rules above have a named test with the FR id in its Javadoc; the build is green

---
**CRITICAL: One transaction, expire first, exact boundary semantics (start ≥ now, end > start, ≤ 7 days, < 3 active, half-open overlap), clear messages.**
