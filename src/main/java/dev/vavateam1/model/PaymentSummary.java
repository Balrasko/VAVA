package dev.vavateam1.model;

import java.math.BigDecimal;
import java.util.List;

public class PaymentSummary {

    private Payment payment;
    private List<OrderItem> orderItems;
    private BigDecimal orderItemsTotal;
    private Integer totalQuantity;

    public PaymentSummary() {
    }

    public PaymentSummary(Payment payment, List<OrderItem> orderItems, BigDecimal orderItemsTotal, Integer totalQuantity) {
        this.payment = payment;
        this.orderItems = orderItems;
        this.orderItemsTotal = orderItemsTotal;
        this.totalQuantity = totalQuantity;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
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
