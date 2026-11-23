package test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.model.persistence.Database;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the SQLite schema created by {@link Database#createSchema()}:
 * tables, constraints, foreign keys (FR-35), no deletes (SRS §4.10),
 * the default pickup window (FR-15) and persistence across restarts (FR-36).
 */
class DatabaseSchemaTest {

    @TempDir
    Path dir;

    private Database db;

    @BeforeEach
    void setUp() throws SQLException {
        db = new Database(dir.resolve("test.db").toString());
        db.createSchema();
    }

    private static void insertUserAndItem(Statement st) throws SQLException {
        st.execute("INSERT INTO users VALUES ('jsmith', 'h', 's', 'BORROWER')");
        st.execute("INSERT INTO equipment VALUES ('EQ-1', 'Laptop', 'Laptop', '', 'GOOD', 'OPERATIONAL')");
    }

    @Test
    void createsAllSixTables() throws SQLException {
        Set<String> tables = new HashSet<>();
        try (Connection c = db.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table'")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }
        assertTrue(tables.containsAll(Set.of("users", "equipment", "reservations",
                "checkouts", "maintenance_records", "settings")), tables.toString());
    }

    @Test
    void createSchemaTwiceIsSafe() {
        assertDoesNotThrow(() -> db.createSchema());
    }

    @Test
    void defaultPickupWindowIsThirtyMinutes() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT value FROM settings WHERE key = 'pickup_window_minutes'")) {
            assertTrue(rs.next());
            assertEquals("30", rs.getString(1));
        }
    }

    @Test
    void foreignKeysAreEnforced() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            st.execute("INSERT INTO equipment VALUES ('EQ-1', 'Laptop', 'Laptop', '', 'GOOD', 'OPERATIONAL')");
            assertThrows(SQLException.class, () -> st.execute(
                    "INSERT INTO reservations VALUES ('R-1', 'nobody', 'EQ-1', "
                    + "'2026-12-01 10:00', '2026-12-01 12:00', '2026-11-30 09:00', 'ACTIVE')"));
        }
    }

    @Test
    void endMustBeAfterStart() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            insertUserAndItem(st);
            assertThrows(SQLException.class, () -> st.execute(
                    "INSERT INTO reservations VALUES ('R-1', 'jsmith', 'EQ-1', "
                    + "'2026-12-01 12:00', '2026-12-01 10:00', '2026-11-30 09:00', 'ACTIVE')"));
        }
    }

    @Test
    void invalidStatusIsRejected() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            insertUserAndItem(st);
            assertThrows(SQLException.class, () -> st.execute(
                    "INSERT INTO reservations VALUES ('R-1', 'jsmith', 'EQ-1', "
                    + "'2026-12-01 10:00', '2026-12-01 12:00', '2026-11-30 09:00', 'LOST')"));
        }
    }

    @Test
    void rowsCanNeverBeDeleted() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            insertUserAndItem(st);
            SQLException e = assertThrows(SQLException.class,
                    () -> st.execute("DELETE FROM equipment WHERE id = 'EQ-1'"));
            assertTrue(e.getMessage().contains("never deleted"), e.getMessage());
        }
    }

    @Test
    void dataSurvivesReopeningTheDatabase() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            insertUserAndItem(st);
        }
        Database reopened = new Database(dir.resolve("test.db").toString());
        reopened.createSchema();
        try (Connection c = reopened.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM equipment WHERE id = 'EQ-1'")) {
            assertTrue(rs.next());
            assertEquals("Laptop", rs.getString(1));
        }
    }
}
