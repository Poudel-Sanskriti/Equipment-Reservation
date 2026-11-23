package equipmentReservation.model.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.sqlite.SQLiteConfig;

/**
 * The application's local SQLite database (FR-35, FR-36). Creates the
 * schema and hands out connections that always enforce foreign keys.
 *
 * <p>Every connection uses IMMEDIATE transactions with a busy timeout, so
 * two writers are serialized instead of interleaving (groundwork for FR-12).
 * Rows are never deleted: a trigger on every table rejects DELETE
 * (SRS §4.10).</p>
 */
public class Database {

    /** How long a connection waits for another writer before failing, in milliseconds. */
    private static final int BUSY_TIMEOUT_MS = 5_000;

    /** Names of every table, used for the no-delete triggers. */
    private static final List<String> TABLES = List.of(
            "users", "equipment", "reservations", "checkouts", "maintenance_records", "settings");

    /** SQL statements that create the schema; each is safe to run more than once. */
    private static final List<String> SCHEMA = List.of(
            """
            CREATE TABLE IF NOT EXISTS users (
                username      TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                salt          TEXT NOT NULL,
                role          TEXT NOT NULL CHECK (role IN ('BORROWER', 'ADMIN'))
            )""",
            """
            CREATE TABLE IF NOT EXISTS equipment (
                id                 TEXT PRIMARY KEY,
                name               TEXT NOT NULL,
                category           TEXT NOT NULL,
                description        TEXT NOT NULL DEFAULT '',
                condition          TEXT NOT NULL CHECK (condition IN ('GOOD', 'FAIR', 'DAMAGED')),
                operational_status TEXT NOT NULL
                    CHECK (operational_status IN ('OPERATIONAL', 'NON_OPERATIONAL'))
            )""",
            """
            CREATE TABLE IF NOT EXISTS reservations (
                id           TEXT PRIMARY KEY,
                borrower     TEXT NOT NULL REFERENCES users (username),
                equipment_id TEXT NOT NULL REFERENCES equipment (id),
                start_time   TEXT NOT NULL,
                end_time     TEXT NOT NULL,
                created_at   TEXT NOT NULL,
                status       TEXT NOT NULL
                    CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'COMPLETED', 'CANCELLED', 'EXPIRED')),
                CHECK (end_time > start_time)
            )""",
            """
            CREATE INDEX IF NOT EXISTS idx_reservations_equipment_status
                ON reservations (equipment_id, status)""",
            """
            CREATE TABLE IF NOT EXISTS checkouts (
                reservation_id   TEXT PRIMARY KEY REFERENCES reservations (id),
                checked_out_at   TEXT NOT NULL,
                checked_out_by   TEXT NOT NULL REFERENCES users (username),
                returned_at      TEXT,
                return_condition TEXT
                    CHECK (return_condition IS NULL OR return_condition IN ('GOOD', 'FAIR', 'DAMAGED'))
            )""",
            """
            CREATE TABLE IF NOT EXISTS maintenance_records (
                id                     INTEGER PRIMARY KEY AUTOINCREMENT,
                equipment_id           TEXT NOT NULL REFERENCES equipment (id),
                reason                 TEXT NOT NULL,
                started_at             TEXT NOT NULL,
                returned_to_service_at TEXT
            )""",
            """
            CREATE TABLE IF NOT EXISTS settings (
                key   TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )""",
            "INSERT OR IGNORE INTO settings (key, value) VALUES ('pickup_window_minutes', '30')");

    /** JDBC URL of the database file. */
    private final String url;

    /**
     * Creates a database backed by the given file. The file is created when
     * first used.
     *
     * @param path path of the SQLite database file
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public Database(String path) {
        this.url = "jdbc:sqlite:" + Objects.requireNonNull(path, "path");
    }

    /**
     * Opens a new connection with foreign keys enforced (FR-35), IMMEDIATE
     * transactions and a busy timeout. The caller must close it.
     *
     * @return a new connection
     * @throws SQLException if the database cannot be opened
     */
    public Connection getConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
        config.setBusyTimeout(BUSY_TIMEOUT_MS);
        return config.createConnection(url);
    }

    /**
     * Creates every table, index and trigger that does not exist yet, and
     * the default pickup window of 30 minutes (FR-15). Safe to call on every
     * start-up.
     *
     * @throws SQLException if the schema cannot be created
     */
    public void createSchema() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String sql : SCHEMA) {
                st.execute(sql);
            }
            for (String table : TABLES) {
                st.execute("CREATE TRIGGER IF NOT EXISTS no_delete_" + table
                        + " BEFORE DELETE ON " + table
                        + " BEGIN SELECT RAISE(ABORT, 'Records are never deleted'); END");
            }
        }
    }
}
