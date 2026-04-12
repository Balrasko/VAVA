package dev.vavateam1.dao;

import java.util.List;

import dev.vavateam1.dto.CreateOrder;
import dev.vavateam1.model.OrderItem;

public interface OrderItemDao {
    public List<OrderItem> findByPayment(int paymentId);

    public List<OrderItem> getUnpaidOrderItems();

    public List<OrderItem> getOrderItemsByTableId(int tableId);

    public boolean hasActiveKitchenItemsByTableId(int tableId);

    public OrderItem createOrderItem(CreateOrder orderCreateDto);

    public void updateOrderItem(OrderItem orderItem);

    public void deleteOrderItem(int orderItemId);
}
