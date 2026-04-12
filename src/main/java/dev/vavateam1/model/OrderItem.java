package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Integer id;
    private int menuItemId;
    private Integer paymentId;
    private int waiterId;
    private int tableId;
    private int quantity;
    private BigDecimal discount;
    private BigDecimal price;
    private String note;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;


    public OrderItem(OrderItem other) {
        this.id = other.id;
        this.menuItemId = other.menuItemId;
        this.paymentId = other.paymentId;
        this.waiterId = other.waiterId;
        this.tableId = other.tableId;
        this.quantity = other.quantity;
        this.discount = other.discount;
        this.price = other.price;
        this.note = other.note;
        this.status = other.status;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.deletedAt = other.deletedAt;
    }
}
