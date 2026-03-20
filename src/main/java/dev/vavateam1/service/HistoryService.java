package dev.vavateam1.service;

import java.util.List;

import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Payment;

public interface HistoryService {
    List<Payment> getPayments();

    List<OrderItem> getOrderItemsForPayment(Payment payment);

    Payment refund(Payment payment);
}
