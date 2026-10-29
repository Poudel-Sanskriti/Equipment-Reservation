# INCREMENT 20: Catalog Screen and Reservation Form
**Status:** Standard · **Owner:** Thomas · **Dependencies:** 11, 12, 19 · **Requirements:** FR-04–FR-14 (UI), FR-39, NFR-04

**CRITICAL: Match SRS Figures 2 and 3. Input dates use `yyyy-MM-dd HH:mm`. Rejections show the model's message, and the offending field is highlighted while the user's valid input is KEPT (NFR-04). The table refreshes on RESERVATIONS/EQUIPMENT/MAINTENANCE/CHECKOUTS events.**

---

### Capability
- `CatalogView` (+ `CatalogController`):
  - Name field, Category combo ("All categories" + `listCategories()`), Start and End fields, Search, Clear
  - a `JTable` (non-editable `AbstractTableModel`) with ID, Name, Category, Condition, Status, Availability for period
  - My Reservations and Reserve Selected… buttons; the common header
  - search with no rows → "No equipment matches your search." in a label (FR-04)
  - a date parse error → `showFieldError("start", "Use the format YYYY-MM-DD HH:mm …")`
  - `refresh(e)` re-runs the last search
  - for admins (opened from AdminHome), hide "Reserve Selected…" and "My Reservations"
- `ReservationFormView` (+ `ReservationFormController`), one instance per request:
  - shows the item, Start/End prefilled from the catalog period
  - the inline red error label; Cancel; Confirm Reservation
  - success → `showInfo("Reservation R-2041 confirmed. Pick up by 2026-11-12 09:30.")` and close
  - failure → the message, with the field highlighted (red border on the field named by `ValidationException.getField()`)

### Verification (manual, take screenshots for docs/screenshots/)
- [ ] search "cam" (all four availability states visible with `--demo` data plus some setup)
- [ ] end before start shows the field error (Figure 3)
- [ ] a reservation success message shows the pickup deadline
- [ ] two app windows: a reservation in one updates the catalog in the other via the model event

---
**CRITICAL: SRS layout, strict date format, field-level errors that keep input, event-driven refresh, borrower-only reserve.**
