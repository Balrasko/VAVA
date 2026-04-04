package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.util.List;
import dev.vavateam1.model.Table;

public interface TableDao {
    public List<Table> findAll();

    void updatePosition(int tableId, BigDecimal posX, BigDecimal posY);

    void updateTableDetails(int tableId, int locationId, int tableNumber, boolean availability);

    void softDeleteTable(int tableId);

    Table createTable(int locationId);
}
