package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.UserSession;
//import dev.vavateam1.util.SqlUtils;

public class UserSessionDaoImpl implements UserSessionDao {
    private static final Logger log = LoggerFactory.getLogger(UserSessionDaoImpl.class);

    private final ConnectionFactory connectionFactory;

    @Inject
    public UserSessionDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private UserSession mapResultSetToUserSession(ResultSet rs) throws SQLException {
        UserSession session = new UserSession();
        session.setId(rs.getInt("id"));
        session.setUserId(rs.getInt("user_id"));
        session.setLoginTime(rs.getObject("login_time", OffsetDateTime.class));
        session.setLogoutTime(rs.getObject("logout_time", OffsetDateTime.class));
        session.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        session.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        session.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
        return session;
    }

    public Optional<UserSession> findByUserId(long userId) {
        log.info("Looking up session for user id: {}", userId);
        String sql = "SELECT id, user_id, login_time, logout_time, created_at, updated_at FROM user_sessions WHERE user_id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                log.info("No session found for user id: {}", userId);
                return Optional.empty();
            }
            log.info("Session found for user id: {}", userId);
            return Optional.of(mapResultSetToUserSession(rs));
        } catch (SQLException e) {
            log.error("Failed to find user session for user id: {}", userId, e);
            throw new RuntimeException("Failed to find user session by user id: " + userId, e);
        }
    }

    public UserSession create(int userId) {
        log.info("Creating session for user id: {}", userId);
        String sql = "INSERT INTO user_sessions (user_id, login_time, created_at, updated_at) VALUES (?, NOW(), NOW(), NOW()) "
                + "RETURNING id, user_id, login_time, logout_time, created_at, updated_at, deleted_at";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to create user session for user id: " + userId);
                }
                UserSession session = mapResultSetToUserSession(rs);
                log.info("Session created with id: {} for user id: {}", session.getId(), userId);
                return session;
            }
        } catch (SQLException e) {
            log.error("Failed to create session for user id: {}", userId, e);
            throw new RuntimeException("Failed to create user session for user id: " + userId, e);
        }
    }

    public boolean close(long id) {
        log.info("Closing session id: {}", id);
        String sql = "UPDATE user_sessions SET logout_time = NOW(), updated_at = NOW() WHERE id = ? AND logout_time IS NULL";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            boolean closed = stmt.executeUpdate() > 0;
            if (closed) {
                log.info("Session closed id: {}", id);
            } else {
                log.info("Session id: {} was already closed", id);
            }
            return closed;
        } catch (SQLException e) {
            log.error("Failed to close session id: {}", id, e);
            throw new RuntimeException("Failed to close user session by id: " + id, e);
        }
    }

}
