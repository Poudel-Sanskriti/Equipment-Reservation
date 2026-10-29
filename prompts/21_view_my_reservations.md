# INCREMENT 21: My Reservations Screen
**Status:** Standard · **Owner:** Thomas · **Dependencies:** 15, 20 · **Requirements:** FR-18, FR-19 (UI)

**CRITICAL: Match SRS Figure 4. The Active tab shows ACTIVE and CHECKED_OUT with a "Pick up by" column. The History tab shows everything. Cancel only works on the selected ACTIVE reservation and asks for confirmation first.**

---

### Capability
- `MyReservationsView` + controller:
  - `JTabbedPane` with Active and History tabs
  - columns: Reservation, Item, Start, End, Pick up by (Active), Status, Checked out, Returned (History)
  - Back to Catalog; Cancel Selected Reservation, with a confirm dialog "Cancel R-2041 for EQ-104 Canon EOS Camera?"
  - refreshes on RESERVATIONS/CHECKOUTS events, so an expiration while the screen is open moves the row to History automatically

### Verification (manual + screenshots)
- [ ] cancel → the row moves to History as Cancelled
- [ ] cancel with nothing selected → "Select a reservation first."
- [ ] set the pickup window to 1 minute (admin), create a reservation starting now, wait ~1–2 min → it shows Expired without any click (scheduler + EDT event)

---
**CRITICAL: Figure 4 layout, confirm before cancel, live refresh from the background expiration.**
