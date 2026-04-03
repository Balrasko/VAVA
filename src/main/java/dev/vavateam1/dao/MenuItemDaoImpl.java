package dev.vavateam1.dao;

import com.google.inject.Inject;
import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.util.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        item.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        item.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
        return item;
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        String sql = "SELECT * FROM menu_items";
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
        String sql = "SELECT * FROM menu_items WHERE category_id = ?";
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
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
}