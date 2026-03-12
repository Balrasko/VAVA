package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;
import dev.vavateam1.dao.TableDao;
import dev.vavateam1.model.Table;

public class LocalTableService implements TableService {

    private final TableDao tableDao;

    @Inject
    public LocalTableService(TableDao tableDao) {
        this.tableDao = tableDao;
    }

    @Override
    public List<Table> getTables() {
        return tableDao.findAll();
    }

    @Override
    public Optional<Table> getTableById(int id) {
        return tableDao.findById(id);
    }

    @Override
    public Table createTable(int locationId, int tableNumber, BigDecimal posX, BigDecimal posY, boolean availability) {
        return tableDao.create(locationId, tableNumber, posX, posY, availability);
    }

    @Override
    public Table updateTable(Table table) {
        return tableDao.update(table);
    }

    @Override
    public boolean deleteTable(int id) {
        return tableDao.delete(id);
    }

    @Override
    public boolean updateTableAvailability(int id, boolean availability) {
        return tableDao.updateAvailability(id, availability);
    }
}
