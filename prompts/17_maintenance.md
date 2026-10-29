# INCREMENT 17: Maintenance Records
**Status:** Standard · **Owner:** Theo · **Dependencies:** 09, 16 · **Requirements:** FR-31, FR-32, FR-33, FR-34

**CRITICAL: Opening maintenance is rejected for a checked-out item or one already under maintenance. Open maintenance blocks NEW reservations and checkouts but does NOT cancel existing reservations. Closing records the return time and leaves reservations untouched.**

---

### Capability (`MaintenanceService`)
- `openMaintenance(equipmentId, reason)`:
  - admin only
  - reason blank → ValidationException("reason", …)
  - not found → NotFoundException
  - open checkout → `InvalidOperationException("EQ-113 is checked out; record the return first.")`
  - already open → InvalidOperationException
  - insert (openedAt = now, openedBy = admin); fire `MAINTENANCE`, `EQUIPMENT`
  - **FR-34:** the confirmation step happens in the UI. The controller first calls `reservationService.getActiveReservationsFor(id)` and shows them, then calls open on confirm.
- `closeMaintenance(recordId)`: admin only; already closed → InvalidOperationException; sets closedAt/closedBy; fire events.
- `listOpen()`, `listFor(equipmentId)`.

### Tests to write FIRST (`test/MaintenanceTest.java`)
- open → item UNDER_MAINTENANCE in the catalog; reserve → rejected; checkout of an existing ACTIVE reservation → rejected (FR-32, FR-34)
- existing ACTIVE reservations remain ACTIVE after open and after close; they can still be cancelled, and they still expire (FR-34)
- open rejected for a checked-out item and for a duplicate open (FR-33)
- close records the time; the item is reservable again; a second close is rejected
- a borrower → AuthorizationException

---
**CRITICAL: Block new reservations and checkouts, never cancel existing ones, reject when checked out or already open, admin only.**
