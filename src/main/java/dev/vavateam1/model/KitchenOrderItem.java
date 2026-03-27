package dev.vavateam1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrderItem {
    private String name;
    private int quantity;
    private String note;
}
