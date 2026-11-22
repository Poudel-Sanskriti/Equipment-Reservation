package equipmentReservation.model;

/**
 * Thrown when a user, equipment item or reservation does not exist.
 */
public class NotFoundException extends Exception {

    /** Serialization version (required because Exception is serializable). */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a message that can be shown to the user (NFR-04).
     *
     * @param message explanation of what went wrong
     */
    public NotFoundException(String message) {
        super(message);
    }
}
