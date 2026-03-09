package dev.vavateam1.dao;

import java.util.Optional;
import dev.vavateam1.model.UserSession;

public interface UserSessionDao {
    Optional<UserSession> findByUserId(long userId);

    UserSession create(int userId);

    boolean close(long id);
}
