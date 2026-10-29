# INCREMENT 01: MVC Project Structure
**Status:** Standard · **Owner:** Sanskriti · **Dependencies:** none · **State:** ✅ DONE during repo setup (2026-10-29)

**CRITICAL: Create the directory and package layout of master context §2 exactly. Create NO Java classes in this increment.**

---

### Capability
Directory layout for the `equipmentReservation` system, with `.gitkeep` files so the empty packages are committed:
```
src/main/java/equipmentReservation/{model, model/persistence, view, controller, util}
src/test/java/test
ctxt/  prompts/  docs/{uml, acceptance, screenshots}
```

### Must NOT change
Nothing exists yet.

### Verification
- [x] All directories exist and are tracked by git.
- [x] Package names are exactly `equipmentReservation.model`, `.model.persistence`, `.view`, `.controller`, `.util`, plus `test`.

---
**CRITICAL: Package names must match master context §2. Every later prompt depends on them.**
