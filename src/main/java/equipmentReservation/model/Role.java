package equipmentReservation.model;

/**
 * The role stored on a user account (FR-01). The role decides which
 * operations the signed-in user may perform (NFR-08).
 */
public enum Role {
    /** Can search the catalog, reserve, cancel and view their own history. */
    BORROWER,
    /** Can manage equipment, process checkouts and returns, and handle maintenance. */
    ADMIN
}
