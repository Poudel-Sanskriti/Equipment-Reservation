package equipmentReservation.model;

import java.time.LocalDateTime;

/**
 * A borrower's reservation of one equipment item for a time period (FR-13).
 * New reservations are {@link ReservationStatus#ACTIVE}.
 */
public class Reservation extends AbstractModel {

    /** Unique id (FR-13). */
    private final String id;

    /** The borrower who made the reservation (association R1). */
    private final User borrower;

    /** The reserved item (association R2). */
    private final Equipment item;

    /** Start of the reserved period. */
    private final LocalDateTime start;

    /** End of the reserved period; always after {@link #start} (FR-08). */
    private final LocalDateTime end;

    /** When the reservation was created (FR-13). */
    private final LocalDateTime createdAt;

    /** Current lifecycle status. */
    private ReservationStatus status;

    /**
     * Creates an active reservation.
     *
     * @param id        unique id; not blank
     * @param borrower  the borrower; not null
     * @param item      the reserved item; not null
     * @param start     start of the period; not null
     * @param end       end of the period; must be after {@code start}
     * @param createdAt creation time; not null
     * @throws ValidationException if an argument is missing or {@code end} is not after {@code start} (FR-08)
     */
    public Reservation(String id, User borrower, Equipment item, LocalDateTime start,
                       LocalDateTime end, LocalDateTime createdAt) throws ValidationException {
        this.id = Checks.requireText(id, "Reservation ID");
        this.borrower = Checks.requireValue(borrower, "Borrower");
        this.item = Checks.requireValue(item, "Item");
        this.start = Checks.requireValue(start, "Start");
        this.end = Checks.requireValue(end, "End");
        this.createdAt = Checks.requireValue(createdAt, "Creation time");
        if (!end.isAfter(start)) {
            throw new ValidationException("End must be later than start.");
        }
        this.status = ReservationStatus.ACTIVE;
    }

    /**
     * Returns the unique id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the borrower who made the reservation.
     *
     * @return the borrower
     */
    public User getBorrower() {
        return borrower;
    }

    /**
     * Returns the reserved item.
     *
     * @return the item
     */
    public Equipment getItem() {
        return item;
    }

    /**
     * Returns the start of the reserved period.
     *
     * @return the start time
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Returns the end of the reserved period.
     *
     * @return the end time
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Returns when the reservation was created.
     *
     * @return the creation time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the current status.
     *
     * @return the status
     */
    public ReservationStatus getStatus() {
        return status;
    }

    /**
     * Tells whether the reservation is still active.
     *
     * @return {@code true} if the status is {@link ReservationStatus#ACTIVE}
     */
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    /**
     * Tells whether this reservation's period overlaps another period (FR-11).
     * Two periods overlap when each starts before the other ends; a period
     * that ends exactly when the other starts does NOT overlap, so
     * back-to-back reservations are allowed.
     *
     * @param otherStart start of the other period
     * @param otherEnd   end of the other period
     * @return {@code true} if the periods overlap
     * @throws UnsupportedOperationException always, until task 2.11 implements it
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        throw new UnsupportedOperationException("FR-11: implemented in task 2.11");
    }
}
