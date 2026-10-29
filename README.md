# Equipment Reservation and Checkout System

CS 4398.001 · Fall 2026 · **Group 9**: Sanskriti Poudel, Thomas Platt, Theo Kliewer
**Final submission: 2026-12-08**

A Java Swing desktop app for reserving, checking out, returning, and maintaining shared equipment. It uses MVC, local SQLite, and JUnit 5.
Requirements: [`ctxt/SRS_Group9.pdf`](ctxt/SRS_Group9.pdf) · course and project digest: [`ctxt/PROJECT_REFERENCE.md`](ctxt/PROJECT_REFERENCE.md)

## Quick start
```bash
./gradlew build                 # compile + run all tests
./gradlew run --args="--demo"   # start the app with sample equipment
```
Default accounts (created on first run): `admin1 / admin123` (administrator), `jsmith / password1`, `mlee / password2` (borrowers).
Requires only Java 17+. On Windows use `gradlew.bat`.

## Repository layout
| Path | Contents |
|---|---|
| `src/main/java/equipmentReservation/` | `model` (+ `model/persistence`), `view`, `controller`, `util`, `Main` |
| `src/test/java/test/` | JUnit 5 tests |
| `prompts/` | **Module of Thought prompts**: `00_master_context.md` + one prompt per increment (a graded deliverable) |
| `ctxt/` | Reference material for agents. Only our SRS is in git; course materials stay local (see below) |
| `docs/` | `tracking.md` (MoT tracking matrix), `uml/`, `acceptance/`, `screenshots/` |

## Local-only course materials (not in git)
The professor's course materials are **never committed** (`.gitignore` blocks them). After cloning, each teammate copies them from the course files export into `ctxt/` so the agent can read them:

| Put here | Taken from the course export |
|---|---|
| `ctxt/MVC_JohnHunt.md` | `MoTtutorial/JavaAcctMgr/Context/MVC_JohnHunt.md` |
| `ctxt/Module_of_Thought_instr.md` | `MoTtutorial/JavaAcctMgr/Context/Module of Thought_instr.md` |
| `ctxt/reference/calcProj/` | the course site's `codeEx/calcProj.zip` (its `src` folder) |
| `ctxt/reference/JavaAcctMgr-prompts/` | the `prompts/` folder inside `MoTtutorial/JavaAcctMgr/JavaAcctMgr.zip` |

Sanskriti can also send you her local `ctxt/PROJECT_REFERENCE.md` digest privately.

## How we build: Module of Thought with an AI agent
1. Pick the next increment assigned to you in the table below and create a branch: `git checkout -b inc/12-reservation-rules`.
2. Start the agent in the repo root (e.g. `claude`) and say:
   > Read prompts/00_master_context.md, then do ONLY prompts/12_reservation_rules.md. Write the tests first, implement, and run ./gradlew build.
3. **Review the diff yourself.** Run `./gradlew build` and try the behaviour. Ask the agent to fix what's wrong, in the same session.
4. If you had to give the agent extra instructions, append them to the prompt file under `### Follow-up prompts used`. **We must submit every prompt.**
5. Update `docs/tracking.md`, commit the code **and** the prompt, push, and open a PR. A teammate reviews it, then you merge into `main`.
6. Never commit straight to `main`. One increment per PR keeps the git Network graph readable (it goes in the final slides).

## Increments
Work at your own pace. Just respect the **Depends on** column, because a step can't start until those are merged. Owners are a default split, so swap freely.
★ = critical checkpoint (risk, mitigation and rollback are written in the prompt)

| # | Increment | Owner | Depends on |
|---|---|---|---|
| 01–02 | Project structure, Gradle | Sanskriti | ✅ done |
| 03 | MVC framework (John Hunt) | Sanskriti | 02 |
| 04 ★ | Model API skeleton (all review!) | Sanskriti | 03 |
| 05 | DateTimes, PasswordHasher | Sanskriti | 04 |
| 06 ★ | Database + transactions | Sanskriti | 04 |
| 07 | Repositories + seeding | Sanskriti | 05, 06 |
| 08 | ReservationSystem, Session, test fixture | Thomas | 05–07 |
| 09 | Equipment admin, Return to Service | Theo | 08 |
| 10 | Settings (pickup window) | Theo | 08 |
| 11 | Catalog search + availability | Thomas | 08 |
| 12 ★ | Reservation rules + conflicts | Thomas | 08, 11 |
| 13 ★ | Concurrent reservation safety | Thomas | 12 |
| 14 ★ | Expiration | Thomas | 10, 12 |
| 15 | Cancellation, history, admin search | Thomas | 12, 14 |
| 16 ★ | Checkout, return, overdue | Theo | 09, 12, 14 |
| 17 | Maintenance | Theo | 09, 16 |
| 18 | Persistence & recovery tests | Theo | 12–17 |
| 19 ★ | Main, login, navigation | Sanskriti | 03, 08, 14 |
| 20 | Catalog + reservation form screens | Thomas | 11, 12, 19 |
| 21 | My Reservations screen | Thomas | 15, 20 |
| 22 | Equipment management + settings screens | Theo | 09, 10, 19 |
| 23 | Checkout & return screen | Theo | 16, 19 |
| 24 | Maintenance screen | Theo | 17, 19 |
| 25 | Performance test (NFR-03) | Sanskriti | 11, 12 |
| 26 | Acceptance tests, macOS + Windows | All | 19–24 |
| 27 | Final UML, statecharts, Javadoc, slides, archive | Sanskriti + all | all |

While the foundation (03–07) is being built, Thomas and Theo should review `docs/uml/` and the master context API, then read the professor's JavaAcctMgr example prompts (local copy in `ctxt/reference/JavaAcctMgr-prompts/`).
