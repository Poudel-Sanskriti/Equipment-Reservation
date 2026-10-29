# INCREMENT 16: Checkout, Return and Overdue List
**Status:** ★ CRITICAL · **Owner:** Theo · **Dependencies:** 09, 12, 14 · **Requirements:** FR-25, FR-26, FR-27, FR-28, FR-29, FR-30

**CRITICAL: Checkout needs an ACTIVE reservation with `start <= now < pickupDeadline` and an operational item that is not under maintenance and not already checked out. Return works only on an open checkout, completes the reservation, and a DAMAGED return makes the item NON_OPERATIONAL. Each operation is one transaction.**

---

### Why this is critical
This integrates three subsystems (reservations, equipment state, maintenance), and the state transitions are easy to get half-right.
- **Failure indicators:** a checkout allowed exactly at the deadline (must be strictly before); a second return accepted; the equipment condition not updated; the reservation left CHECKED_OUT after a return.
- **Isolation:** test checkout alone, then return alone (with the checkout row inserted via the repository), then the full flow.
- **Rollback:** revert the branch; the stubs keep the GUI work unblocked.

### Capability (`CheckoutService`)
- `checkOut(reservationId)`, admin only:
  - expire first
  - not found → NotFoundException
  - status ≠ ACTIVE → InvalidOperationException (with the expired case named explicitly: "Reservation R-2041 has expired and cannot be checked out.")
  - `now < start` → InvalidOperationException("Too early: pickup starts at …")
  - `now >= deadline` → expired (the sweep should have caught it, but guard anyway)
  - item NON_OPERATIONAL / open maintenance / open checkout → InvalidOperationException (FR-26)
  - insert a checkout (time, admin), status ACTIVE→CHECKED_OUT (FR-27); fire `CHECKOUTS` + `RESERVATIONS`
- `returnItem(reservationId, condition)`, admin only:
  - no open checkout → `InvalidOperationException("R-2041 has no open checkout (already returned?).")` (FR-28)
  - otherwise mark returned (time, condition, admin), reservation CHECKED_OUT→COMPLETED, equipment.condition = condition, and if DAMAGED set status NON_OPERATIONAL (FR-29)
  - fire `CHECKOUTS`, `RESERVATIONS`, `EQUIPMENT`
- `listOpenCheckouts()`; `listOverdue()`: open checkouts with `end < now`, `overdueBy = Duration.between(end, now)` (FR-30). Passing the end time NEVER auto-closes a checkout.

### Tests to write FIRST (`test/CheckoutReturnTest.java`)
- checkout at start (09:00) ok; at 09:29 ok; **at 09:30 (deadline) rejected**; before start rejected
- checkout rejected for maintenance, non-operational, already checked out (a second reservation of the same item can't exist due to overlap, so insert the conflicting state via the repository)
- the borrower can't check out (admin only)
- return: COMPLETED, times recorded; second return rejected; DAMAGED → NON_OPERATIONAL and not reservable until Return to Service (09)
- after a return, a future overlapping reservation by someone else is still respected (FR-29)
- overdue: the clock past end → listed with the right duration; returned items are not listed

---
**CRITICAL: start ≤ now < deadline, admin only, one transaction each, DAMAGED → NON_OPERATIONAL, no auto-close, second return rejected.**
