package equipmentReservation.model;

/**
 * A model in the push-MVC architecture (John Hunt Calculator pattern, NFR-05).
 * A model holds application state and pushes change notifications to its
 * registered listeners. Models never depend on views or controllers.
 */
public interface Model {

    /**
     * Notifies every registered listener that the model has changed.
     *
     * @param e the event describing the change; passed unchanged to each listener
     */
    void notifyChanged(ModelEvent e);
}
