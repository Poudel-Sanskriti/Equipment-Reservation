package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import equipmentReservation.model.persistence.Database;
import equipmentReservation.model.persistence.EquipmentRepository;
import equipmentReservation.model.persistence.ReservationRepository;
import equipmentReservation.model.persistence.UserRepository;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link UserRepository}, {@link EquipmentRepository} and
 * {@link ReservationRepository}: data is saved and loaded unchanged (FR-35,
 * FR-36), only through valid references, and all-or-nothing inside a
 * transaction (FR-37).
 */
class RepositoriesTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 11, 30, 9, 0);

    @TempDir
    Path dir;

    private Database db;
    private UserRepository users;
    private EquipmentRepository equipment;
    private ReservationRepository reservations;
    private User jsmith;
    private User mlee;
    private Equipment camera;
    private Equipment laptop;

    @BeforeEach
    void setUp() throws Exception {
        db = new Database(dir.resolve("test.db").toString());
        db.createSchema();
        users = new UserRepository(db);
        equipment = new EquipmentRepository(db);
        reservations = new ReservationRepository(db);
        jsmith = new User("jsmith", "hash1", "salt1", Role.BORROWER);
        mlee = new User("mlee", "hash2", "salt2", Role.BORROWER);
        camera = new Equipment("EQ-104", "Canon EOS", "Camera", "DSLR kit",
                Condition.GOOD, OperationalStatus.OPERATIONAL);
        laptop = new Equipment("EQ-001", "Dell Latitude", "Laptop", "",
                Condition.FAIR, OperationalStatus.OPERATIONAL);
        users.save(jsmith);
        users.save(mlee);
        equipment.save(camera);
        equipment.save(laptop);
    }

    private static LocalDateTime at(int day, int hour) {
        return LocalDateTime.of(2026, 12, day, hour, 0);
    }

    private Reservation reservation(String id, User u, Equipment e, int day, int startHour, int endHour)
            throws ValidationException {
        return new Reservation(id, u, e, at(day, startHour), at(day, endHour), CREATED);
    }

    @Test
    void userRoundTripAndUnknownUser() throws Exception {
        User loaded = users.findByUsername("jsmith");
        assertEquals("jsmith", loaded.getUsername());
        assertEquals("hash1", loaded.getPasswordHash());
        assertEquals("salt1", loaded.getSalt());
        assertEquals(Role.BORROWER, loaded.getRole());
        assertThrows(NotFoundException.class, () -> users.findByUsername("nobody"));
    }

    @Test
    void savingEquipmentAgainUpdatesIt() throws Exception {
        equipment.save(new Equipment("EQ-104", "Canon EOS R6", "Camera", "Mirrorless",
                Condition.FAIR, OperationalStatus.NON_OPERATIONAL));
        Equipment loaded = equipment.findById("EQ-104");
        assertEquals("Canon EOS R6", loaded.getName());
        assertEquals(Condition.FAIR, loaded.getCondition());
        assertEquals(OperationalStatus.NON_OPERATIONAL, loaded.getOperationalStatus());
        assertEquals(2, equipment.findAll().size());
    }

    @Test
    void findAllIsOrderedById() throws Exception {
        List<Equipment> all = equipment.findAll();
        assertEquals(List.of("EQ-001", "EQ-104"), all.stream().map(Equipment::getId).toList());
        assertThrows(NotFoundException.class, () -> equipment.findById("EQ-999"));
    }

    @Test
    void reservationRoundTripKeepsEveryField() throws Exception {
        reservations.save(reservation("R-1", jsmith, camera, 1, 10, 12));
        Reservation loaded = reservations.findById("R-1");
        assertEquals("R-1", loaded.getId());
        assertEquals("jsmith", loaded.getBorrower().getUsername());
        assertEquals("EQ-104", loaded.getItem().getId());
        assertEquals(at(1, 10), loaded.getStart());
        assertEquals(at(1, 12), loaded.getEnd());
        assertEquals(CREATED, loaded.getCreatedAt());
        assertEquals(ReservationStatus.ACTIVE, loaded.getStatus());
        assertThrows(NotFoundException.class, () -> reservations.findById("R-404"));
    }

    @Test
    void savingAgainUpdatesOnlyTheStatus() throws Exception {
        Reservation r = reservation("R-1", jsmith, camera, 1, 10, 12);
        reservations.save(r);
        reservations.save(Reservation.restore("R-1", jsmith, camera, at(2, 8), at(2, 9), CREATED,
                ReservationStatus.CANCELLED));
        Reservation loaded = reservations.findById("R-1");
        assertEquals(ReservationStatus.CANCELLED, loaded.getStatus());
        assertEquals(at(1, 10), loaded.getStart());
        assertEquals(at(1, 12), loaded.getEnd());
    }

    @Test
    void findActiveByEquipmentReturnsOnlyActiveForThatItem() throws Exception {
        reservations.save(reservation("R-1", jsmith, camera, 1, 10, 12));
        reservations.save(Reservation.restore("R-2", mlee, camera, at(2, 10), at(2, 12), CREATED,
                ReservationStatus.CANCELLED));
        reservations.save(reservation("R-3", mlee, laptop, 1, 10, 12));
        reservations.save(reservation("R-4", mlee, camera, 3, 10, 12));
        List<Reservation> active = reservations.findActiveByEquipment("EQ-104");
        assertEquals(List.of("R-1", "R-4"), active.stream().map(Reservation::getId).toList());
    }

    @Test
    void findByBorrowerReturnsOnlyTheirsOrderedByStart() throws Exception {
        reservations.save(reservation("R-1", jsmith, camera, 3, 10, 12));
        reservations.save(reservation("R-2", mlee, laptop, 1, 10, 12));
        reservations.save(reservation("R-3", jsmith, laptop, 1, 8, 9));
        List<Reservation> mine = reservations.findByBorrower("jsmith");
        assertEquals(List.of("R-3", "R-1"), mine.stream().map(Reservation::getId).toList());
        assertTrue(reservations.findByBorrower("nobody").isEmpty());
    }

    @Test
    void dataIsFoundAfterReopeningTheDatabase() throws Exception {
        reservations.save(reservation("R-1", jsmith, camera, 1, 10, 12));
        Database reopened = new Database(dir.resolve("test.db").toString());
        reopened.createSchema();
        assertEquals("Canon EOS", new EquipmentRepository(reopened).findById("EQ-104").getName());
        assertEquals("jsmith", new UserRepository(reopened).findByUsername("jsmith").getUsername());
        assertEquals("EQ-104", new ReservationRepository(reopened).findById("R-1").getItem().getId());
    }

    @Test
    void reservationForUnsavedUserIsRejected() throws Exception {
        User ghost = new User("ghost", "h", "s", Role.BORROWER);
        assertThrows(SQLException.class, () -> reservations.save(reservation("R-1", ghost, camera, 1, 10, 12)));
    }

    @Test
    void savesInsideAFailedTransactionAreUndone() throws Exception {
        ValidationException stop = new ValidationException("stop");
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> db.<ValidationException>inTransaction(conn -> {
                    equipment.save(conn, new Equipment("EQ-777", "Projector", "Projector", "",
                            Condition.GOOD, OperationalStatus.OPERATIONAL));
                    reservations.save(conn, reservation("R-9", jsmith, camera, 5, 10, 12));
                    throw stop;
                }));
        assertSame(stop, thrown);
        assertThrows(NotFoundException.class, () -> equipment.findById("EQ-777"));
        assertThrows(NotFoundException.class, () -> reservations.findById("R-9"));
    }
}
