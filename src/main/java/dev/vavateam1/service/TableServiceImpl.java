package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.TableDao;
import dev.vavateam1.model.Table;

public class TableServiceImpl implements TableService {
    private TableDao tableDao;

    @Inject
    public TableServiceImpl(TableDao tableDao) {
        this.tableDao = tableDao;
    }

    public List<Table> getTables() {
        return tableDao.findAll();
    }

    @Override
    public void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY) {
        tableDao.updatePosition(tableId, posX, posY);
    }
}
