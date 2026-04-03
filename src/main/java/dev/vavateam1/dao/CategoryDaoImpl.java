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
        String sql = "SELECT * FROM categories WHERE is_deleted = FALSE ORDER BY id ASC";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDeleted(rs.getBoolean("is_deleted"));
                category.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                category.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
                categories.add(category);
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }

    @Override
    public Category createCategory(String name) {
        String sql = "INSERT INTO categories (name, is_deleted) VALUES (?, FALSE) RETURNING id, name, is_deleted, created_at, updated_at";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDeleted(rs.getBoolean("is_deleted"));
                category.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                category.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
                return category;
            }
            throw new RuntimeException("Failed to create category");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @Override
    public void updateCategory(int categoryId, String name) {
        String sql = "UPDATE categories SET name = ?, updated_at = NOW() WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @Override
    public void softDeleteCategory(int categoryId) {
        String sql = "UPDATE categories SET is_deleted = TRUE, updated_at = NOW() WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft delete category", e);
        }
    }
}
