package equipmentReservation.model.persistence;

import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.Reservation;
import equipmentReservation.model.ReservationStatus;
import equipmentReservation.model.ValidationException;
import equipmentReservation.util.DateTimes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Saves and loads {@link Reservation}s together with their borrower and item
 * (FR-13, FR-19, FR-35). Contains no business rules: overlap checks belong to
 * the model. Every method also has a version that takes a {@link Connection},
 * so a caller can check for conflicts and save in one transaction (FR-11, FR-12).
 */
public class ReservationRepository {

    /** Selects a reservation joined with its borrower (prefix u_) and item (prefix e_). */
    private static final String SELECT = """
            SELECT r.id, r.start_time, r.end_time, r.created_at, r.status,
                   u.username AS u_username, u.password_hash AS u_password_hash,
                   u.salt AS u_salt, u.role AS u_role,
                   e.id AS e_id, e.name AS e_name, e.category AS e_category,
                   e.description AS e_description, e.condition AS e_condition,
                   e.operational_status AS e_operational_status
            FROM reservations r
            JOIN users u ON u.username = r.borrower
            JOIN equipment e ON e.id = r.equipment_id
            """;

    /** The database the reservations are stored in. */
    private final Database db;

    /**
     * Creates a repository for the given database.
     *
     * @param db the database; not null
     */
    public ReservationRepository(Database db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * Saves a reservation in its own transaction. A new reservation is
     * inserted; for an existing id only the status is updated.
     *
     * @param r the reservation to save
     * @throws SQLException if the database operation fails, for example
     *                      because its borrower or item is not saved (FR-35)
     */
    public void save(Reservation r) throws SQLException {
        db.inTransaction(conn -> save(conn, r));
    }

    /**
     * Saves a reservation using an open connection. A new reservation is
     * inserted; for an existing id only the status is updated.
     *
     * @param conn an open connection, usually inside a transaction
     * @param r    the reservation to save
     * @throws SQLException if the database operation fails
     */
    public void save(Connection conn, Reservation r) throws SQLException {
        String sql = """
                INSERT INTO reservations (id, borrower, equipment_id, start_time, end_time, created_at, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET status = excluded.status""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getBorrower().getUsername());
            ps.setString(3, r.getItem().getId());
            ps.setString(4, DateTimes.format(r.getStart()));
            ps.setString(5, DateTimes.format(r.getEnd()));
            ps.setString(6, DateTimes.format(r.getCreatedAt()));
            ps.setString(7, r.getStatus().name());
            ps.executeUpdate();
        }
    }

    /**
     * Finds a reservation by id.
     *
     * @param id the id to look up
     * @return the reservation, with its borrower and item
     * @throws NotFoundException if no reservation has that id
     * @throws SQLException      if the database operation fails
     */
    public Reservation findById(String id) throws NotFoundException, SQLException {
        try (Connection conn = db.getConnection()) {
            return findById(conn, id);
        }
    }

    /**
     * Finds a reservation by id using an open connection.
     *
     * @param conn an open connection
     * @param id   the id to look up
     * @return the reservation, with its borrower and item
     * @throws NotFoundException if no reservation has that id
     * @throws SQLException      if the database operation fails
     */
    public Reservation findById(Connection conn, String id) throws NotFoundException, SQLException {
        List<Reservation> found = query(conn, SELECT + "WHERE r.id = ?", id);
        if (found.isEmpty()) {
            throw new NotFoundException("No reservation with ID \"" + id + "\".");
        }
        return found.get(0);
    }

    /**
     * Returns the ACTIVE reservations for one item, ordered by start.
     *
     * @param equipmentId the item's id
     * @return the active reservations; empty if there are none
     * @throws SQLException if the database operation fails
     */
    public List<Reservation> findActiveByEquipment(String equipmentId) throws SQLException {
        try (Connection conn = db.getConnection()) {
            return findActiveByEquipment(conn, equipmentId);
        }
    }

    /**
     * Returns the ACTIVE reservations for one item, ordered by start, using
     * an open connection.
     *
     * @param conn        an open connection
     * @param equipmentId the item's id
     * @return the active reservations; empty if there are none
     * @throws SQLException if the database operation fails
     */
    public List<Reservation> findActiveByEquipment(Connection conn, String equipmentId)
            throws SQLException {
        return query(conn, SELECT + "WHERE r.equipment_id = ? AND r.status = 'ACTIVE' ORDER BY r.start_time",
                equipmentId);
    }

    /**
     * Returns every reservation made by one borrower, ordered by start (FR-19).
     *
     * @param username the borrower's username
     * @return the borrower's reservations; empty if there are none
     * @throws SQLException if the database operation fails
     */
    public List<Reservation> findByBorrower(String username) throws SQLException {
        try (Connection conn = db.getConnection()) {
            return findByBorrower(conn, username);
        }
    }

    /**
     * Returns every reservation made by one borrower, ordered by start, using
     * an open connection.
     *
     * @param conn     an open connection
     * @param username the borrower's username
     * @return the borrower's reservations; empty if there are none
     * @throws SQLException if the database operation fails
     */
    public List<Reservation> findByBorrower(Connection conn, String username) throws SQLException {
        return query(conn, SELECT + "WHERE r.borrower = ? ORDER BY r.start_time", username);
    }

    /**
     * Runs a SELECT with one string parameter and maps every row.
     *
     * @param conn  an open connection
     * @param sql   the query
     * @param param the value for its single parameter
     * @return the reservations found
     * @throws SQLException if the query fails or a row is invalid
     */
    private List<Reservation> query(Connection conn, String sql, String param) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Turns the current row into a reservation with its borrower and item.
     *
     * @param rs the result set, positioned on a row
     * @return the reservation
     * @throws SQLException if the stored values are invalid
     */
    private Reservation map(ResultSet rs) throws SQLException {
        try {
            return Reservation.restore(rs.getString("id"), Rows.user(rs, "u_"), Rows.equipment(rs, "e_"),
                    Rows.time(rs, "start_time"), Rows.time(rs, "end_time"), Rows.time(rs, "created_at"),
                    ReservationStatus.valueOf(rs.getString("status")));
        } catch (ValidationException | IllegalArgumentException e) {
            throw new SQLException("Stored reservation is invalid: " + e.getMessage(), e);
        }
    }
}
