package dev.vavateam1.dto;

import java.math.BigDecimal;

import dev.vavateam1.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrder {
    private int menuItemId;
    private Integer paymentId;
    private int waiterId;
    private int tableId;
    private int quantity;
    private BigDecimal discount;
    private BigDecimal price;
    private String note;
    private OrderStatus status;
}
