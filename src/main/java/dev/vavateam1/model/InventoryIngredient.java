package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIngredient {
    private int id;
    private String name;
    private BigDecimal quantity;
    private BigDecimal minimalQuantity;
    private String unit;
    private BigDecimal costPerUnit;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}
