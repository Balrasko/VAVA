package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Payment;
import dev.vavateam1.util.SqlUtils;

public class OrderItemDaoImpl implements OrderItemDao {

    private final ConnectionFactory connectionFactory;

    @Inject
    public OrderItemDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<OrderItem> findByPayment(Payment payment) {
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

            stmt.setInt(1, payment.getId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(rs.getInt("id"));
                orderItem.setMenuItemId(rs.getInt("menu_item_id"));
                orderItem.setMenuItemName(rs.getString("menu_item_name"));
                orderItem.setPaymentId(rs.getInt("payment_id"));
                orderItem.setWaiterId(rs.getInt("waiter_id"));
                orderItem.setTableId(rs.getInt("table_id"));
                orderItem.setTableNumber(rs.getInt("table_number"));
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
            throw new RuntimeException("Failed to fetch order items for payment " + payment.getId(), e);
        }
    }
}
