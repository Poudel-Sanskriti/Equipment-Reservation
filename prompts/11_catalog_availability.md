# INCREMENT 11: Catalog Search and Availability
**Status:** Standard · **Owner:** Thomas · **Dependencies:** 08 (and 07 repos) · **Requirements:** FR-04, FR-05, FR-06, FR-07, NFR-03 (keep queries indexed)

**CRITICAL: Availability is computed with the precedence in master context §5 (FR-06/07): NON_OPERATIONAL → UNDER_MAINTENANCE → CHECKED_OUT → RESERVED → AVAILABLE. Search runs `expireOverdue(c)` first, inside the same read/transaction (FR-16).**

---

### Capability (`CatalogService.search(name, category, start, end)`)
- Any signed-in user.
- `name`: case-insensitive partial match (`LOWER(name) LIKE LOWER(?)` with `%` escaping); null or blank = all.
- `category`: exact match; null or "All categories" handled by the caller = all.
- Period: both null → availability "now" (overlap with [now, now+1min)). Exactly one null → `ValidationException`. `end <= start` → `ValidationException("end", "End time must be later than the start time.")`.
- Returns `CatalogEntry` objects ordered by equipment id. An empty list is legitimate; the view shows "No equipment matches your search." (FR-04).
- Use `inTransaction` (not `read`), because the expiration sweep may write.

### Tests to write FIRST (`test/CatalogServiceTest.java`)
- "CAM" matches "Canon EOS Camera" and "Webcam HD" (FR-04); "zzz" → empty
- category filter (FR-05)
- availability for each Availability value, one item each (FR-06/07); precedence: a NON_OPERATIONAL item that also has maintenance → NON_OPERATIONAL
- adjacent reservation [09:00,11:00) and query [11:00,12:00) → AVAILABLE
- an expired reservation (advance the clock past the pickup deadline) no longer makes the item RESERVED (FR-17)

---
**CRITICAL: Precedence order, case-insensitive partial match, adjacency is available, expire before computing.**
