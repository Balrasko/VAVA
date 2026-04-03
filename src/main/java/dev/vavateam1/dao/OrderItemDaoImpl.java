package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.dto.CreateOrder;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.util.SqlUtils;

public class OrderItemDaoImpl implements OrderItemDao {

    private final ConnectionFactory connectionFactory;

    @Inject
    public OrderItemDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<OrderItem> findByPayment(int paymentId) {
        String sql = """
                SELECT oi.id, oi.menu_item_id, mi.name AS menu_item_name, oi.payment_id, oi.waiter_id,
                       oi.table_id, t.table_number, oi.quantity, oi.discount, oi.price, oi.note,
                       oi.status, oi.created_at, oi.updated_at
                FROM order_items oi
                JOIN menu_items mi ON oi.menu_item_id = mi.id
                JOIN tables t ON oi.table_id = t.id
                WHERE oi.payment_id = ?
                ORDER BY oi.created_at ASC, oi.id ASC
                """;

        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(rs.getInt("id"));
                orderItem.setMenuItemId(rs.getInt("menu_item_id"));
                orderItem.setPaymentId(rs.getObject("payment_id", Integer.class));
                orderItem.setWaiterId(rs.getInt("waiter_id"));
                orderItem.setTableId(rs.getInt("table_id"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setDiscount(rs.getBigDecimal("discount"));
                orderItem.setPrice(rs.getBigDecimal("price"));
                orderItem.setNote(rs.getString("note"));
                orderItem.setStatus(rs.getString("status"));
                orderItem.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                orderItem.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
                orderItems.add(orderItem);
            }

            return orderItems;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch order items for payment " + paymentId, e);
        }
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(rs.getInt("id"));
        orderItem.setMenuItemId(rs.getInt("menu_item_id"));
        orderItem.setPaymentId(rs.getObject("payment_id", Integer.class));
        orderItem.setWaiterId(rs.getInt("waiter_id"));
        orderItem.setTableId(rs.getInt("table_id"));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setDiscount(rs.getBigDecimal("discount"));
        orderItem.setPrice(rs.getBigDecimal("price"));
        orderItem.setNote(rs.getString("note"));
        orderItem.setStatus(rs.getString("status"));
        orderItem.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        orderItem.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
        return orderItem;
    }

    @Override
    public List<OrderItem> getUnpaidOrderItems() {
        String sql = """
                SELECT id, menu_item_id, payment_id, waiter_id, table_id, quantity, discount, price, note,
                       status, created_at, updated_at
                FROM order_items
                WHERE payment_id IS NULL
                ORDER BY created_at ASC, id ASC
                """;

        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orderItems.add(mapResultSetToOrderItem(rs));
            }

            return orderItems;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch unpaid order items", e);
        }
    }

    @Override
    public List<OrderItem> getOrderItemsByTableId(int tableId) {
        String sql = """
                SELECT oi.id, oi.menu_item_id, mi.name AS menu_item_name, oi.payment_id, oi.waiter_id,
                       oi.table_id, t.table_number, oi.quantity, oi.discount, oi.price, oi.note,
                       oi.status, oi.created_at, oi.updated_at
                FROM order_items oi
                JOIN menu_items mi ON oi.menu_item_id = mi.id
                JOIN tables t ON oi.table_id = t.id
                WHERE oi.table_id = ? AND oi.payment_id IS NULL
                ORDER BY oi.created_at ASC, oi.id ASC
                """;

        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orderItems.add(mapResultSetToOrderItem(rs));
            }

            return orderItems;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch order items for table " + tableId, e);
        }
    }

    @Override
    public OrderItem createOrderItem(CreateOrder orderCreateDto) {
        String sql = """
                INSERT INTO order_items (menu_item_id, payment_id, waiter_id, table_id, quantity, discount, price, note, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, orderCreateDto.getMenuItemId());
            if (orderCreateDto.getPaymentId() != null) {
                stmt.setInt(2, orderCreateDto.getPaymentId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setInt(3, orderCreateDto.getWaiterId());
            stmt.setInt(4, orderCreateDto.getTableId());
            stmt.setInt(5, orderCreateDto.getQuantity());
            stmt.setBigDecimal(6, orderCreateDto.getDiscount());
            stmt.setBigDecimal(7, orderCreateDto.getPrice());
            stmt.setString(8, orderCreateDto.getNote());
            stmt.setString(9, orderCreateDto.getStatus());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return findOrderItemById(id, conn);
                }
            }

            throw new RuntimeException("Failed to create order item: no generated key returned.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order item", e);
        }
    }

    private OrderItem findOrderItemById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderItem(rs);
                }
            }
        }

        throw new RuntimeException("Failed to load created order item " + id);
    }

    @Override
    public void updateOrderItem(OrderItem orderItem) {
        String sql = """
                UPDATE order_items
                SET menu_item_id = ?, payment_id = ?, waiter_id = ?, table_id = ?, quantity = ?, discount = ?, price = ?, note = ?, status = ?, updated_at = NOW()
                WHERE id = ?
                """;
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderItem.getMenuItemId());
            if (orderItem.getPaymentId() != null) {
                stmt.setInt(2, orderItem.getPaymentId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setInt(3, orderItem.getWaiterId());
            stmt.setInt(4, orderItem.getTableId());
            stmt.setInt(5, orderItem.getQuantity());
            stmt.setBigDecimal(6, orderItem.getDiscount());
            stmt.setBigDecimal(7, orderItem.getPrice());
            stmt.setString(8, orderItem.getNote());
            stmt.setString(9, orderItem.getStatus());
            stmt.setInt(10, orderItem.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order item " + orderItem.getId(), e);
        }
    }

    @Override
    public void deleteOrderItem(int orderItemId) {
        String sql = "DELETE FROM order_items WHERE id = ? AND payment_id IS NULL";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderItemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete order item " + orderItemId, e);
        }
    }
}
