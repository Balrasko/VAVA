package dev.vavateam1.model;

import java.math.BigDecimal;

public class OrderItemView {
    private final OrderItem orderItem;
    private final MenuItem menuItem;

    public OrderItemView(OrderItem orderItem, MenuItem menuItem) {
        this.orderItem = orderItem;
        this.menuItem = menuItem;
    }

    public OrderItemView copy() {
        return new OrderItemView(new OrderItem(this.orderItem), this.menuItem);
    }

    public Integer getMenuItemId() {
        return menuItem.getId();
    }

    public Integer getOrderItemId() {
        return orderItem.getId();
    }

    public String getName() {
        return menuItem.getName();
    }

    public Integer getQuantity() {
        return orderItem.getQuantity();
    }

    public String getNote() {
        return orderItem.getNote();
    }

    public BigDecimal getDiscount() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getPrice() {
        return orderItem.getPrice();
    }

    public OrderItem getOrderItem() {
        return this.orderItem;
    }

    public MenuItem getMenuItem() {
        return this.menuItem;
    }

    public void setOrderIdToNull() {
        orderItem.setId(null);
    }

    public void setQuantity(Integer newQuantity) {
        orderItem.setQuantity(newQuantity);
    }

    public void setNote(String newNote) {
        orderItem.setNote(newNote);
    }

    public boolean isSameItem(MenuItem menuItem, String note) {
        return this.getMenuItemId().equals(menuItem.getId()) && (this.getNote() == null && note == null);
    }
}
