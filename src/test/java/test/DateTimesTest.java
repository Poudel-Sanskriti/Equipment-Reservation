package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.model.ValidationException;
import equipmentReservation.util.DateTimes;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DateTimes}: strict {@code YYYY-MM-DD HH:mm} parsing and
 * formatting (FR-39) with helpful error messages (NFR-04).
 */
class DateTimesTest {

    @Test
    void parsesValidText() throws ValidationException {
        assertEquals(LocalDateTime.of(2026, 12, 1, 10, 0), DateTimes.parse("2026-12-01 10:00"));
    }

    @Test
    void formatsWithZeroPadding() {
        assertEquals("2026-01-05 09:07", DateTimes.format(LocalDateTime.of(2026, 1, 5, 9, 7)));
    }

    @Test
    void parseAndFormatRoundTrip() throws ValidationException {
        LocalDateTime t = LocalDateTime.of(2026, 11, 30, 23, 59);
        assertEquals(t, DateTimes.parse(DateTimes.format(t)));
    }

    @Test
    void acceptsSurroundingSpaces() throws ValidationException {
        assertEquals(LocalDateTime.of(2026, 12, 1, 10, 0), DateTimes.parse("  2026-12-01 10:00 "));
    }

    @Test
    void rejectsBadInputWithHelpfulMessage() {
        String[] bad = {null, "", "   ", "2026-13-01 10:00", "2026-02-30 10:00",
            "2026-12-01T10:00", "12/01/2026 10:00", "2026-12-01"};
        for (String text : bad) {
            ValidationException e = assertThrows(ValidationException.class, () -> DateTimes.parse(text));
            assertTrue(e.getMessage().contains(DateTimes.PATTERN), "message for: " + text);
        }
    }
}
