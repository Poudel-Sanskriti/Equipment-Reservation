package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.model.AbstractModel;
import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Tests for the model skeleton: {@link User}, {@link Equipment} and
 * {@link Reservation} keep their data, validate their input (FR-08, FR-21),
 * and start in the right state (FR-13).
 */
class DomainSkeletonTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 12, 1, 12, 0);
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 11, 30, 9, 0);

    private static User borrower() throws ValidationException {
        return new User("jsmith", "hash", "salt", Role.BORROWER);
    }

    private static Equipment camera() throws ValidationException {
        return new Equipment("EQ-104", "Canon EOS", "Camera", "DSLR kit",
                Condition.GOOD, OperationalStatus.OPERATIONAL);
    }

    @Test
    void userKeepsItsFields() throws ValidationException {
        User u = new User("admin1", "h", "s", Role.ADMIN);
        assertEquals("admin1", u.getUsername());
        assertEquals("h", u.getPasswordHash());
        assertEquals("s", u.getSalt());
        assertEquals(Role.ADMIN, u.getRole());
        assertTrue(u.isAdmin());
        assertFalse(borrower().isAdmin());
    }

    @Test
    void userRejectsBlankUsernameAndNullRole() {
        assertThrows(ValidationException.class, () -> new User("  ", "h", "s", Role.BORROWER));
        assertThrows(ValidationException.class, () -> new User("jsmith", "h", "s", null));
    }

    @Test
    void equipmentKeepsItsFields() throws ValidationException {
        Equipment e = camera();
        assertEquals("EQ-104", e.getId());
        assertEquals("Canon EOS", e.getName());
        assertEquals("Camera", e.getCategory());
        assertEquals("DSLR kit", e.getDescription());
        assertEquals(Condition.GOOD, e.getCondition());
        assertEquals(OperationalStatus.OPERATIONAL, e.getOperationalStatus());
        assertTrue(e.isOperational());
    }

    @Test
    void equipmentRejectsBlankIdAndAllowsEmptyDescription() throws ValidationException {
        assertThrows(ValidationException.class, () -> new Equipment("", "Laptop", "Laptop", "",
                Condition.GOOD, OperationalStatus.OPERATIONAL));
        Equipment e = new Equipment("EQ-1", "Laptop", "Laptop", "",
                Condition.FAIR, OperationalStatus.NON_OPERATIONAL);
        assertEquals("", e.getDescription());
        assertFalse(e.isOperational());
    }

    @Test
    void reservationKeepsItsFieldsAndStartsActive() throws ValidationException {
        User u = borrower();
        Equipment e = camera();
        Reservation r = new Reservation("R-2041", u, e, START, END, CREATED);
        assertEquals("R-2041", r.getId());
        assertSame(u, r.getBorrower());
        assertSame(e, r.getItem());
        assertEquals(START, r.getStart());
        assertEquals(END, r.getEnd());
        assertEquals(CREATED, r.getCreatedAt());
        assertEquals(ReservationStatus.ACTIVE, r.getStatus());
        assertTrue(r.isActive());
    }

    @Test
    void reservationRejectsEndNotAfterStart() throws ValidationException {
        User u = borrower();
        Equipment e = camera();
        assertThrows(ValidationException.class, () -> new Reservation("R-1", u, e, START, START, CREATED));
        assertThrows(ValidationException.class, () -> new Reservation("R-1", u, e, END, START, CREATED));
    }

    @Test
    void reservationRejectsMissingBorrower() throws ValidationException {
        Equipment e = camera();
        assertThrows(ValidationException.class, () -> new Reservation("R-1", null, e, START, END, CREATED));
    }

    @Test
    void domainClassesAreModels() throws ValidationException {
        assertTrue(borrower() instanceof AbstractModel);
        assertTrue(camera() instanceof AbstractModel);
        assertTrue(new Reservation("R-1", borrower(), camera(), START, END, CREATED) instanceof AbstractModel);
    }
}
