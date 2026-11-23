package equipmentReservation.model;

import java.awt.event.ActionEvent;

/**
 * Event sent from a {@link Model} to its {@link ModelListener}s when the
 * model changes (NFR-05). It carries only the source, an id and a command
 * string; listeners read the new state from the model itself.
 */
public class ModelEvent extends ActionEvent {

    /** Serialization version (required because the superclass is serializable). */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a model event.
     *
     * @param source  the model that changed
     * @param id      an event id, for example {@link ActionEvent#ACTION_PERFORMED}
     * @param command a short description of the change, for example "reservationAdded"
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    public ModelEvent(Object source, int id, String command) {
        super(source, id, command);
    }
}
