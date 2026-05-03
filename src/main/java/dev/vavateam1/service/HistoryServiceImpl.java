package dev.vavateam1.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.dto.PaymentDto;
import dev.vavateam1.dto.PaymentSummary;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.util.I18n;

public class HistoryServiceImpl implements HistoryService {
    private static final Logger log = LoggerFactory.getLogger(HistoryServiceImpl.class);
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");

    private final PaymentDao paymentDao;
    private final OrderItemDao orderItemDao;
    private final OrderService orderService;

    @Inject
    public HistoryServiceImpl(PaymentDao paymentDao, OrderItemDao orderItemDao, OrderService orderService) {
        this.paymentDao = paymentDao;
        this.orderItemDao = orderItemDao;
        this.orderService = orderService;
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
        return orderService.buildOrderItemViews(orderItems, orderService.getMenuItemsIncludingDeleted());
    }

    @Override
    public String buildReceiptText(PaymentDto payment) {
        List<OrderItemDto> orderItems = getOrderItemsByPaymentId(payment.getId());
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("history.receiptTitle")).append("\n");
        sb.append(I18n.t("history.order", String.valueOf(payment.getId()))).append("\n");
        sb.append(I18n.t("history.date", payment.getCreatedAt().format(DISPLAY_FORMAT))).append("\n");
        sb.append(I18n.t("history.waiter", String.valueOf(payment.getWaiterId()))).append("\n");

        String methodText = payment.getPaymentMethodName() != null
                ? localizePaymentMethod(payment.getPaymentMethodName()) : "-";
        sb.append(I18n.t("history.payment", methodText)).append("\n");

        String tipText = payment.getTip() != null
                ? payment.getTip().stripTrailingZeros().toPlainString() + "%" : "-";
        sb.append(I18n.t("history.tip", tipText)).append("\n\n");
        sb.append(I18n.t("history.items")).append(":\n");

        for (OrderItemDto item : orderItems) {
            String priceText = item.getPrice() != null
                    ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            .stripTrailingZeros().toPlainString() + "€" : "-";
            sb.append("  ").append(item.getName()).append(" x").append(item.getQuantity())
                    .append(" - ").append(priceText).append("\n");
        }

        sb.append("\n").append(I18n.t("history.total", payment.getAmount().toPlainString())).append("\n");

        if (Boolean.TRUE.equals(payment.getRefunded())) {
            sb.append(I18n.t("history.refunded")).append("\n");
        }

        return sb.toString();
    }

    private String localizePaymentMethod(String methodName) {
        return switch (methodName.toLowerCase()) {
            case "cash" -> I18n.t("common.cash");
            case "card" -> I18n.t("common.card");
            case "meal card" -> I18n.t("payment.mealCard");
            default -> methodName;
        };
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
