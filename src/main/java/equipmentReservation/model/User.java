package equipmentReservation.model;

/**
 * A local user account (FR-01). The password is stored only as a salted hash
 * (NFR-08); the role decides which operations the user may perform.
 */
public class User extends AbstractModel {

    /** Unique login name. */
    private final String username;

    /** Salted hash of the password; the plain password is never stored. */
    private final String passwordHash;

    /** Random salt used when hashing the password. */
    private final String salt;

    /** The account's role. */
    private final Role role;

    /**
     * Creates a user account.
     *
     * @param username     unique login name; not blank
     * @param passwordHash salted password hash; not blank
     * @param salt         salt used for the hash; not blank
     * @param role         the account's role; not null
     * @throws ValidationException if any argument is null or blank
     */
    public User(String username, String passwordHash, String salt, Role role)
            throws ValidationException {
        this.username = Checks.requireText(username, "Username");
        this.passwordHash = Checks.requireText(passwordHash, "Password hash");
        this.salt = Checks.requireText(salt, "Salt");
        this.role = Checks.requireValue(role, "Role");
    }

    /**
     * Returns the login name.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the salted password hash.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns the salt used for the password hash.
     *
     * @return the salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Returns the account's role.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Tells whether this account is an administrator (FR-20, NFR-08).
     *
     * @return {@code true} if the role is {@link Role#ADMIN}
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
