package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.LocationDao;
import dev.vavateam1.dao.TableDao;
import dev.vavateam1.model.Location;
import dev.vavateam1.model.Table;

public class TableServiceImpl implements TableService {
    private TableDao tableDao;
    private LocationDao locationDao;

    @Inject
    public TableServiceImpl(TableDao tableDao, LocationDao locationDao) {
        this.tableDao = tableDao;
        this.locationDao = locationDao;
    }

    public List<Table> getTables() {
        return tableDao.findAll();
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
    public void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY) {
        tableDao.updatePosition(tableId, posX, posY);
    }
}
