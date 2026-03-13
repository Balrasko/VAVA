package dev.vavateam1.service;

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
}
