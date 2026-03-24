package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemIngredient {
    private int id;
    private int ingredientId;
    private int menuItemId;
    private BigDecimal quantityNeeded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}