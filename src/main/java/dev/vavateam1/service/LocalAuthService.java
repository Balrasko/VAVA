package dev.vavateam1.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.google.inject.Inject;

import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dao.UserSessionDao;
import dev.vavateam1.data.config.SecurityConfig;
import dev.vavateam1.model.User;
import dev.vavateam1.model.UserSession;

public class LocalAuthService implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(LocalAuthService.class);

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
    public boolean login(String email, String password) {
        log.info("Login attempt for email: {}", email);
        Optional<User> foundUser = userDao.findByEmail(email);
        if (foundUser.isEmpty()) {
            log.info("Login failed: no user found for email: {}", email);
            return false;
        }

        User user = foundUser.get();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(SecurityConfig.BCRYPT_STRENGTH);
        if (!encoder.matches(password, user.getPasswordHash())) {
            log.info("Login failed: invalid password for email: {}", email);
            return false;
        }

        currentUser = user;
        currentSession = userSessionDao.create(user.getId());
        log.info("Login successful for user id: {} ({})", user.getId(), email);
        return true;
    }

    @Override
    public User getUser() {
        return currentUser;
    }

    @Override
    public void logout() {
        if (currentSession != null) {
            log.info("Logging out user id: {}", currentUser != null ? currentUser.getId() : "unknown");
            userSessionDao.close(currentSession.getId());
        }

        currentSession = null;
        currentUser = null;
    }
}
