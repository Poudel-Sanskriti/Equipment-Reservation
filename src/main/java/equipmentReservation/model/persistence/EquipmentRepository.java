package equipmentReservation.model.persistence;

import equipmentReservation.model.Equipment;
import equipmentReservation.model.NotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Saves and loads {@link Equipment} items (FR-03, FR-35). Contains no
 * business rules. Every method also has a version that takes a
 * {@link Connection}, so it can run inside {@link Database#inTransaction}.
 */
public class EquipmentRepository {

    /** The database the equipment is stored in. */
    private final Database db;

    /**
     * Creates a repository for the given database.
     *
     * @param db the database; not null
     */
    public EquipmentRepository(Database db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * Saves an item in its own transaction: inserts it, or updates the item
     * with the same id. The id itself never changes (FR-21) and the row is
     * never removed, so its history is kept (FR-22).
     *
     * @param e the item to save
     * @throws SQLException if the database operation fails
     */
    public void save(Equipment e) throws SQLException {
        db.inTransaction(conn -> save(conn, e));
    }

    /**
     * Saves an item using an open connection: inserts it, or updates the item
     * with the same id.
     *
     * @param conn an open connection, usually inside a transaction
     * @param e    the item to save
     * @throws SQLException if the database operation fails
     */
    public void save(Connection conn, Equipment e) throws SQLException {
        String sql = """
                INSERT INTO equipment (id, name, category, description, condition, operational_status)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    name = excluded.name, category = excluded.category,
                    description = excluded.description, condition = excluded.condition,
                    operational_status = excluded.operational_status""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getCategory());
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getCondition().name());
            ps.setString(6, e.getOperationalStatus().name());
            ps.executeUpdate();
        }
    }

    /**
     * Finds an item by id.
     *
     * @param id the id to look up
     * @return the item
     * @throws NotFoundException if no item has that id
     * @throws SQLException      if the database operation fails
     */
    public Equipment findById(String id) throws NotFoundException, SQLException {
        try (Connection conn = db.getConnection()) {
            return findById(conn, id);
        }
    }

    /**
     * Finds an item by id using an open connection.
     *
     * @param conn an open connection
     * @param id   the id to look up
     * @return the item
     * @throws NotFoundException if no item has that id
     * @throws SQLException      if the database operation fails
     */
    public Equipment findById(Connection conn, String id) throws NotFoundException, SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM equipment WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NotFoundException("No equipment with ID \"" + id + "\".");
                }
                return Rows.equipment(rs, "");
            }
        }
    }

    /**
     * Returns every item, ordered by id.
     *
     * @return all items; empty if there are none
     * @throws SQLException if the database operation fails
     */
    public List<Equipment> findAll() throws SQLException {
        try (Connection conn = db.getConnection()) {
            return findAll(conn);
        }
    }

    /**
     * Returns every item, ordered by id, using an open connection.
     *
     * @param conn an open connection
     * @return all items; empty if there are none
     * @throws SQLException if the database operation fails
     */
    public List<Equipment> findAll(Connection conn) throws SQLException {
        List<Equipment> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM equipment ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(Rows.equipment(rs, ""));
            }
        }
        return items;
    }
}
