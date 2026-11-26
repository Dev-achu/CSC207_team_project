package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void constructorStoresFields() {
        User user = new User("u1", "haroun", "hash123");
        assertEquals("u1", user.getId());
        assertEquals("haroun", user.getUsername());
        assertEquals("hash123", user.getPasswordHash());
    }

    @Test
    void usernameIsTrimmed() {
        User user = new User("u1", "  haroun  ", "hash123");
        assertEquals("haroun", user.getUsername());
    }

    @Test
    void nullOrBlankIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, "haroun", "hash"));
        assertThrows(IllegalArgumentException.class, () -> new User(" ", "haroun", "hash"));
    }

    @Test
    void nullOrBlankUsernameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new User("u1", null, "hash"));
        assertThrows(IllegalArgumentException.class, () -> new User("u1", "", "hash"));
    }

    @Test
    void nullOrBlankPasswordHashThrows() {
        assertThrows(IllegalArgumentException.class, () -> new User("u1", "haroun", null));
        assertThrows(IllegalArgumentException.class, () -> new User("u1", "haroun", "  "));
    }

    @Test
    void usersWithSameIdAreEqual() {
        User a = new User("u1", "haroun", "hash1");
        User b = new User("u1", "other", "hash2");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void usersWithDifferentIdsAreNotEqual() {
        assertNotEquals(new User("u1", "haroun", "hash"), new User("u2", "haroun", "hash"));
    }

    @Test
    void toStringDoesNotExposePasswordHash() {
        User user = new User("u1", "haroun", "secretHash");
        assertFalse(user.toString().contains("secretHash"));
    }
}
