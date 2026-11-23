package test;

import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import equipmentReservation.model.admin.SettingsService;
import equipmentReservation.model.admin.SettingsStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettingsServiceTest {
    private AtomicReference<User> currentUser;
    private SettingsStore store;
    private SettingsService settings;

    @BeforeEach
    void setUp() throws ValidationException {
        currentUser = new AtomicReference<>(new User("admin", "hash", "salt", Role.ADMIN));
        store = new MemorySettingsStore();
        settings = new SettingsService(store, currentUser::get);
    }

    /** FR-15: The initial pickup window is 30 minutes. */
    @Test
    void defaultWindowIsThirtyMinutes() throws Exception {
        assertEquals(30, settings.getPickupWindowMinutes());
    }

    /** FR-15: Saved settings can be read through a new service instance. */
    @Test
    void savedWindowCanBeReadAgain() throws Exception {
        settings.setPickupWindowMinutes(45);
        SettingsService reopened = new SettingsService(store, currentUser::get);
        assertEquals(45, reopened.getPickupWindowMinutes());
    }

    /** FR-15: The valid range includes both endpoints. */
    @Test
    void acceptsBoundaryValues() throws Exception {
        settings.setPickupWindowMinutes(1);
        assertEquals(1, settings.getPickupWindowMinutes());
        settings.setPickupWindowMinutes(1440);
        assertEquals(1440, settings.getPickupWindowMinutes());
    }

    /** FR-15: Invalid values do not change the saved setting. */
    @Test
    void rejectsInvalidValues() throws Exception {
        for (int minutes : new int[] {0, -5, 1441}) {
            assertThrows(ValidationException.class, () -> settings.setPickupWindowMinutes(minutes));
        }
        assertEquals(30, settings.getPickupWindowMinutes());
    }

    /** FR-15: Borrowers cannot change administrator settings. */
    @Test
    void borrowerCannotChangeWindow() throws Exception {
        currentUser.set(new User("borrower", "hash", "salt", Role.BORROWER));
        assertThrows(AuthorizationException.class, () -> settings.setPickupWindowMinutes(45));
    }

    private static class MemorySettingsStore implements SettingsStore {
        private Integer minutes;

        @Override
        public Integer getPickupWindowMinutes() {
            return minutes;
        }

        @Override
        public void setPickupWindowMinutes(int minutes) {
            this.minutes = minutes;
        }
    }
}
