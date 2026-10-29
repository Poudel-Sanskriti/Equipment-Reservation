package equipmentReservation.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract root class of the model hierarchy; provides the listener
 * registration and notification behaviour of the John Hunt pattern (NFR-05).
 * Listeners are kept in a {@link CopyOnWriteArrayList} so that notification is
 * safe while listeners are added or removed, including from background threads.
 */
public abstract class AbstractModel implements Model {

    /** Listeners interested in changes to this model. */
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Notifies every registered listener of a change. A listener may add or
     * remove listeners while being notified.
     *
     * @param event describes what changed
     */
    @Override
    public void notifyChanged(ModelEvent event) {
        for (ModelListener listener : listeners) {
            listener.modelChanged(event);
        }
    }

    /**
     * Registers a listener for model change events.
     *
     * @param listener the listener to add
     */
    public void addModelListener(ModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters a listener; it receives no further events.
     *
     * @param listener the listener to remove
     */
    public void removeModelListener(ModelListener listener) {
        listeners.remove(listener);
    }
}
