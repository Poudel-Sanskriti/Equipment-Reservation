package equipmentReservation.model;

/**
 * Receives change notifications from a {@link Model} (NFR-05).
 * Views implement this interface so they can refresh when the model changes.
 */
public interface ModelListener {

    /**
     * Called by the model after its state has changed.
     *
     * @param e the event describing the change
     */
    void modelChanged(ModelEvent e);
}
