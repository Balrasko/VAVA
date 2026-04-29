package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(HistoryServiceImpl.class);

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
        log.info("Fetching all payments");
        List<PaymentDto> payments = paymentDao.findAll();
        log.info("Fetched {} payments", payments.size());
        return payments;
    }

    @Override
    public PaymentSummary getPaymentSummary(int paymentId) {
        log.info("Fetching payment summary for payment id: {}", paymentId);
        PaymentDto payment = paymentDao.findById(paymentId);
        if (payment == null) {
            log.info("Payment not found for id: {}", paymentId);
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

        for (MenuItem menuItem : menuItemDao.getAllMenuItemsIncludingDeleted()) {
            menuItemsById.put(menuItem.getId(), menuItem);
        }

        return orderItems.stream()
                .map(orderItem -> new OrderItemDto(orderItem, menuItemsById.getOrDefault(
                        orderItem.getMenuItemId(), createDeletedMenuItemFallback(orderItem))))
                .toList();
    }

    private MenuItem createDeletedMenuItemFallback(OrderItem orderItem) {
        MenuItem fallback = new MenuItem();
        fallback.setId(orderItem.getMenuItemId());
        fallback.setName("Deleted item #" + orderItem.getMenuItemId());
        fallback.setPrice(orderItem.getPrice());
        fallback.setAvailability(false);
        fallback.setToKitchen(false);
        fallback.setDiscount(BigDecimal.ZERO);
        return fallback;
    }

    @Override
    public void refund(int paymentId) {
        log.info("Refunding payment id: {}", paymentId);
        PaymentDto existingPayment = paymentDao.findById(paymentId);
        if (existingPayment == null) {
            log.error("Refund failed: payment id {} does not exist", paymentId);
            throw new IllegalArgumentException("Payment " + paymentId + " does not exist");
        }

        paymentDao.setRefunded(existingPayment);
        log.info("Payment refunded id: {}", paymentId);
    }

}
