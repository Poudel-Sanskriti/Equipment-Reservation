package equipmentReservation.model;

/**
 * Thrown when a reservation would overlap an active reservation for the
 * same item, including when two requests arrive at once (FR-11, FR-12).
 */
public class ReservationConflictException extends Exception {

    /** Serialization version (required because Exception is serializable). */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a message that can be shown to the user (NFR-04).
     *
     * @param message explanation of what went wrong
     */
    public ReservationConflictException(String message) {
        super(message);
    }
}
