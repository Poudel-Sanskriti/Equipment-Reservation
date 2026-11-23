package equipmentReservation.model;

/**
 * Lifecycle status of a reservation (SRS §1.4). A reservation starts as
 * {@link #ACTIVE}; it then becomes {@link #CHECKED_OUT} and finally
 * {@link #COMPLETED}, or ends early as {@link #CANCELLED} or {@link #EXPIRED}.
 */
public enum ReservationStatus {
    /** Accepted and not yet checked out, cancelled or expired (FR-13). */
    ACTIVE,
    /** The item has been handed to the borrower (FR-27). */
    CHECKED_OUT,
    /** The item has been returned (FR-28). */
    COMPLETED,
    /** Cancelled by the borrower before checkout (FR-18). */
    CANCELLED,
    /** Not picked up before the pickup deadline (FR-15). */
    EXPIRED
}
