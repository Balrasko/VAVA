package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.dto.PaymentDto;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.dto.PaymentSummary;

public class HistoryServiceImpl implements HistoryService {

    private final PaymentDao paymentDao;
    private final OrderItemDao orderItemDao;
    private final MenuItemDao menuItemDao;

    @Inject
    public HistoryServiceImpl(PaymentDao paymentDao, OrderItemDao orderItemDao, MenuItemDao menuItemDao) {
        this.paymentDao = paymentDao;
        this.orderItemDao = orderItemDao;
        this.menuItemDao = menuItemDao;
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
    public List<OrderItemDto> getOrderItemsByPaymentId(int paymentId) {
        List<OrderItem> orderItems = orderItemDao.findByPayment(paymentId);
        Map<Integer, MenuItem> menuItemsById = new HashMap<>();

        for (MenuItem menuItem : menuItemDao.getAllMenuItems()) {
            menuItemsById.put(menuItem.getId(), menuItem);
        }

        return orderItems.stream()
                .filter(orderItem -> menuItemsById.containsKey(orderItem.getMenuItemId()))
                .map(orderItem -> new OrderItemDto(orderItem, menuItemsById.get(orderItem.getMenuItemId())))
                .toList();
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
