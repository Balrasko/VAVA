package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import dev.vavateam1.model.Table;

public interface TableService {
    List<Table> getTables();
    void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY);
}
