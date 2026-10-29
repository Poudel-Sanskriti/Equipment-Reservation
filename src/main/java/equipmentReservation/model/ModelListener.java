package equipmentReservation.model;

/**
 * Receives notifications of model changes. Views implement this interface
 * and register with the model (NFR-05).
 */
public interface ModelListener {

    /**
     * Called by the model after its state has changed.
     *
     * @param event describes what changed
     */
    void modelChanged(ModelEvent event);
}
