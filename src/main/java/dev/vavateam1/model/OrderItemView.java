package dev.vavateam1.model;

import java.math.BigDecimal;

public class OrderItemView {
    private final OrderItem orderItem;
    private final MenuItem menuItem;

    public OrderItemView(OrderItem orderItem, MenuItem menuItem) {
        this.orderItem = orderItem;
        this.menuItem = menuItem;
    }

    public Integer getMenuItemId() {
        return menuItem.getId();
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

    public void setQuantity(Integer newQuantity) {
        orderItem.setQuantity(newQuantity);
    }

    public void setNote(String newNote) {
        orderItem.setNote(newNote);
    }

    public boolean isSameItem(MenuItem menuItem, String note) {
        return this.getMenuItemId() == menuItem.getId() && (this.getNote() == null && note == null);
    }
}
