package equipmentReservation.model;

/**
 * Thrown when input is invalid, for example a blank required field or an
 * end time that is not after the start time (FR-08, FR-14, FR-21).
 */
public class ValidationException extends Exception {

    /** Serialization version (required because Exception is serializable). */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a message that can be shown to the user (NFR-04).
     *
     * @param message explanation of what went wrong
     */
    public ValidationException(String message) {
        super(message);
    }
}
