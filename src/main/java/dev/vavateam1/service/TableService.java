package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import dev.vavateam1.model.Location;
import dev.vavateam1.model.Table;

public interface TableService {
    List<Table> getTables();

    Set<Integer> getTablesWithUnpaidItems();

    List<Location> getLocations();

    void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY);

    void updateTableDetails(int tableId, int locationId, int tableNumber, boolean availability);

    void softDeleteTable(int tableId);

    Table createTable(int locationId);

    Location createZone(String name);

    void updateZoneName(int zoneId, String name);

    void softDeleteZone(int zoneId);
}
