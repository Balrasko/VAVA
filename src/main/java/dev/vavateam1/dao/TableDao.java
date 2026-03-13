package dev.vavateam1.dao;

import java.util.List;
import dev.vavateam1.model.Table;

public interface TableDao {
    public List<Table> findAll();
}
