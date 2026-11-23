package equipmentReservation.model;

/**
 * Small input checks shared by the model classes. Each check throws a
 * {@link ValidationException} whose message names the field (NFR-04).
 */
final class Checks {

    /** Not instantiable. */
    private Checks() {
    }

    /**
     * Requires a non-null, non-blank string.
     *
     * @param value the value to check
     * @param field the field name used in the error message
     * @return {@code value}, unchanged
     * @throws ValidationException if {@code value} is null or blank
     */
    static String requireText(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required.");
        }
        return value;
    }

    /**
     * Requires a non-null value.
     *
     * @param <T>   the value's type
     * @param value the value to check
     * @param field the field name used in the error message
     * @return {@code value}, unchanged
     * @throws ValidationException if {@code value} is null
     */
    static <T> T requireValue(T value, String field) throws ValidationException {
        if (value == null) {
            throw new ValidationException(field + " is required.");
        }
        return value;
    }
}
