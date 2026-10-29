# INCREMENT 10: Settings (Pickup Window)
**Status:** Standard · **Owner:** Theo · **Dependencies:** 08 · **Requirements:** FR-15 (window part), SRS §2.4

**CRITICAL: The pickup window is a positive whole number of minutes, default 30, and only an administrator may change it.**

---

### Capability (`SettingsService`)
- `getPickupWindowMinutes()` reads `settings.pickup_window_minutes`, default 30.
- `setPickupWindowMinutes(int)`:
  - admin only
  - `<= 0` → `ValidationException("pickupWindow", "Pickup window must be a positive number of minutes.")`
  - an upper bound of 1440 (one day) is a sensible sanity limit; same message style
  - persists and fires `SETTINGS`

### Tests to write FIRST (`test/SettingsServiceTest.java`)
- the default is 30 on a fresh DB; the value persists across a reopen
- 0, −5, and 1441 are rejected; 1 and 1440 are accepted (boundary values)
- a borrower setting it → AuthorizationException

---
**CRITICAL: Positive, admin-only, persisted, default 30.**
