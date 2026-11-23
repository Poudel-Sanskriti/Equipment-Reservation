package equipmentReservation.model;

/**
 * Thrown when credentials are invalid or the signed-in user's role is not
 * allowed to perform an operation (FR-01, NFR-08).
 */
public class AuthorizationException extends Exception {

    /** Serialization version (required because Exception is serializable). */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a message that can be shown to the user (NFR-04).
     *
     * @param message explanation of what went wrong
     */
    public AuthorizationException(String message) {
        super(message);
    }
}
