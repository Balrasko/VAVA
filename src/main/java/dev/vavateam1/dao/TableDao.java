package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import dev.vavateam1.model.Table;

public interface TableDao {
    List<Table> findAll();
    Optional<Table> findById(int id);
    Table create(int locationId, int tableNumber, BigDecimal posX, BigDecimal posY, boolean availability);
    Table update(Table table);
    boolean delete(int id);
    boolean updateAvailability(int id, boolean availability);
}
