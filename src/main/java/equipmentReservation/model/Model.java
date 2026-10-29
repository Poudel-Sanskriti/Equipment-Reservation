package equipmentReservation.model;

/**
 * Root of the model hierarchy in the push-MVC architecture (John Hunt pattern).
 * A model notifies its registered listeners whenever its state changes (NFR-05).
 */
public interface Model {

    /**
     * Notifies every registered listener that the model state has changed.
     *
     * @param event describes what changed
     */
    void notifyChanged(ModelEvent event);
}
