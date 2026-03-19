package dev.vavateam1.dao;

import java.util.List;

import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Payment;

public interface OrderItemDao {
    public List<OrderItem> findByPayment(Payment payment);
}
