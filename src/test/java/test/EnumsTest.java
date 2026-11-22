package test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import equipmentReservation.model.Condition;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.Role;
import org.junit.jupiter.api.Test;

/**
 * Checks that each model enum has exactly the values defined in the SRS
 * glossary (§1.4) and FR-03, in order.
 */
class EnumsTest {

    @Test
    void roleValues() {
        assertArrayEquals(new Role[] {Role.BORROWER, Role.ADMIN}, Role.values());
    }

    @Test
    void reservationStatusValues() {
        assertArrayEquals(new ReservationStatus[] {
            ReservationStatus.ACTIVE, ReservationStatus.CHECKED_OUT, ReservationStatus.COMPLETED,
            ReservationStatus.CANCELLED, ReservationStatus.EXPIRED
        }, ReservationStatus.values());
    }

    @Test
    void conditionValues() {
        assertArrayEquals(new Condition[] {Condition.GOOD, Condition.FAIR, Condition.DAMAGED},
                Condition.values());
    }

    @Test
    void operationalStatusValues() {
        assertArrayEquals(new OperationalStatus[] {
            OperationalStatus.OPERATIONAL, OperationalStatus.NON_OPERATIONAL
        }, OperationalStatus.values());
    }
}
