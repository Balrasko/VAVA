package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.util.List;
import dev.vavateam1.model.Table;

public interface TableDao {
    public List<Table> findAll();

    void updatePosition(int tableId, BigDecimal posX, BigDecimal posY);

    Table createTable(int locationId);
}
