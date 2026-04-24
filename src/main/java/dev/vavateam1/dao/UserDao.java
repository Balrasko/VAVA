package dev.vavateam1.dao;

import java.util.List;
import java.util.Optional;

import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;

public interface UserDao {
    Optional<User> findByEmail(String email);

    List<UserWithSessionDto> getAllUsers();

    void createUser(User user);

    void updateUser(User user);

    void deleteUser(int userId);
}
