package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import dev.vavateam1.model.Table;

public interface TableService {
    List<Table> getTables();
    Optional<Table> getTableById(int id);
    Table createTable(int locationId, int tableNumber, BigDecimal posX, BigDecimal posY, boolean availability);
    Table updateTable(Table table);
    boolean deleteTable(int id);
    boolean updateTableAvailability(int id, boolean availability);
}
