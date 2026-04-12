package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Category;
//import dev.vavateam1.util.SqlUtils;

public class CategoryDaoImpl implements CategoryDao {

    private final ConnectionFactory connectionFactory;

    @Inject
    public CategoryDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories WHERE deleted_at IS NULL ORDER BY id ASC";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                category.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                category.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
                categories.add(category);
            }

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }

    @Override
    public Category createCategory(String name) {
        String sql = "INSERT INTO categories (name) VALUES (?) RETURNING id, name, created_at, updated_at, deleted_at";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                category.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                category.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
                return category;
            }
            throw new RuntimeException("Failed to create category");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @Override
    public void updateCategory(int categoryId, String name) {
        String sql = "UPDATE categories SET name = ?, updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
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
        String sql = "UPDATE categories SET deleted_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft delete category", e);
        }
    }
}
