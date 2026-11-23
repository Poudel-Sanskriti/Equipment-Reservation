package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import equipmentReservation.util.PasswordHasher;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PasswordHasher}: salted hashes that verify correctly and
 * never reveal the plain password (NFR-08).
 */
class PasswordHasherTest {

    @Test
    void verifiesRightPasswordAndRejectsWrongOne() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("password1", salt);
        assertTrue(PasswordHasher.verify("password1", salt, hash));
        assertFalse(PasswordHasher.verify("password2", salt, hash));
    }

    @Test
    void sameSaltGivesSameHashDifferentSaltGivesDifferentHash() {
        String salt = PasswordHasher.newSalt();
        assertEquals(PasswordHasher.hash("secret", salt), PasswordHasher.hash("secret", salt));
        assertNotEquals(PasswordHasher.hash("secret", salt),
                PasswordHasher.hash("secret", PasswordHasher.newSalt()));
    }

    @Test
    void hashIsNotThePlainPassword() {
        assertNotEquals("admin123", PasswordHasher.hash("admin123", PasswordHasher.newSalt()));
    }

    @Test
    void saltsAreRandomAndNotBlank() {
        String a = PasswordHasher.newSalt();
        String b = PasswordHasher.newSalt();
        assertFalse(a.isBlank());
        assertNotEquals(a, b);
    }

    @Test
    void nullPasswordThrows() {
        assertThrows(NullPointerException.class, () -> PasswordHasher.hash(null, PasswordHasher.newSalt()));
    }
}
