package equipmentReservation.model.persistence;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import equipmentReservation.util.DateTimes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Turns database rows back into model objects. Shared by the repositories.
 * A row that cannot become a valid object is reported as an
 * {@link SQLException}, because it means the stored data is damaged.
 */
final class Rows {

    /** Not instantiable. */
    private Rows() {
    }

    /**
     * Reads a user from columns named {@code <prefix>username},
     * {@code <prefix>password_hash}, {@code <prefix>salt} and {@code <prefix>role}.
     *
     * @param rs     the result set, positioned on a row
     * @param prefix column-name prefix, empty for none
     * @return the user
     * @throws SQLException if a column is missing or the stored values are invalid
     */
    static User user(ResultSet rs, String prefix) throws SQLException {
        try {
            return new User(rs.getString(prefix + "username"), rs.getString(prefix + "password_hash"),
                    rs.getString(prefix + "salt"), Role.valueOf(rs.getString(prefix + "role")));
        } catch (ValidationException | IllegalArgumentException e) {
            throw new SQLException("Stored user is invalid: " + e.getMessage(), e);
        }
    }

    /**
     * Reads an equipment item from columns named {@code <prefix>id},
     * {@code <prefix>name}, {@code <prefix>category}, {@code <prefix>description},
     * {@code <prefix>condition} and {@code <prefix>operational_status}.
     *
     * @param rs     the result set, positioned on a row
     * @param prefix column-name prefix, empty for none
     * @return the equipment item
     * @throws SQLException if a column is missing or the stored values are invalid
     */
    static Equipment equipment(ResultSet rs, String prefix) throws SQLException {
        try {
            return new Equipment(rs.getString(prefix + "id"), rs.getString(prefix + "name"),
                    rs.getString(prefix + "category"), rs.getString(prefix + "description"),
                    Condition.valueOf(rs.getString(prefix + "condition")),
                    OperationalStatus.valueOf(rs.getString(prefix + "operational_status")));
        } catch (ValidationException | IllegalArgumentException e) {
            throw new SQLException("Stored equipment is invalid: " + e.getMessage(), e);
        }
    }

    /**
     * Reads a date-time stored in the {@value DateTimes#PATTERN} format.
     *
     * @param rs     the result set, positioned on a row
     * @param column the column name
     * @return the date-time
     * @throws SQLException if the stored text is not a valid date-time
     */
    static LocalDateTime time(ResultSet rs, String column) throws SQLException {
        try {
            return DateTimes.parse(rs.getString(column));
        } catch (ValidationException e) {
            throw new SQLException("Stored time in " + column + " is invalid", e);
        }
    }
}
