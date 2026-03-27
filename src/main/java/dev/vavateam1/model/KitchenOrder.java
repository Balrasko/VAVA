package dev.vavateam1.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrder {
    private int id;
    private int tableNumber;
    private List<KitchenOrderItem> items = new ArrayList<>();
    private OrderStatus status;
}
