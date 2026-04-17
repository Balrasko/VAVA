package dev.vavateam1.dao;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;
import dev.vavateam1.model.UserSession;
//import dev.vavateam1.util.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;

public class UserDaoImpl implements UserDao {
    private final ConnectionFactory connectionFactory;

    @Inject
    public UserDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<User> findByEmailOrUsername(String emailOrUsername) {
        String sql = "SELECT id, role_id, name, email, password, status, created_at, updated_at, deleted_at "
                + "FROM users WHERE (LOWER(email) = LOWER(?) OR LOWER(name) = LOWER(?)) AND deleted_at IS NULL";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailOrUsername);
            stmt.setString(2, emailOrUsername);

            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setRoleId(resultSet.getInt("role_id"));
            user.setName(resultSet.getString("name"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            user.setStatus((Boolean) resultSet.getObject("status"));
            user.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
            user.setUpdatedAt(resultSet.getObject("updated_at", OffsetDateTime.class));
            user.setDeletedAt(resultSet.getObject("deleted_at", OffsetDateTime.class));

            return Optional.of(user);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email or username: " + emailOrUsername, e);
        }
    }

    @Override
    public List<UserWithSessionDto> getAllUsers() {
        String sql = """
                    SELECT
                        u.id,
                        u.role_id,
                        u.name,
                        u.email,
                        u.status,
                        u.created_at,
                        u.updated_at,
                        u.deleted_at,
                        s.login_time,
                        s.logout_time
                    FROM users u
                    LEFT JOIN user_sessions s
                        ON s.user_id = u.id
                        AND s.deleted_at IS NULL
                    WHERE u.deleted_at IS NULL
                    AND (
                        s.id IS NULL OR
                        s.login_time = (
                            SELECT MAX(s2.login_time)
                            FROM user_sessions s2
                            WHERE s2.user_id = u.id
                            AND s2.deleted_at IS NULL
                        )
                    )
                    ORDER BY u.id;
                """;

        List<UserWithSessionDto> users = new java.util.ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UserWithSessionDto dto = new UserWithSessionDto();

                User user = new User();
                user.setId(rs.getInt("id"));
                user.setRoleId(rs.getInt("role_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(null);
                user.setStatus(rs.getObject("status") == null ? false : rs.getBoolean("status"));
                user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                user.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                user.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));

                OffsetDateTime loginTime = rs.getObject("login_time", OffsetDateTime.class);
                UserSession session = null;
                if (loginTime != null) {
                    session = new UserSession();
                    session.setUserId(user.getId());
                    session.setLoginTime(loginTime);
                    session.setLogoutTime(rs.getObject("logout_time", OffsetDateTime.class));
                }

                dto.setUser(user);
                dto.setSession(session);

                users.add(dto);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch users with sessions", e);
        }

        return users;
    }

    @Override
    public void createUser(User user) {
        String sql = "INSERT INTO users (role_id, name, email, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getRoleId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user: " + user.getName(), e);
        }
    }

    @Override
    public void updateUser(User user) {
        String sql = user.getPassword() != null
                ? "UPDATE users SET role_id=?, name=?, email=?, password=?, updated_at=NOW() WHERE id=?"
                : "UPDATE users SET role_id=?, name=?, email=?, updated_at=NOW() WHERE id=?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getRoleId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            if (user.getPassword() != null) {
                stmt.setString(4, user.getPassword());
                stmt.setInt(5, user.getId());
            } else {
                stmt.setInt(4, user.getId());
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + user.getId(), e);
        }
    }

    @Override
    public void deleteUser(int userId) {
        String sql = "UPDATE users SET deleted_at=NOW() WHERE id=?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user: " + userId, e);
        }
    }

}
