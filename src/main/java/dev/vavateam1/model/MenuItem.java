package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class MenuItem {

    private Integer id;
    private Integer categoryId;
    private Integer itemCode;
    private String name;
    private BigDecimal price;
    private Boolean availability;
    private String description;
    private Boolean toKitchen;
    private BigDecimal discount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MenuItem() {}

    public MenuItem(Integer id, Integer categoryId, Integer itemCode, String name, BigDecimal price, Boolean availability, String description, Boolean toKitchen, BigDecimal discount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.itemCode = itemCode;
        this.name = name;
        this.price = price;
        this.availability = availability;
        this.description = description;
        this.toKitchen = toKitchen;
        this.discount = discount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getItemCode() { return itemCode; }
    public void setItemCode(Integer itemCode) { this.itemCode = itemCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getAvailability() { return availability; }
    public void setAvailability(Boolean availability) { this.availability = availability; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getToKitchen() { return toKitchen; }
    public void setToKitchen(Boolean toKitchen) { this.toKitchen = toKitchen; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem menuItem)) return false;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}