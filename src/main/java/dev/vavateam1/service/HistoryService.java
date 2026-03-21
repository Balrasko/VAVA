package dev.vavateam1.service;

import java.util.List;

import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Payment;
import dev.vavateam1.model.PaymentSummary;

public interface HistoryService {
    List<Payment> getPayments();

    List<OrderItem> getOrderItemsForPayment(Payment payment);

    PaymentSummary getPaymentSummary(int paymentId);

    Payment refund(Payment payment);
}
