# MoT Tracking Matrix

Thresholds: see `prompts/00_master_context.md` §6. 🟢 green · 🟡 yellow · 🔴 red.

| Inc | Name | Owner | Date | Build | Tests (pass/total) | Requirements covered | Status | Notes |
|---|---|---|---|---|---|---|---|---|
| 01 | Project structure | Sanskriti | 2026-10-29 | 🟢 | n/a | NFR-05 | ✅ | Packages + .gitkeep |
| 02 | Gradle build | Sanskriti | 2026-10-29 | 🟢 | 0/0 | NFR-09 (fatJar) | ✅ | Wrapper 8.14.3, Java 17, sqlite-jdbc 3.46.1.3, JUnit 5.10 |
| 03 | MVC framework | Sanskriti | | | | | ⬜ | |
| 04 ★ | Model skeleton | Sanskriti | | | | | ⬜ | |
| 05 | Utilities | Sanskriti | | | | | ⬜ | |
| 06 ★ | Database/transactions | Sanskriti | | | | | ⬜ | |
| 07 | Repositories | Sanskriti | | | | | ⬜ | |
| 08 | Session/auth | Thomas | | | | | ⬜ | |
| 09 | Equipment admin | Theo | | | | | ⬜ | |
| 10 | Settings | Theo | | | | | ⬜ | |
| 11 | Catalog | Thomas | | | | | ⬜ | |
| 12 ★ | Reservation rules | Thomas | | | | | ⬜ | |
| 13 ★ | Concurrency | Thomas | | | | | ⬜ | 50/50 repetitions required |
| 14 ★ | Expiration | Thomas | | | | | ⬜ | |
| 15 | Cancellation/history | Thomas | | | | | ⬜ | |
| 16 ★ | Checkout/return | Theo | | | | | ⬜ | |
| 17 | Maintenance | Theo | | | | | ⬜ | |
| 18 | Persistence tests | Theo | | | | | ⬜ | |
| 19 ★ | Main/login/navigation | Sanskriti | | | | | ⬜ | |
| 20 | Catalog + reserve UI | Thomas | | | | | ⬜ | |
| 21 | My Reservations UI | Thomas | | | | | ⬜ | |
| 22 | Equipment + settings UI | Theo | | | | | ⬜ | |
| 23 | Checkout/return UI | Theo | | | | | ⬜ | |
| 24 | Maintenance UI | Theo | | | | | ⬜ | |
| 25 | Performance | Sanskriti | | | | | ⬜ | |
| 26 | Acceptance tests | All | | | | | ⬜ | |
| 27 | Final docs | Sanskriti | | | | | ⬜ | |
