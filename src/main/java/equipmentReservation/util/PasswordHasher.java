package equipmentReservation.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Creates and checks salted password hashes so that plain passwords are
 * never stored (NFR-08). Uses PBKDF2 with HMAC-SHA256 from the JDK.
 */
public final class PasswordHasher {

    /** PBKDF2 algorithm name. */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /** Number of PBKDF2 iterations; makes guessing passwords slow. */
    private static final int ITERATIONS = 65_536;

    /** Length of the derived key in bits. */
    private static final int KEY_BITS = 256;

    /** Length of a new salt in bytes. */
    private static final int SALT_BYTES = 16;

    /** Source of random salts. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Not instantiable; all methods are static. */
    private PasswordHasher() {
    }

    /**
     * Creates a new random salt.
     *
     * @return 16 random bytes, Base64-encoded
     */
    public static String newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with the given salt.
     *
     * @param password the plain password
     * @param salt     the salt, as returned by {@link #newSalt()}
     * @return the hash, Base64-encoded
     * @throws NullPointerException if {@code password} or {@code salt} is {@code null}
     */
    public static String hash(String password, String salt) {
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(salt, "salt");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),
                salt.getBytes(StandardCharsets.UTF_8), ITERATIONS, KEY_BITS);
        try {
            byte[] key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ALGORITHM + " is not available in this JDK", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Checks a password against a stored hash. The comparison takes the same
     * time whether or not the hashes match.
     *
     * @param password     the plain password to check
     * @param salt         the salt stored with the hash
     * @param expectedHash the stored hash
     * @return {@code true} if the password matches
     * @throws NullPointerException if any argument is {@code null}
     */
    public static boolean verify(String password, String salt, String expectedHash) {
        Objects.requireNonNull(expectedHash, "expectedHash");
        byte[] actual = hash(password, salt).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }
}
