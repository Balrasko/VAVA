package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Table {

    private Integer id;
    private Integer locationId;
    private Integer tableNumber;
    private BigDecimal posX;
    private BigDecimal posY;
    private Boolean availability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Table() {}

    public Table(Integer id, Integer locationId, Integer tableNumber, BigDecimal posX, BigDecimal posY, Boolean availability, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.locationId = locationId;
        this.tableNumber = tableNumber;
        this.posX = posX;
        this.posY = posY;
        this.availability = availability;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public BigDecimal getPosX() { return posX; }
    public void setPosX(BigDecimal posX) { this.posX = posX; }

    public BigDecimal getPosY() { return posY; }
    public void setPosY(BigDecimal posY) { this.posY = posY; }

    public Boolean getAvailability() { return availability; }
    public void setAvailability(Boolean availability) { this.availability = availability; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table table)) return false;
        return Objects.equals(id, table.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}