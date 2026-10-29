# INCREMENT 27: Final UML, Statecharts, Javadoc and Submission Archive
**Status:** Standard · **Owner:** Sanskriti (+ ALL for slides) · **Dependencies:** everything · **Requirements:** professor deliverables 1–10

**CRITICAL: The final class diagram must be REVERSE-ENGINEERED from the code with a deterministic tool and match it. Submit ONE archive by 2026-12-08 that keeps the project directory structure.**

---

### Capability: checklist (professor's list, `ctxt/PROJECT_REFERENCE.md` §1.3)
1. [ ] **Prompts**: the `prompts/` folder (all increments) + `docs/tracking.md`
2. [ ] **Acceptance test descriptions with screenshots**: `docs/acceptance/` (increment 26)
3. [ ] **UML class diagram**: reverse-engineered (IntelliJ Diagrams, VP-UML, or a PlantUML generator); export PNG/PDF to `docs/uml/`; also the per-package diagrams (model, view, controller)
4. [ ] **Statechart diagrams**: `docs/uml/statechart-reservation.puml` and `statechart-equipment.puml`, updated to match the final behaviour, exported to PNG
5. [ ] **Text description** `docs/architecture.md`: packages, MVC flow, threading (EDT + expiration scheduler), and the critical algorithms (availability precedence, half-open overlap, the transaction/lock design that guarantees FR-12, the expiration deadline rule)
6. [ ] **Source code**: the repository
7. [ ] **Javadoc**: `./gradlew javadoc` → zip `build/docs/javadoc`
8. [ ] **JUnit tests**: `src/test/java/test` + an `./gradlew test` report screenshot (`build/reports/tests/test/index.html`)
9. [ ] **Presentation (.pptx)**: purpose, class diagram/structure, statecharts, acceptance test cases, the **GitHub "Network" graph screenshot** (Insights → Network), the MoT process and tracking matrix, and a demo plan
10. [ ] Archive: `git archive` or a clean copy without `build/` and `.gradle/`, plus the javadoc zip and the slides → `Group9_EquipmentReservation.zip`; one member uploads

---
**CRITICAL: Reverse-engineered final UML consistent with the code, all 10 deliverables present, one archive, submitted by Dec 8.**
