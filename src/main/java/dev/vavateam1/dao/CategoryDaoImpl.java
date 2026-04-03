package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Category;
import dev.vavateam1.util.SqlUtils;

public class CategoryDaoImpl implements CategoryDao {

    private final ConnectionFactory connectionFactory;

    @Inject
    public CategoryDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                category.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
                categories.add(category);
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }
}
