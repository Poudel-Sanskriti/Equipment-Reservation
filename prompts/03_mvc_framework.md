# INCREMENT 03: MVC Framework Classes (John Hunt pattern)
**Status:** Standard · **Owner:** Sanskriti · **Dependencies:** 02 · **Requirements:** NFR-05, NFR-06

**CRITICAL: Re-implement the professor's MVC base classes from `ctxt/reference/calcProj/src/calc/` in our packages, with the adaptations below. Signatures MUST match master context §3.1.**

---

### Selective context
- The original classes are `Model`, `ModelEvent`, `ModelListener`, `AbstractModel` (calc.model), `Controller`, `AbstractController` (calc.controller), and `View`, `JFrameView` (calc.view). Read them first.
- Our app has **one** Model, `ReservationSystem` (built in a later increment). Background threads (the expiration scheduler) will fire events.

### Capability
| Class | Package | Adaptation vs. calcProj |
|---|---|---|
| `Model` | model | same |
| `ModelEvent extends ActionEvent` | model | constructor `(Object source, String what)`; string constants EQUIPMENT, RESERVATIONS, CHECKOUTS, MAINTENANCE, SETTINGS, SESSION; drop `amount` |
| `ModelListener` | model | same |
| `AbstractModel` | model | `CopyOnWriteArrayList<ModelListener>` (thread-safe iteration) |
| `Controller` | controller | same |
| `AbstractController` | controller | add `public abstract void operation(String command)` |
| `View` | view | same |
| `JFrameView` | view | `modelChanged` is `final`: if `SwingUtilities.isEventDispatchThread()` call `refresh(e)`, otherwise `invokeLater(() -> refresh(e))`; abstract `refresh(ModelEvent)`; `showError`, `showInfo`, `showFieldError` helpers using `JOptionPane`; `dispose()` unregisters from the model |

### Implementation instructions
1. Full Javadoc on every type and member (class purpose, `@param`, `@return`).
2. `JFrameView` must call `registerWithModel()` from `setModel`, as in the original.
3. **No other classes in this increment.**

### Tests to write FIRST (`src/test/java/test/MvcFrameworkTest.java`)
- A listener added to a concrete test subclass of `AbstractModel` receives the event; after `removeModelListener` it doesn't.
- A listener that removes itself during notification causes no `ConcurrentModificationException`.
- `ModelEvent.getActionCommand()` returns the `what` string.
- (Optional; needs a display, so skip it when `GraphicsEnvironment.isHeadless()`): JFrameView refresh runs on the EDT when fired from a background thread.

### Verification
- [ ] `./gradlew build` passes and the tests are green
- [ ] `model` imports nothing from `javax.swing` (`grep -r "javax.swing" src/main/java/equipmentReservation/model` → empty)

---
**CRITICAL: These classes are the backbone of every view. Keep the John Hunt structure, use CopyOnWriteArrayList, and make JFrameView EDT-safe.**
