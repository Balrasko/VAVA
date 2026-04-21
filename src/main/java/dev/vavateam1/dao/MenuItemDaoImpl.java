package dev.vavateam1.dao;

import com.google.inject.Inject;
import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.MenuItem;
//import dev.vavateam1.util.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDaoImpl implements MenuItemDao {

    private final ConnectionFactory connectionFactory;

    @Inject
    public MenuItemDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getInt("id"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setItemCode(rs.getInt("item_code"));
        item.setName(rs.getString("name"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setAvailability(rs.getBoolean("availability"));
        item.setDescription(rs.getString("description"));
        item.setToKitchen(rs.getBoolean("to_kitchen"));
        item.setDiscount(rs.getBigDecimal("discount"));
        item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        item.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
        return item;
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        String sql = "SELECT * FROM menu_items WHERE deleted_at IS NULL";
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToMenuItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all menu items", e);
        }
        return items;
    }

    @Override
    public List<MenuItem> getMenuItemsByCategoryId(int categoryId) {
        String sql = "SELECT * FROM menu_items WHERE category_id = ? AND deleted_at IS NULL";
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToMenuItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get menu items by category id", e);
        }
        return items;
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        String sql = "INSERT INTO menu_items (category_id, item_code, name, price, availability, description, to_kitchen, discount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuItem.getCategoryId());
            stmt.setInt(2, menuItem.getItemCode());
            stmt.setString(3, menuItem.getName());
            stmt.setBigDecimal(4, menuItem.getPrice());
            stmt.setBoolean(5, menuItem.isAvailability());
            stmt.setString(6, menuItem.getDescription());
            stmt.setBoolean(7, menuItem.isToKitchen());
            stmt.setBigDecimal(8, menuItem.getDiscount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add menu item", e);
        }
    }

    @Override
    public void updateMenuItem(MenuItem menuItem) {
        String sql = "UPDATE menu_items SET category_id = ?, name = ?, price = ?, availability = ?, description = ?, to_kitchen = ?, discount = ?, updated_at = NOW() "
                + "WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuItem.getCategoryId());
            stmt.setString(2, menuItem.getName());
            stmt.setBigDecimal(3, menuItem.getPrice());
            stmt.setBoolean(4, menuItem.isAvailability());
            stmt.setString(5, menuItem.getDescription());
            stmt.setBoolean(6, menuItem.isToKitchen());
            stmt.setBigDecimal(7, menuItem.getDiscount());
            stmt.setInt(8, menuItem.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update menu item", e);
        }
    }

    @Override
    public void softDeleteMenuItem(int menuItemId) {
        String sql = "UPDATE menu_items SET deleted_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuItemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft delete menu item", e);
        }
    }

    @Override
    public MenuItem getItemByPluCode(int pluCode) {
        String sql = "SELECT * FROM menu_items WHERE item_code = ? AND deleted_at IS NULL";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pluCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMenuItem(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch menu item by PLU code", e);
        }
        return null;
    }
}