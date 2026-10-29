# INCREMENT 22: Equipment Management and Settings Screens
**Status:** Standard · **Owner:** Theo · **Dependencies:** 09, 10, 19 · **Requirements:** FR-20–FR-23, FR-15 (UI), NFR-04

**CRITICAL: Match SRS Figure 5. The ID field is editable only for a New Item and read-only after saving (FR-21). Return to Service is enabled only for Non-operational items and asks for the condition (Good/Fair).**

---

### Capability
- `EquipmentManagementView` + controller:
  - a list table (ID, Name, Status) on the left; a form on the right (ID, Name, Category as an editable combo, Description, Condition combo, Status combo)
  - New Item, Settings…, Return to Service, and Save buttons
  - Save → add or update depending on mode; errors are field-highlighted
  - Return to Service → a dialog to choose Good/Fair, then `returnToService`
- `SettingsView` + controller: a pickup window spinner/field (minutes) and Save; a validation error for ≤ 0.

### Verification (manual + screenshots)
- [ ] add a duplicate ID → error; blank name → field error
- [ ] Return to Service flow on a Non-operational item, Figure 5
- [ ] settings: 0 rejected, 45 saved and shown again after restarting the app

---
**CRITICAL: Figure 5 layout, immutable IDs after save, Return to Service rules surfaced in the UI, admin-only screens.**
