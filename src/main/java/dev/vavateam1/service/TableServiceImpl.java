package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import dev.vavateam1.dao.LocationDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.TableDao;
import dev.vavateam1.model.Location;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;

public class TableServiceImpl implements TableService {
    private TableDao tableDao;
    private LocationDao locationDao;
    private OrderItemDao orderItemDao;

    @Inject
    public TableServiceImpl(TableDao tableDao, LocationDao locationDao, OrderItemDao orderItemDao) {
        this.tableDao = tableDao;
        this.locationDao = locationDao;
        this.orderItemDao = orderItemDao;
    }

    public List<Table> getTables() {
        return tableDao.findAll();
    }

    @Override
    public Set<Integer> getTablesWithUnpaidItems() {
        return orderItemDao.getUnpaidOrderItems().stream()
                .map(OrderItem::getTableId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Location> getLocations() {
        return locationDao.findAll();
    }

    @Override
    public Table createTable(int locationId) {
        return tableDao.createTable(locationId);
    }

    @Override
    public Location createZone(String name) {
        return locationDao.createLocation(name);
    }

    @Override
    public void updateZoneName(int zoneId, String name) {
        locationDao.updateLocationName(zoneId, name);
    }

    @Override
    public void softDeleteZone(int zoneId) {
        locationDao.softDeleteLocation(zoneId);
    }

    @Override
    public void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY) {
        tableDao.updatePosition(tableId, posX, posY);
    }

    @Override
    public void updateTableDetails(int tableId, int locationId, int tableNumber, boolean availability) {
        tableDao.updateTableDetails(tableId, locationId, tableNumber, availability);
    }

    @Override
    public void softDeleteTable(int tableId) {
        tableDao.softDeleteTable(tableId);
    }
}
