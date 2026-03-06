package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import com.google.inject.Inject;
import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.User;
import dev.vavateam1.util.SqlUtils;

public class UserDaoImpl implements UserDao {
    private final ConnectionFactory connectionFactory;

    @Inject
    public UserDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, role_id, name, email, password, status, created_at, updated_at FROM users WHERE email = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

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
            user.setCreatedAt(SqlUtils.toLocalDateTime(resultSet.getTimestamp("created_at")));
            user.setUpdatedAt(SqlUtils.toLocalDateTime(resultSet.getTimestamp("updated_at")));

            return Optional.of(user);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email: " + email, e);
        }
    }

}
