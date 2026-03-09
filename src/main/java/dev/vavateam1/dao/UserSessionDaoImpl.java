package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import com.google.inject.Inject;
import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.UserSession;
import dev.vavateam1.util.SqlUtils;

public class UserSessionDaoImpl implements UserSessionDao {
    private final ConnectionFactory connectionFactory;

    @Inject
    public UserSessionDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<UserSession> findByUserId(long userId) {
        String sql = "SELECT id, user_id, login_time, logout_time, created_at, updated_at FROM user_sessions WHERE user_id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            UserSession session = new UserSession();
            session.setId(resultSet.getInt("id"));
            session.setUserId(resultSet.getInt("user_id"));
            session.setLoginTime(SqlUtils.toLocalDateTime(resultSet.getTimestamp("login_time")));
            session.setLogoutTime(SqlUtils.toLocalDateTime(resultSet.getTimestamp("logout_time")));
            session.setCreatedAt(SqlUtils.toLocalDateTime(resultSet.getTimestamp("created_at")));
            session.setUpdatedAt(SqlUtils.toLocalDateTime(resultSet.getTimestamp("updated_at")));

            return Optional.of(session);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user session by user id: " + userId, e);
        }
    }

    public UserSession create(int userId) {
        String sql = "INSERT INTO user_sessions (user_id, login_time, created_at, updated_at) VALUES (?, NOW(), NOW(), NOW()) RETURNING id, user_id, login_time, logout_time, created_at, updated_at";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RuntimeException(
                            "Failed to create user session for user id: " + userId);
                }

                UserSession session = new UserSession();
                session.setId(resultSet.getInt("id"));
                session.setUserId(resultSet.getInt("user_id"));
                session.setLoginTime(
                        SqlUtils.toLocalDateTime(resultSet.getTimestamp("login_time")));
                session.setLogoutTime(
                        SqlUtils.toLocalDateTime(resultSet.getTimestamp("logout_time")));
                session.setCreatedAt(
                        SqlUtils.toLocalDateTime(resultSet.getTimestamp("created_at")));
                session.setUpdatedAt(
                        SqlUtils.toLocalDateTime(resultSet.getTimestamp("updated_at")));
                return session;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user session for user id: " + userId, e);
        }
    }

    public boolean close(long id) {
        String sql = "UPDATE user_sessions SET logout_time = NOW(), updated_at = NOW() WHERE id = ? AND logout_time IS NULL";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close user session by id: " + id, e);
        }
    }

}
