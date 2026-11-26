package entity;

import java.util.Objects;

/**
 * A registered user of the app.
 * The entity stores only a hash of the password, never the plain text;
 * hashing and checking passwords is done in the use case layer by a PasswordHasher.
 * Two users are equal when they have the same id.
 */
public class User {
    private final String id;
    private final String username;
    private final String passwordHash;

    /**
     * Creates a user.
     *
     * @param id           a unique id for this user
     * @param username     the username used to log in; leading and trailing spaces are removed
     * @param passwordHash the hashed password
     * @throws IllegalArgumentException if any argument is null or blank
     */
    public User(String id, String username, String passwordHash) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id must not be empty.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be empty.");
        }
        this.id = id;
        this.username = username.trim();
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User other = (User) o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns the username. The password hash is left out on purpose so it
     * does not end up in logs or error messages.
     */
    @Override
    public String toString() {
        return username;
    }
}
