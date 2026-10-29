# INCREMENT 15: Cancellation, Borrower History and Admin Search
**Status:** Standard · **Owner:** Thomas · **Dependencies:** 12, 14 · **Requirements:** FR-18, FR-19, FR-24

**CRITICAL: A borrower may cancel ONLY their own ACTIVE reservation. Borrowers see ONLY their own history. Admins can search everything.**

---

### Capability
- `cancelReservation(id)`:
  - borrower only
  - expire first
  - not found → NotFoundException
  - another borrower's reservation → `AuthorizationException("You can only cancel your own reservations.")`
  - status ≠ ACTIVE → `InvalidOperationException("Only active reservations can be cancelled (this one is Checked out).")`
  - guarded update ACTIVE→CANCELLED; fire `RESERVATIONS`
- `getMyHistory()`: borrower; all of their reservations (every status) with the Checkout if any, newest start first.
- `searchReservations(borrower, equipmentId, status)`: admin; nulls mean any; borrower is a case-insensitive partial match.
- `getActiveReservationsFor(equipmentId)`: admin; ACTIVE only, ordered by start.

### Tests to write FIRST (`test/CancellationHistoryTest.java`)
- cancel own ACTIVE → CANCELLED, still in history, item available again (FR-18)
- cancel someone else's, a CHECKED_OUT, a COMPLETED, or an EXPIRED reservation → rejected, status unchanged
- jsmith's history doesn't contain mlee's reservations; it includes checkout/return times once 16 exists (FR-19)
- admin search by borrower, item, and status, plus combinations (FR-24); borrower calling it → AuthorizationException

---
**CRITICAL: Own + ACTIVE only for cancel, history isolation per borrower, admin-only global search.**
