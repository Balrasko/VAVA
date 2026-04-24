package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.dao.LocationDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.TableDao;
import dev.vavateam1.model.Location;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;

public class TableServiceImpl implements TableService {
    private static final Logger log = LoggerFactory.getLogger(TableServiceImpl.class);

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
        log.info("Creating table in location id: {}", locationId);
        Table table = tableDao.createTable(locationId);
        log.info("Table created with id: {}", table.getId());
        return table;
    }

    @Override
    public Location createZone(String name) {
        log.info("Creating zone: {}", name);
        Location zone = locationDao.createLocation(name);
        log.info("Zone created with id: {}", zone.getId());
        return zone;
    }

    @Override
    public void updateZoneName(int zoneId, String name) {
        log.info("Updating zone id: {} to name: {}", zoneId, name);
        locationDao.updateLocationName(zoneId, name);
        log.info("Zone name updated id: {}", zoneId);
    }

    @Override
    public void softDeleteZone(int zoneId) {
        log.info("Soft deleting zone id: {}", zoneId);
        locationDao.softDeleteLocation(zoneId);
        log.info("Zone soft deleted id: {}", zoneId);
    }

    @Override
    public void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY) {
        log.info("Updating position for table id: {} to ({}, {})", tableId, posX, posY);
        tableDao.updatePosition(tableId, posX, posY);
    }

    @Override
    public void updateTableDetails(int tableId, int locationId, int tableNumber, boolean availability) {
        log.info("Updating details for table id: {}", tableId);
        tableDao.updateTableDetails(tableId, locationId, tableNumber, availability);
        log.info("Table details updated id: {}", tableId);
    }

    @Override
    public void softDeleteTable(int tableId) {
        log.info("Soft deleting table id: {}", tableId);
        tableDao.softDeleteTable(tableId);
        log.info("Table soft deleted id: {}", tableId);
    }
}
