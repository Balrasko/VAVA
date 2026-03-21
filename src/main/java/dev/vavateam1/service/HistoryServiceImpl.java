package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Payment;
import dev.vavateam1.model.PaymentSummary;

public class HistoryServiceImpl implements HistoryService {

    private final PaymentDao paymentDao;
    private final OrderItemDao orderItemDao;

    @Inject
    public HistoryServiceImpl(PaymentDao paymentDao, OrderItemDao orderItemDao) {
        this.paymentDao = paymentDao;
        this.orderItemDao = orderItemDao;
    }

    @Override
    public List<Payment> getPayments() {
        return paymentDao.findAll();
    }

    @Override
    public List<OrderItem> getOrderItemsForPayment(Payment payment) {
        return orderItemDao.findByPayment(payment);
    }

    @Override
    public PaymentSummary getPaymentSummary(int paymentId) {
        Payment payment = paymentDao.findById(paymentId);
        if (payment == null) {
            return null;
        }

        List<OrderItem> orderItems = orderItemDao.findByPayment(payment);
        BigDecimal orderItemsTotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getPrice() != null) {
                orderItemsTotal = orderItemsTotal.add(orderItem.getPrice());
            }
            if (orderItem.getQuantity() != null) {
                totalQuantity += orderItem.getQuantity();
            }
        }

        return new PaymentSummary(payment, orderItems, orderItemsTotal, totalQuantity);
    }

    @Override
    public Payment refund(Payment payment) {
        if (payment == null || payment.getId() == null) {
            throw new IllegalArgumentException("Payment must have an id before it can be refunded");
        }

        Payment existingPayment = paymentDao.findById(payment.getId());
        if (existingPayment == null) {
            throw new IllegalArgumentException("Payment " + payment.getId() + " does not exist");
        }
        if (Boolean.TRUE.equals(existingPayment.getRefunded())) {
            throw new IllegalStateException("Payment " + payment.getId() + " is already refunded");
        }

        return paymentDao.setRefunded(existingPayment);
    }
}
