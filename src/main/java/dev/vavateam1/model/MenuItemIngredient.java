package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class MenuItemIngredient {

    private Integer id;
    private Integer ingredientId;
    private Integer menuItemId;
    private BigDecimal quantityNeeded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MenuItemIngredient() {}

    public MenuItemIngredient(Integer id, Integer ingredientId, Integer menuItemId, BigDecimal quantityNeeded, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.menuItemId = menuItemId;
        this.quantityNeeded = quantityNeeded;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIngredientId() { return ingredientId; }
    public void setIngredientId(Integer ingredientId) { this.ingredientId = ingredientId; }

    public Integer getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Integer menuItemId) { this.menuItemId = menuItemId; }

    public BigDecimal getQuantityNeeded() { return quantityNeeded; }
    public void setQuantityNeeded(BigDecimal quantityNeeded) { this.quantityNeeded = quantityNeeded; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItemIngredient that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}