package dev.vavateam1.dao;

import java.util.Optional;
import dev.vavateam1.model.User;

public interface UserDao {
    Optional<User> findByEmailOrUsername(String emailOrUsername);
}
