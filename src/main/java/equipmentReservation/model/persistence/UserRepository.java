package equipmentReservation.model.persistence;

import equipmentReservation.model.NotFoundException;
import equipmentReservation.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Saves and loads {@link User} accounts (FR-01, FR-35). Contains no business
 * rules. Every method also has a version that takes a {@link Connection}, so
 * it can run inside {@link Database#inTransaction}.
 */
public class UserRepository {

    /** The database the users are stored in. */
    private final Database db;

    /**
     * Creates a repository for the given database.
     *
     * @param db the database; not null
     */
    public UserRepository(Database db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * Saves a user in its own transaction: inserts it, or updates the user
     * with the same username.
     *
     * @param u the user to save
     * @throws SQLException if the database operation fails
     */
    public void save(User u) throws SQLException {
        db.inTransaction(conn -> save(conn, u));
    }

    /**
     * Saves a user using an open connection: inserts it, or updates the user
     * with the same username.
     *
     * @param conn an open connection, usually inside a transaction
     * @param u    the user to save
     * @throws SQLException if the database operation fails
     */
    public void save(Connection conn, User u) throws SQLException {
        String sql = """
                INSERT INTO users (username, password_hash, salt, role) VALUES (?, ?, ?, ?)
                ON CONFLICT (username) DO UPDATE SET
                    password_hash = excluded.password_hash, salt = excluded.salt, role = excluded.role""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getSalt());
            ps.setString(4, u.getRole().name());
            ps.executeUpdate();
        }
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to look up
     * @return the user
     * @throws NotFoundException if no user has that username
     * @throws SQLException      if the database operation fails
     */
    public User findByUsername(String username) throws NotFoundException, SQLException {
        try (Connection conn = db.getConnection()) {
            return findByUsername(conn, username);
        }
    }

    /**
     * Finds a user by username using an open connection.
     *
     * @param conn     an open connection
     * @param username the username to look up
     * @return the user
     * @throws NotFoundException if no user has that username
     * @throws SQLException      if the database operation fails
     */
    public User findByUsername(Connection conn, String username) throws NotFoundException, SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NotFoundException("No user named \"" + username + "\".");
                }
                return Rows.user(rs, "");
            }
        }
    }
}
