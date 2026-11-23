package equipmentReservation.util;

import equipmentReservation.model.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

/**
 * Parses and formats date-times in the one format used everywhere in the
 * application, {@value #PATTERN}, in the computer's local time zone (FR-39).
 */
public final class DateTimes {

    /** The date-time format shown to users and used in error messages (FR-39). */
    public static final String PATTERN = "YYYY-MM-DD HH:mm";

    /** Strict formatter: rejects impossible dates such as 2026-02-30. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    /** Not instantiable; all methods are static. */
    private DateTimes() {
    }

    /**
     * Parses text in the {@value #PATTERN} format. Surrounding spaces are ignored.
     *
     * @param text the text to parse, for example "2026-12-01 10:00"
     * @return the parsed date-time
     * @throws ValidationException if the text is missing, in another format, or
     *                             not a real date and time; the message shows the
     *                             expected format (NFR-04)
     */
    public static LocalDateTime parse(String text) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Enter a date and time as " + PATTERN + ".");
        }
        try {
            return LocalDateTime.parse(text.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "\"" + text.trim() + "\" is not a valid date and time. Use " + PATTERN + ".");
        }
    }

    /**
     * Formats a date-time as {@value #PATTERN}, for example "2026-01-05 09:07".
     *
     * @param t the date-time to format
     * @return the formatted text
     * @throws NullPointerException if {@code t} is {@code null}
     */
    public static String format(LocalDateTime t) {
        return FORMATTER.format(Objects.requireNonNull(t, "date-time"));
    }
}
