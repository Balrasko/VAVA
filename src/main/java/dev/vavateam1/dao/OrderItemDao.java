package dev.vavateam1.dao;

import java.util.List;

import dev.vavateam1.model.OrderItem;

public interface OrderItemDao {
    public List<OrderItem> findByPayment(int paymentId);
}
