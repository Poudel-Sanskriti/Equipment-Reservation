package test;

import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.Condition;
import equipmentReservation.model.ConflictException;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.InvalidOperationException;
import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import equipmentReservation.model.admin.EquipmentAdminStore;
import equipmentReservation.model.admin.EquipmentService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EquipmentServiceTest {
    private final User admin = user("admin", Role.ADMIN);
    private final User borrower = user("borrower", Role.BORROWER);
    private AtomicReference<User> currentUser;
    private MemoryStore store;
    private EquipmentService equipmentService;

    private static User user(String name, Role role) {
        try {
            return new User(name, "hash", "salt", role);
        } catch (ValidationException e) {
            throw new AssertionError(e);
        }
    }

    private static Equipment equipment(String id, String name, Condition condition,
                                       OperationalStatus status) {
        try {
            return new Equipment(id, name, "Camera", "", condition, status);
        } catch (ValidationException e) {
            throw new AssertionError(e);
        }
    }

    @BeforeEach
    void setUp() {
        currentUser = new AtomicReference<>(admin);
        store = new MemoryStore();
        Clock clock = Clock.fixed(Instant.parse("2026-11-23T09:00:00Z"), ZoneOffset.UTC);
        equipmentService = new EquipmentService(store, currentUser::get, clock);
    }

    /** FR-20: A borrower cannot add or edit equipment. */
    @Test
    void borrowerCannotEditEquipment() {
        currentUser.set(borrower);
        Equipment item = equipment("EQ-1", "Camera", Condition.GOOD, OperationalStatus.OPERATIONAL);
        assertThrows(AuthorizationException.class, () -> equipmentService.addEquipment(item));
        assertThrows(AuthorizationException.class, () -> equipmentService.updateEquipment(item));
    }

    /** FR-21: An equipment ID cannot be used twice. */
    @Test
    void duplicateIdIsRejected() throws Exception {
        equipmentService.addEquipment(equipment("EQ-104", "Camera", Condition.GOOD,
                OperationalStatus.OPERATIONAL));
        ConflictException error = assertThrows(ConflictException.class,
                () -> equipmentService.addEquipment(equipment("EQ-104", "Tripod", Condition.GOOD,
                        OperationalStatus.OPERATIONAL)));
        assertEquals("Equipment ID EQ-104 already exists.", error.getMessage());
    }

    /** FR-21: Name and category are required. */
    @Test
    void blankFieldsAreRejected() {
        assertThrows(ValidationException.class, () -> new Equipment("x", " ", "Camera", "",
                Condition.GOOD, OperationalStatus.OPERATIONAL));
        assertThrows(ValidationException.class, () -> new Equipment("x", "Camera", " ", "",
                Condition.GOOD, OperationalStatus.OPERATIONAL));
    }

    /** FR-22: Updating an item does not erase its reservations. */
    @Test
    void updateKeepsReservationHistory() throws Exception {
        equipmentService.addEquipment(equipment("EQ-1", "Old", Condition.GOOD,
                OperationalStatus.OPERATIONAL));
        store.reservations.put("EQ-1", List.of("RES-1"));
        equipmentService.updateEquipment(equipment("EQ-1", "New", Condition.FAIR,
                OperationalStatus.OPERATIONAL));
        assertEquals("New", equipmentService.getEquipment("EQ-1").getName());
        assertEquals(List.of("RES-1"), store.reservations.get("EQ-1"));
    }

    /** FR-23: Return to service records the administrator and time. */
    @Test
    void returnToServiceRecordsAdmin() throws Exception {
        equipmentService.addEquipment(equipment("EQ-1", "Camera", Condition.DAMAGED,
                OperationalStatus.NON_OPERATIONAL));
        Equipment updated = equipmentService.returnToService("EQ-1", Condition.FAIR);
        assertEquals(OperationalStatus.OPERATIONAL, updated.getOperationalStatus());
        assertEquals(Condition.FAIR, updated.getCondition());
        assertEquals("admin", store.logAdmin);
        assertEquals(Instant.parse("2026-11-23T09:00:00Z"), store.logTime);
    }

    /** FR-23: Operational items, damaged conditions and open maintenance are rejected. */
    @Test
    void returnToServiceChecksStatus() throws Exception {
        equipmentService.addEquipment(equipment("EQ-1", "Camera", Condition.GOOD,
                OperationalStatus.OPERATIONAL));
        assertThrows(InvalidOperationException.class,
                () -> equipmentService.returnToService("EQ-1", Condition.GOOD));
        equipmentService.updateEquipment(equipment("EQ-1", "Camera", Condition.DAMAGED,
                OperationalStatus.NON_OPERATIONAL));
        assertThrows(ValidationException.class,
                () -> equipmentService.returnToService("EQ-1", Condition.DAMAGED));
        store.maintenanceOpen = true;
        assertThrows(InvalidOperationException.class,
                () -> equipmentService.returnToService("EQ-1", Condition.GOOD));
        assertEquals(OperationalStatus.NON_OPERATIONAL,
                equipmentService.getEquipment("EQ-1").getOperationalStatus());
    }

    private static class MemoryStore implements EquipmentAdminStore {
        final Map<String, Equipment> items = new LinkedHashMap<>();
        final Map<String, List<String>> reservations = new HashMap<>();
        boolean maintenanceOpen;
        String logAdmin;
        Instant logTime;

        @Override
        public Equipment find(String id) {
            return items.get(id);
        }

        @Override
        public List<Equipment> findAll() {
            return new ArrayList<>(items.values());
        }

        @Override
        public void insert(Equipment item) throws ConflictException {
            if (items.containsKey(item.getId())) {
                throw new ConflictException("Equipment ID " + item.getId() + " already exists.");
            }
            items.put(item.getId(), item);
        }

        @Override
        public void update(Equipment item) throws NotFoundException {
            if (!items.containsKey(item.getId())) {
                throw new NotFoundException("Equipment " + item.getId() + " was not found.");
            }
            items.put(item.getId(), item);
        }

        @Override
        public Equipment returnToService(String id, Condition condition,
                                          String adminUsername, Instant time) throws Exception {
            Equipment item = items.get(id);
            if (item == null) {
                throw new NotFoundException("Equipment " + id + " was not found.");
            }
            if (item.getOperationalStatus() != OperationalStatus.NON_OPERATIONAL) {
                throw new InvalidOperationException("Item must be non-operational.");
            }
            if (maintenanceOpen) {
                throw new InvalidOperationException("Close the open maintenance record first.");
            }
            Equipment updated = new Equipment(id, item.getName(), item.getCategory(),
                    item.getDescription(), condition, OperationalStatus.OPERATIONAL);
            items.put(id, updated);
            logAdmin = adminUsername;
            logTime = time;
            return updated;
        }
    }
}
