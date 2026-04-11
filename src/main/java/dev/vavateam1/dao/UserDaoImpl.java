package dev.vavateam1.dao;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.User;
//import dev.vavateam1.util.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
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
        String sql = "SELECT id, role_id, name, email, password, status, created_at, updated_at "
                + "FROM users WHERE LOWER(email) = LOWER(?) OR LOWER(name) = LOWER(?)";

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

}
