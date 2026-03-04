package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class InventoryIngredient {

    private Integer id;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal costPerUnit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InventoryIngredient() {}

    public InventoryIngredient(Integer id, String name, BigDecimal quantity, String unit, BigDecimal costPerUnit, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.costPerUnit = costPerUnit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getCostPerUnit() { return costPerUnit; }
    public void setCostPerUnit(BigDecimal costPerUnit) { this.costPerUnit = costPerUnit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryIngredient that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}