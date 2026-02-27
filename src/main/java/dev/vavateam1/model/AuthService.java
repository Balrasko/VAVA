package dev.vavateam1.model;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.vavateam1.model.UserDao.User;

public class AuthService {

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserDao userDao = new UserDao();

    /**
     * Returns true when the username exists and the password matches.
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            logger.log(Level.WARNING, "Authentication failed due to null username or password.");
            return false;
        }

        logger.log(Level.FINE, "Authentication attempt for user: {0}", username);

        Optional<User> user = userDao.findByUsername(username);
        if (user.isEmpty()) {
            logger.log(Level.INFO, "Authentication failed: user not found for username: {0}", username);
            return false;
        }

        boolean isPasswordCorrect = user.get().password().equals(password);

        if (isPasswordCorrect) {
            logger.log(Level.INFO, "Authentication successful for user: {0}", username);
        } else {
            logger.log(Level.INFO, "Authentication failed: invalid password for user: {0}", username);
        }

        return isPasswordCorrect;
    }
}
