package dev.vavateam1.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dev.vavateam1.model.Table;

public class MockTableService implements TableService {
    public List<Table> getTables() {
        List<Table> tables = new ArrayList<>();

        // Mock table attributes made by AI

        tables.add(new Table(
            1,
            1,
            1,
            new BigDecimal("120.0"),
            new BigDecimal("80.0"),
            true,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now()
        ));

        tables.add(new Table(
                2,
                1,
                2,
                new BigDecimal("320.0"),
                new BigDecimal("80.0"),
                false,
                LocalDateTime.now().minusDays(9),
                LocalDateTime.now()
        ));

        tables.add(new Table(
                3,
                1,
                3,
                new BigDecimal("120.0"),
                new BigDecimal("220.0"),
                true,
                LocalDateTime.now().minusDays(8),
                LocalDateTime.now()
        ));

        tables.add(new Table(
                4,
                1,
                4,
                new BigDecimal("420.0"),
                new BigDecimal("220.0"),
                true,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        ));

        return tables;
    }

    @Override
    public void updateTablePosition(int tableId, BigDecimal posX, BigDecimal posY) {
    }
}
