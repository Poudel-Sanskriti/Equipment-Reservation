# INCREMENT 24: Maintenance Screen
**Status:** Standard · **Owner:** Theo · **Dependencies:** 17, 19 · **Requirements:** FR-31–FR-34 (UI)

**CRITICAL: Match SRS Figure 7. Before opening maintenance, show the item's active reservations in a "Confirm Maintenance" dialog explaining that they stay in place but can't be checked out while maintenance is open (FR-34).**

---

### Capability
- `MaintenanceView` + controller:
  - an Item combo, a Reason field, and Open Maintenance
  - a table of open records (Item, Reason, Started) with a Close action (button for the selected row)
  - on Open: fetch `getActiveReservationsFor(item)`; if there are any, show the confirmation dialog with a table (Reservation, Borrower, Start) and Cancel/Confirm; if there are none, a simple confirm
  - refreshes on MAINTENANCE events

### Verification (manual + screenshots)
- [ ] the confirmation lists the affected reservations (Figure 7)
- [ ] after opening, the catalog shows "Unavailable – under maintenance"; after closing it is available again
- [ ] opening for a checked-out item → error

---
**CRITICAL: Figure 7 confirmation with affected reservations, reservations kept, live refresh.**
