package dev.vavateam1.service;

import java.util.List;

import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;

public interface UsersService {
    List<UserWithSessionDto> getAllUsers();

    void createUser(User user);

    void updateUser(User user);

    void deleteUser(User user);
}
