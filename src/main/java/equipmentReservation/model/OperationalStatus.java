package equipmentReservation.model;

/**
 * Whether an equipment item can be used (FR-03, FR-21). Non-operational
 * items cannot be reserved or checked out (FR-09, FR-26).
 */
public enum OperationalStatus {
    /** The item works and can be reserved. */
    OPERATIONAL,
    /** The item cannot be used until an administrator returns it to service (FR-23). */
    NON_OPERATIONAL
}
