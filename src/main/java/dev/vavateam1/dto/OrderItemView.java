package dev.vavateam1.dto;

import java.math.BigDecimal;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;

public class OrderItemView {
    // Combines MenuItem and OrderItem since OrderItem is missing some useful values which MenuItem has - e.g. the item name

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
