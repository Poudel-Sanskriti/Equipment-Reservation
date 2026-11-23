package equipmentReservation.model;

/**
 * Physical condition of an equipment item (FR-03, FR-21).
 */
public enum Condition {
    /** No visible wear or damage. */
    GOOD,
    /** Usable with minor wear. */
    FAIR,
    /** Damaged; an item returned in this condition becomes non-operational (FR-29). */
    DAMAGED
}
