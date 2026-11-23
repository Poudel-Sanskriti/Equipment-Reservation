package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import equipmentReservation.model.ValidationException;
import equipmentReservation.model.persistence.Database;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Database#inTransaction}: work is saved all-or-nothing and
 * the original exception reaches the caller unchanged (FR-37).
 */
class DatabaseTransactionTest {

    private static final String INSERT_ITEM =
            "INSERT INTO equipment VALUES ('EQ-1', 'Laptop', 'Laptop', '', 'GOOD', 'OPERATIONAL')";
    private static final String INSERT_ITEM_2 =
            "INSERT INTO equipment VALUES ('EQ-2', 'Camera', 'Camera', '', 'GOOD', 'OPERATIONAL')";

    @TempDir
    Path dir;

    private Database db;

    @BeforeEach
    void setUp() throws SQLException {
        db = new Database(dir.resolve("test.db").toString());
        db.createSchema();
    }

    private int equipmentCount() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM equipment")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void successfulWorkIsCommitted() throws SQLException {
        db.inTransaction(conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute(INSERT_ITEM);
                st.execute(INSERT_ITEM_2);
            }
        });
        assertEquals(2, equipmentCount());
    }

    @Test
    void sqlErrorHalfwayLeavesNoRows() throws SQLException {
        assertThrows(SQLException.class, () -> db.inTransaction(conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute(INSERT_ITEM);
                st.execute(INSERT_ITEM);   // duplicate primary key
            }
        }));
        assertEquals(0, equipmentCount());
    }

    @Test
    void domainExceptionRollsBackAndIsRethrownUnchanged() throws SQLException {
        ValidationException original = new ValidationException("End must be later than start.");
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> db.<ValidationException>inTransaction(conn -> {
                    try (Statement st = conn.createStatement()) {
                        st.execute(INSERT_ITEM);
                    }
                    throw original;
                }));
        assertSame(original, thrown);
        assertEquals(0, equipmentCount());
    }

    @Test
    void runtimeExceptionAlsoRollsBack() throws SQLException {
        assertThrows(IllegalStateException.class, () -> db.inTransaction(conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute(INSERT_ITEM);
            }
            throw new IllegalStateException("boom");
        }));
        assertEquals(0, equipmentCount());
    }
}
