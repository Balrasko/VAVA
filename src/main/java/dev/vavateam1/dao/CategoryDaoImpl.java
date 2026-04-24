package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Category;
//import dev.vavateam1.util.SqlUtils;

public class CategoryDaoImpl implements CategoryDao {
    private static final Logger log = LoggerFactory.getLogger(CategoryDaoImpl.class);

    private final ConnectionFactory connectionFactory;

    @Inject
    public CategoryDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Category> getAllCategories() {
        log.info("Fetching all categories");
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

            log.info("Fetched {} categories", categories.size());
            return categories;
        } catch (SQLException e) {
            log.error("Failed to fetch categories", e);
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }

    @Override
    public Category createCategory(String name) {
        log.info("Creating category: {}", name);
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
                log.info("Category created with id: {}", category.getId());
                return category;
            }
            throw new RuntimeException("Failed to create category");
        } catch (SQLException e) {
            log.error("Failed to create category: {}", name, e);
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @Override
    public void updateCategory(int categoryId, String name) {
        log.info("Updating category id: {} to name: {}", categoryId, name);
        String sql = "UPDATE categories SET name = ?, updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();
            log.info("Category updated id: {}", categoryId);
        } catch (SQLException e) {
            log.error("Failed to update category id: {}", categoryId, e);
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @Override
    public void softDeleteCategory(int categoryId) {
        log.info("Soft deleting category id: {}", categoryId);
        String sql = "UPDATE categories SET deleted_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
            log.info("Category soft deleted id: {}", categoryId);
        } catch (SQLException e) {
            log.error("Failed to soft delete category id: {}", categoryId, e);
            throw new RuntimeException("Failed to soft delete category", e);
        }
    }
}
