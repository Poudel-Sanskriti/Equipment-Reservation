package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.model.Equipment;
import equipmentReservation.model.OperationalStatus;
import equipmentReservation.model.Role;
import equipmentReservation.model.User;
import equipmentReservation.model.persistence.Database;
import equipmentReservation.model.persistence.EquipmentRepository;
import equipmentReservation.model.persistence.SampleDataSeeder;
import equipmentReservation.model.persistence.UserRepository;
import equipmentReservation.util.PasswordHasher;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link SampleDataSeeder}: an empty database gets the demo
 * accounts (with hashed passwords, NFR-08) and equipment; a database that
 * already has users is never changed.
 */
class SampleDataSeederTest {

    @TempDir
    Path dir;

    private Database db;
    private UserRepository users;
    private EquipmentRepository equipment;

    @BeforeEach
    void setUp() throws SQLException {
        db = new Database(dir.resolve("test.db").toString());
        db.createSchema();
        users = new UserRepository(db);
        equipment = new EquipmentRepository(db);
    }

    private int userCount() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void emptyDatabaseGetsThreeUsersAndTenItems() throws Exception {
        new SampleDataSeeder(db).seedIfEmpty();
        assertEquals(3, userCount());
        assertEquals(10, equipment.findAll().size());
    }

    @Test
    void demoPasswordsAreHashedAndVerify() throws Exception {
        new SampleDataSeeder(db).seedIfEmpty();
        String[][] accounts = {{"admin1", "admin123"}, {"jsmith", "password1"}, {"mlee", "password2"}};
        for (String[] a : accounts) {
            User u = users.findByUsername(a[0]);
            assertTrue(PasswordHasher.verify(a[1], u.getSalt(), u.getPasswordHash()), a[0]);
            assertNotEquals(a[1], u.getPasswordHash());
        }
    }

    @Test
    void demoRolesAreCorrect() throws Exception {
        new SampleDataSeeder(db).seedIfEmpty();
        assertEquals(Role.ADMIN, users.findByUsername("admin1").getRole());
        assertEquals(Role.BORROWER, users.findByUsername("jsmith").getRole());
        assertEquals(Role.BORROWER, users.findByUsername("mlee").getRole());
    }

    @Test
    void seedingTwiceAddsNothingMore() throws Exception {
        SampleDataSeeder seeder = new SampleDataSeeder(db);
        seeder.seedIfEmpty();
        seeder.seedIfEmpty();
        assertEquals(3, userCount());
        assertEquals(10, equipment.findAll().size());
    }

    @Test
    void databaseWithUsersIsLeftUnchanged() throws Exception {
        String salt = PasswordHasher.newSalt();
        users.save(new User("owner", PasswordHasher.hash("pw", salt), salt, Role.ADMIN));
        new SampleDataSeeder(db).seedIfEmpty();
        assertEquals(1, userCount());
        assertTrue(equipment.findAll().isEmpty());
    }

    @Test
    void itemsCoverFourCategoriesWithOneNonOperational() throws Exception {
        new SampleDataSeeder(db).seedIfEmpty();
        List<Equipment> items = equipment.findAll();
        Set<String> categories = items.stream().map(Equipment::getCategory).collect(Collectors.toSet());
        assertEquals(Set.of("Laptop", "Camera", "Projector", "Calculator"), categories);
        assertEquals(1, items.stream()
                .filter(e -> e.getOperationalStatus() == OperationalStatus.NON_OPERATIONAL).count());
    }
}
