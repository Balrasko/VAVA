package dev.vavateam1.dao;

import java.util.List;

import dev.vavateam1.model.OrderItem;

public interface OrderItemDao {
    public List<OrderItem> findByPayment(int paymentId);

    public List<OrderItem> getOrderItemsByTableId(int tableId);

    public void addOrderItem(OrderItem orderItem);

    public void updateOrderItem(OrderItem orderItem);
}
