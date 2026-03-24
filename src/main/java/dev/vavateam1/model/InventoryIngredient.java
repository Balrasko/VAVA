package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIngredient {
    private int id;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal costPerUnit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}