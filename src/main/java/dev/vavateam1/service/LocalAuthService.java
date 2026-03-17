package dev.vavateam1.service;

import java.util.Objects;
import java.util.Optional;

import com.google.inject.Inject;

import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dao.UserSessionDao;
import dev.vavateam1.model.User;
import dev.vavateam1.model.UserSession;

public class LocalAuthService implements AuthService {
    private final UserDao userDao;
    private final UserSessionDao userSessionDao;

    private User currentUser;
    private UserSession currentSession;

    @Inject
    public LocalAuthService(UserDao userDao, UserSessionDao userSessionDao) {
        this.userDao = userDao;
        this.userSessionDao = userSessionDao;
    }

    @Override
    public boolean login(String emailOrUsername, String password) {
        Optional<User> foundUser = userDao.findByEmailOrUsername(emailOrUsername);
        if (foundUser.isEmpty()) {
            return false;
        }

        // TODO: hash password!!
        User user = foundUser.get();
        if (!Objects.equals(user.getPassword(), password)) {
            return false;
        }

        currentUser = user;
        currentSession = userSessionDao.create(user.getId());
        return true;
    }

    @Override
    public User getUser() {
        return currentUser;
    }

    @Override
    public void logout() {
        if (currentSession != null) {
            userSessionDao.close(currentSession.getId());
        }

        currentSession = null;
        currentUser = null;
    }
}
