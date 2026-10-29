# INCREMENT 26: Acceptance Tests Through the GUI
**Status:** Standard · **Owner:** ALL (Sanskriti coordinates) · **Dependencies:** 19–24 · **Requirements:** SRS §7, all FR/NFR, professor deliverable #2

**CRITICAL: For every SRS §7 test area, write a step-by-step acceptance test case and execute it on BOTH macOS and Windows (NFR-09), with a screenshot of EVERY window and pop-up along the way. Record the pickup window used.**

---

### Capability
- `docs/acceptance/AT-xx-<name>.md`, one per case, using this template:
  ```
  # AT-03 Reservation conflict          Requirements: FR-11, FR-14
  Preconditions: fresh DB with --demo, pickup window = 30, clock = real time
  | Step | Action | Expected | Actual | Screenshot |
  Result: PASS/FAIL · Platform: macOS 15 / Java 17.0.12 · Tester · Date
  ```
- The minimum set (from the SRS §7 table):
  1. AT-01 sign-in and roles (invalid credentials, borrower vs admin menus, sign out)
  2. AT-02 catalog search and filters
  3. AT-03 reservation conflict, plus the validation errors (Figure 3)
  4. AT-04 concurrent requests: reference the JUnit evidence and run two app instances on the same DB file, clicking Confirm at the same time
  5. AT-05 expiration: pickup window 1 minute
  6. AT-06 cancellation and history
  7. AT-07 equipment admin and Return to Service
  8. AT-08 checkout and return (including Damaged), and overdue
  9. AT-09 maintenance
  10. AT-10 persistence: close and reopen the app
  11. AT-11 input handling and usability
  12. AT-12 performance (link to 25)
- `docs/acceptance/traceability.md`: FR/NFR → JUnit test classes → AT cases.
- Build the jar with `./gradlew fatJar` and test the jar itself on Windows (`java -jar build/libs/equipment-reservation-1.0-SNAPSHOT-all.jar --demo`).

---
**CRITICAL: Every SRS §7 area covered, both OSes, a screenshot of every window and pop-up, a traceability table.**
