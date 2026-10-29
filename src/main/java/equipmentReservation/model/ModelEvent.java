package equipmentReservation.model;

import java.awt.event.ActionEvent;

/**
 * Event sent from the model to its views when model state changes.
 * The action command names the kind of data that changed; views pull the
 * new state from the model through its getters (push-MVC, NFR-05).
 */
public class ModelEvent extends ActionEvent {

    private static final long serialVersionUID = 1L;

    /** Equipment records changed. */
    public static final String EQUIPMENT = "EQUIPMENT";
    /** Reservations were created, cancelled, expired or completed. */
    public static final String RESERVATIONS = "RESERVATIONS";
    /** Checkouts or returns were recorded. */
    public static final String CHECKOUTS = "CHECKOUTS";
    /** Maintenance records were opened or closed. */
    public static final String MAINTENANCE = "MAINTENANCE";
    /** Settings such as the pickup window changed. */
    public static final String SETTINGS = "SETTINGS";
    /** A user signed in or out. */
    public static final String SESSION = "SESSION";

    /**
     * Creates a model change event.
     *
     * @param source the model that changed
     * @param what   what changed; one of the constants of this class
     */
    public ModelEvent(Object source, String what) {
        super(source, ActionEvent.ACTION_PERFORMED, what);
    }
}
