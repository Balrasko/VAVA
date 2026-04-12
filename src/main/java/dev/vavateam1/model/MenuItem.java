package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    private int id;
    private int categoryId;
    private int itemCode;
    private String name;
    private BigDecimal price;
    private boolean availability;
    private String description;
    private boolean toKitchen;
    private BigDecimal discount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}