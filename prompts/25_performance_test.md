# INCREMENT 25: Performance Test (NFR-03)
**Status:** Standard · **Owner:** Sanskriti · **Dependencies:** 11, 12 · **Requirements:** NFR-03

**CRITICAL: With 1,000 items and 5,000 reservations, 20 consecutive catalog searches and 20 reservation checks must EACH finish within 3 seconds. Report the machine and the Java version.**

---

### Capability
- `test/PerformanceTest.java`, tagged `@Tag("performance")` (excluded from the normal `test` task; add a `performanceTest` Gradle task that includes the tag):
  - bulk-insert 1,000 items and 5,000 reservations in ONE transaction (fast setup)
  - time 20 `search(null, null, start, end)` calls and 20 `createReservation` attempts (mix of conflicts and successes), each with `System.nanoTime`
  - assert the max ≤ 3 s; print min/avg/max, `System.getProperty("os.name")`, `java.version`, and the CPU count
- Save the output to `docs/acceptance/performance-report.md` (one run on macOS and one on Windows).
- If slow: check that the indexes from master context §4 exist and that availability is computed with set queries (one query for open maintenance, one for open checkouts, one for overlapping reservations), not N+1 queries.

---
**CRITICAL: 1,000 / 5,000 data set, 20 consecutive timings each ≤ 3 s, documented environment.**
