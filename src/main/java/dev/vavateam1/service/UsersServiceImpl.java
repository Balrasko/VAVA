package dev.vavateam1.service;

import java.util.List;

import com.google.inject.Inject;
import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;

public class UsersServiceImpl implements UsersService {
    private final UserDao userDao;

    @Inject
    public UsersServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<UserWithSessionDto> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public void createUser(User user) {
        userDao.createUser(user);
    }

    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @Override
    public void deleteUser(User user) {
        userDao.deleteUser(user.getId());
    }
}
