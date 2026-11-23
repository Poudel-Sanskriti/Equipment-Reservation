package equipmentReservation.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for every model (NFR-05). Keeps the list of registered
 * {@link ModelListener}s and notifies them of changes.
 *
 * <p>The listener list is a {@link CopyOnWriteArrayList}, so notifying is
 * safe when models are used from several threads (FR-12) and when a listener
 * removes itself while being notified.</p>
 */
public abstract class AbstractModel implements Model {

    /** Registered listeners, in registration order. */
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a model with no listeners.
     */
    protected AbstractModel() {
    }

    /**
     * Registers a listener. Adding a listener that is already registered has
     * no effect.
     *
     * @param l the listener to add
     * @throws NullPointerException if {@code l} is {@code null}
     */
    public void addModelListener(ModelListener l) {
        Objects.requireNonNull(l, "listener");
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Unregisters a listener. Removing a listener that is not registered has
     * no effect.
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    /**
     * Notifies every registered listener, in registration order.
     *
     * @param e the event to pass to each listener
     */
    @Override
    public void notifyChanged(ModelEvent e) {
        for (ModelListener l : listeners) {
            l.modelChanged(e);
        }
    }
}
