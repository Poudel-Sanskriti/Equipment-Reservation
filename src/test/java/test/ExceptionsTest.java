package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.model.AuthorizationException;
import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.ReservationConflictException;
import equipmentReservation.model.ValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Checks that every domain exception keeps its message (NFR-04) and is a
 * checked exception, so callers must handle it.
 */
class ExceptionsTest {

    private static List<Exception> all() {
        return List.of(
            new ValidationException("v"),
            new ReservationConflictException("c"),
            new AuthorizationException("a"),
            new NotFoundException("n"));
    }

    @Test
    void eachExceptionKeepsItsMessage() {
        List<Exception> list = all();
        assertEquals("v", list.get(0).getMessage());
        assertEquals("c", list.get(1).getMessage());
        assertEquals("a", list.get(2).getMessage());
        assertEquals("n", list.get(3).getMessage());
    }

    @Test
    void eachExceptionIsChecked() {
        for (Exception e : all()) {
            assertFalse(e instanceof RuntimeException, e.getClass().getSimpleName());
            assertTrue(e instanceof Exception);
        }
    }
}
