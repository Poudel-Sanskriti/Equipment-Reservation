# INCREMENT 09: Equipment Administration and Return to Service
**Status:** Standard · **Owner:** Theo · **Dependencies:** 08 · **Requirements:** FR-03, FR-20, FR-21, FR-22, FR-23

**CRITICAL: Admin-only writes. Equipment IDs are unique and immutable. Updates never touch history tables. Return to Service is allowed ONLY for NON_OPERATIONAL items without open maintenance, with condition GOOD or FAIR, and must write a `service_log` row.**

---

### Capability (`EquipmentService`)
- `addEquipment(e)`:
  - admin only
  - id, name, and category are trimmed and must be non-blank → `ValidationException(field, "<Field> is required.")`
  - a duplicate id → `ConflictException("Equipment ID EQ-104 already exists.")`
  - fire `EQUIPMENT`
- `updateEquipment(e)`: admin only; unknown id → `NotFoundException`; same validation; updates name, category, description, condition, and status only.
- `getEquipment`, `listEquipment`, `listCategories`: any signed-in user.
- `returnToService(id, condition)`, all in one transaction:
  - admin only
  - item must be NON_OPERATIONAL, else `InvalidOperationException`
  - condition DAMAGED → `ValidationException("condition", "Choose Good or Fair to return an item to service.")`
  - open maintenance → `InvalidOperationException("Close the open maintenance record first.")`
  - sets condition, sets status OPERATIONAL, and inserts `service_log` with `now()` and the admin

### Tests to write FIRST (`test/EquipmentServiceTest.java`)
- a borrower add/update → AuthorizationException (FR-20)
- duplicate id; blank name/category; an invalid enum can't happen by type, so assert the DB CHECK constraint instead (FR-21)
- an update keeps the existing reservations of the item (create a reservation row via the repository, update the item, the reservation still exists) (FR-22)
- return to service: happy path writes a service_log row with time and admin; rejected for OPERATIONAL items, DAMAGED condition, and open maintenance (FR-23)

### Verification
- [ ] Tests green; each test's Javadoc names its FR id

---
**CRITICAL: Admin only, unique immutable IDs, never touch history, Return to Service rules + service_log.**
