package dev.vavateam1.service;

import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Payment;

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
    public Payment refund(Payment payment) {
        return paymentDao.setRefunded(payment);
    }
}
