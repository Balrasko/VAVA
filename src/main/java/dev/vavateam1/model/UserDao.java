package dev.vavateam1.model;

import java.util.List;
import java.util.Optional;

public class UserDao {

    public record User(String username, String password) {
    }

    private static final List<User> USERS = List.of(new User("admin", "admin"));

    public Optional<User> findByUsername(String username) {
        return USERS.stream()
                .filter(u -> u.username().equals(username))
                .findFirst();
    }
}
