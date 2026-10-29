# INCREMENT 04: Model API Skeleton (declarations + Javadoc + stubs)
**Status:** ★ CRITICAL · **Owner:** Sanskriti (reviewed by ALL before merge) · **Dependencies:** 03 · **Requirements:** NFR-05, NFR-06, all FR (API surface)

**CRITICAL: Create EVERY model, persistence and util type in master context §3.2–§3.7 with exact signatures, full Javadoc, and stub bodies that `throw new UnsupportedOperationException("TODO increment NN")`. The code MUST compile. NO business logic in this increment.**

---

### Why this is critical
This is the professor's "skeleton first" step, and it **freezes the contract** so Thomas and Theo can implement services in parallel. A wrong signature here means painful merges later.
- **Risk:** the API drifts from the master context, or business logic sneaks into the skeleton.
- **Failure indicators:** compile errors when later prompts call the contract; teammates changing signatures in their own branches.
- **Mitigation:** all three review the PR against §3 line by line before merging.
- **Rollback:** revert the merge commit; fix the contract in `00_master_context.md` first, then regenerate.

### Capability
1. Enums (§3.2), each with `label()`. **Implement** these fully; they are trivial.
2. Entities (§3.3). **Implement** constructors, getters, equals/hashCode, and `Reservation.getDisplayId/pickupDeadline/overlaps/blocksAvailability`. These are pure functions and good first tests.
3. Exceptions (§3.4). **Implement** fully.
4. Services (§3.5), `ReservationSystem`, `Session`, persistence types (§3.6), util (§3.7): **stubs only**, with Javadoc saying which FR each method satisfies and which exceptions it throws and why.
5. Run `./gradlew javadoc` (initial API docs, all scopes).

### Tests to write FIRST (`test/EntityTest.java`)
- `pickupDeadline`: start 09:00, end 17:00, window 30 → 09:30. Start 09:00, end 09:15, window 30 → 09:15 (min rule).
- `overlaps`: [9,11) vs [10,12) true; [9,11) vs [11,12) **false** (adjacent); [9,11) vs [8,9) false; containment true.
- `blocksAvailability`: true only for ACTIVE and CHECKED_OUT.
- `getDisplayId()` of id 2041 → `"R-2041"`.
- `ValidationException.getField()` returns its field.

### Verification
- [ ] Compiles; `EntityTest` green; `./gradlew javadoc` produces `build/docs/javadoc/index.html`
- [ ] Every type and method from §3 exists (reviewer ticks them off)
- [ ] `docs/uml/class-diagram-initial.puml` updated if names changed

---
**CRITICAL: Exact signatures from master context §3. Stubs only for services. Compiles. Javadoc everywhere. Review by all three before merge.**
