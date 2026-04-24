package dev.vavateam1.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;

public class UsersServiceImpl implements UsersService {
    private static final Logger log = LoggerFactory.getLogger(UsersServiceImpl.class);

    private final UserDao userDao;

    @Inject
    public UsersServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<UserWithSessionDto> getAllUsers() {
        log.info("Fetching all users");
        List<UserWithSessionDto> users = userDao.getAllUsers();
        log.info("Fetched {} users", users.size());
        return users;
    }

    @Override
    public void createUser(User user) {
        log.info("Creating user: {}", user.getEmail());
        userDao.createUser(user);
        log.info("User created: {}", user.getEmail());
    }

    @Override
    public void updateUser(User user) {
        log.info("Updating user id: {}", user.getId());
        userDao.updateUser(user);
        log.info("User updated id: {}", user.getId());
    }

    @Override
    public void deleteUser(User user) {
        log.info("Deleting user id: {}", user.getId());
        userDao.deleteUser(user.getId());
        log.info("User deleted id: {}", user.getId());
    }
}
