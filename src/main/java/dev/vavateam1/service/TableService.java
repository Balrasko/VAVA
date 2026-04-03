package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import dev.vavateam1.model.Location;
import dev.vavateam1.model.Table;

public interface TableService {
    List<Table> getTables();

    List<Location> getLocations();

    void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY);

    Table createTable(int locationId);

    Location createZone(String name);
}
