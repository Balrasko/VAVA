package dev.vavateam1.dto;

import java.math.BigDecimal;
import java.util.List;

import dev.vavateam1.model.OrderItem;

public class PaymentSummary {

    private PaymentDto payment;
    private List<OrderItem> orderItems;
    private BigDecimal orderItemsTotal;
    private Integer totalQuantity;

    public PaymentSummary() {
    }

    public PaymentSummary(PaymentDto payment, List<OrderItem> orderItems, BigDecimal orderItemsTotal,
            Integer totalQuantity) {
        this.payment = payment;
        this.orderItems = orderItems;
        this.orderItemsTotal = orderItemsTotal;
        this.totalQuantity = totalQuantity;
    }

    public PaymentDto getPayment() {
        return payment;
    }

    public void setPayment(PaymentDto payment) {
        this.payment = payment;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public BigDecimal getOrderItemsTotal() {
        return orderItemsTotal;
    }

    public void setOrderItemsTotal(BigDecimal orderItemsTotal) {
        this.orderItemsTotal = orderItemsTotal;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
