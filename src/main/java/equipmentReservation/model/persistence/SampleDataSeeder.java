package equipmentReservation.model.persistence;

import equipmentReservation.model.Condition;
import equipmentReservation.model.Equipment;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.ValidationException;
import equipmentReservation.util.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Fills an empty database with demo accounts and equipment, so the
 * application has something to show on its first run. A database that
 * already has users is never changed.
 *
 * <p>Demo accounts: {@code admin1}/{@code admin123} (administrator),
 * {@code jsmith}/{@code password1} and {@code mlee}/{@code password2}
 * (borrowers). Passwords are stored only as salted hashes (NFR-08).</p>
 */
public class SampleDataSeeder {

    /** Demo accounts: username, password, role. */
    private static final List<String[]> ACCOUNTS = List.of(
            new String[] {"admin1", "admin123", "ADMIN"},
            new String[] {"jsmith", "password1", "BORROWER"},
            new String[] {"mlee", "password2", "BORROWER"});

    /** Demo items: id, name, category, description, condition, operational status. */
    private static final List<String[]> ITEMS = List.of(
            new String[] {"EQ-001", "Dell Latitude 5440", "Laptop", "14-inch, charger included", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-002", "MacBook Air M2", "Laptop", "13-inch, USB-C charger", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-003", "Lenovo ThinkPad T14", "Laptop", "Scratches on lid", "FAIR", "OPERATIONAL"},
            new String[] {"EQ-004", "Canon EOS R50", "Camera", "Kit lens, 64 GB card", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-005", "Nikon D3500", "Camera", "Lens cap missing", "DAMAGED", "NON_OPERATIONAL"},
            new String[] {"EQ-006", "GoPro HERO12", "Camera", "Waterproof case", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-007", "Epson PowerLite", "Projector", "HDMI cable included", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-008", "BenQ Portable Projector", "Projector", "Carry bag", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-009", "TI-84 Plus CE", "Calculator", "Graphing calculator", "GOOD", "OPERATIONAL"},
            new String[] {"EQ-010", "Casio fx-991EX", "Calculator", "Scientific calculator", "GOOD", "OPERATIONAL"});

    /** The database to fill. */
    private final Database db;

    /** Saves the demo accounts. */
    private final UserRepository users;

    /** Saves the demo items. */
    private final EquipmentRepository equipment;

    /**
     * Creates a seeder for the given database.
     *
     * @param db the database to fill; its schema must already exist
     */
    public SampleDataSeeder(Database db) {
        this.db = Objects.requireNonNull(db, "db");
        this.users = new UserRepository(db);
        this.equipment = new EquipmentRepository(db);
    }

    /**
     * Saves the demo accounts and equipment in one transaction, but only if
     * the database has no users yet. Otherwise does nothing.
     *
     * @throws SQLException if the database operation fails; nothing is saved
     */
    public void seedIfEmpty() throws SQLException {
        db.inTransaction(conn -> {
            if (hasUsers(conn)) {
                return;
            }
            try {
                for (String[] a : ACCOUNTS) {
                    String salt = PasswordHasher.newSalt();
                    users.save(conn, new User(a[0], PasswordHasher.hash(a[1], salt), salt, Role.valueOf(a[2])));
                }
                for (String[] i : ITEMS) {
                    equipment.save(conn, new Equipment(i[0], i[1], i[2], i[3],
                            Condition.valueOf(i[4]), OperationalStatus.valueOf(i[5])));
                }
            } catch (ValidationException e) {
                throw new SQLException("Sample data is invalid: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Tells whether the database already has at least one user.
     *
     * @param conn an open connection
     * @return {@code true} if any user exists
     * @throws SQLException if the query fails
     */
    private static boolean hasUsers(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT EXISTS (SELECT 1 FROM users)");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        }
    }
}
