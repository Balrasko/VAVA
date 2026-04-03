package dev.vavateam1.dto;

import java.util.ArrayList;
import java.util.List;

import dev.vavateam1.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrder {
    private int id;
    private int tableNumber;
    private List<OrderItemDto> items = new ArrayList<>();
    private OrderStatus status;
}