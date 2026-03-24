package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dto.PaymentDto;
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
    public List<PaymentDto> getPayments() {
        return paymentDao.findAll();
    }

    @Override
    public PaymentSummary getPaymentSummary(int paymentId) {
        PaymentDto payment = paymentDao.findById(paymentId);
        if (payment == null) {
            return null;
        }

        List<OrderItem> orderItems = orderItemDao.findByPayment(payment.getId());
        BigDecimal orderItemsTotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItem orderItem : orderItems) {
            if (orderItem.getPrice() != null) {
                orderItemsTotal = orderItemsTotal.add(orderItem.getPrice());
            }
            totalQuantity += orderItem.getQuantity();
        }

        return new PaymentSummary(payment, orderItems, orderItemsTotal, totalQuantity);
    }

    @Override
    public void refund(int paymentId) {
        PaymentDto existingPayment = paymentDao.findById(paymentId);
        if (existingPayment == null) {
            throw new IllegalArgumentException("Payment " + paymentId + " does not exist");
        }

        paymentDao.setRefunded(existingPayment);
    }

}
