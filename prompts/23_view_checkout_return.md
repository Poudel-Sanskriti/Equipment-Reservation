# INCREMENT 23: Checkout and Return Screen
**Status:** Standard · **Owner:** Theo · **Dependencies:** 16, 19 · **Requirements:** FR-24–FR-30 (UI)

**CRITICAL: Match SRS Figure 6. There are three tabs, Checkout, Return, and Overdue, and every successful action shows a confirmation line with the time.**

---

### Capability
- `CheckoutReturnView` + controller:
  - **Checkout tab:** a Search field (borrower/item) and a Status combo (default Active), Find, a results table (Reservation, Borrower, Item, Start, Pick up by, Status), Check Out Selected, and a status line "R-2041 checked out at 2026-11-12 09:12."
  - **Return tab:** a table of open checkouts, a Return condition combo (Good/Fair/Damaged), and Record Return. A DAMAGED return warns that "the item will be marked Non-operational".
  - **Overdue tab:** Borrower, Item, Scheduled end, Overdue by (e.g. "2 h 15 min"), and Refresh.
  - refreshes on CHECKOUTS/RESERVATIONS events.

### Verification (manual + screenshots)
- [ ] the full flow: reserve as jsmith → sign in as admin1 → check out → return Good → the history shows both times
- [ ] checkout too early / expired → a clear error dialog
- [ ] overdue list populated (use a short reservation and wait, or demo data with past times inserted via a seed)

---
**CRITICAL: Figure 6 layout, three tabs, confirmation messages, Damaged-return warning, live refresh.**
