package dev.vavateam1.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.vavateam1.dao.TableDao;
import dev.vavateam1.model.Table;

@ExtendWith(MockitoExtension.class)
class LocalTableServiceTest {

    @Mock
    private TableDao tableDao;

    private LocalTableService tableService;

    private Table table1;
    private Table table2;

    @BeforeEach
    void setUp() {
        tableService = new LocalTableService(tableDao);

        table1 = new Table(1, 1, 1, new BigDecimal("100.0"), new BigDecimal("80.0"), true,
                LocalDateTime.now().minusDays(5), LocalDateTime.now());
        table2 = new Table(2, 1, 2, new BigDecimal("300.0"), new BigDecimal("80.0"), false,
                LocalDateTime.now().minusDays(3), LocalDateTime.now());
    }

    @Test
    void getTables_returnsList() {
        when(tableDao.findAll()).thenReturn(List.of(table1, table2));

        List<Table> result = tableService.getTables();

        assertEquals(2, result.size());
        assertEquals(table1, result.get(0));
        assertEquals(table2, result.get(1));
        verify(tableDao).findAll();
    }

    @Test
    void getTables_returnsEmptyListWhenNoTables() {
        when(tableDao.findAll()).thenReturn(List.of());

        List<Table> result = tableService.getTables();

        assertTrue(result.isEmpty());
        verify(tableDao).findAll();
    }

    @Test
    void getTableById_returnsTableWhenFound() {
        when(tableDao.findById(1)).thenReturn(Optional.of(table1));

        Optional<Table> result = tableService.getTableById(1);

        assertTrue(result.isPresent());
        assertEquals(table1, result.get());
        verify(tableDao).findById(1);
    }

    @Test
    void getTableById_returnsEmptyWhenNotFound() {
        when(tableDao.findById(99)).thenReturn(Optional.empty());

        Optional<Table> result = tableService.getTableById(99);

        assertFalse(result.isPresent());
        verify(tableDao).findById(99);
    }

    @Test
    void createTable_returnsCreatedTable() {
        BigDecimal posX = new BigDecimal("150.0");
        BigDecimal posY = new BigDecimal("200.0");
        when(tableDao.create(1, 3, posX, posY, true)).thenReturn(
                new Table(3, 1, 3, posX, posY, true, LocalDateTime.now(), LocalDateTime.now()));

        Table result = tableService.createTable(1, 3, posX, posY, true);

        assertNotNull(result);
        assertEquals(3, result.getId());
        assertEquals(3, result.getTableNumber());
        assertEquals(posX, result.getPosX());
        assertEquals(posY, result.getPosY());
        assertTrue(result.getAvailability());
        verify(tableDao).create(1, 3, posX, posY, true);
    }

    @Test
    void updateTable_returnsUpdatedTable() {
        Table updated = new Table(1, 2, 1, new BigDecimal("200.0"), new BigDecimal("100.0"), false,
                table1.getCreatedAt(), LocalDateTime.now());
        when(tableDao.update(table1)).thenReturn(updated);

        Table result = tableService.updateTable(table1);

        assertEquals(updated, result);
        assertEquals(2, result.getLocationId());
        assertFalse(result.getAvailability());
        verify(tableDao).update(table1);
    }

    @Test
    void deleteTable_returnsTrueOnSuccess() {
        when(tableDao.delete(1)).thenReturn(true);

        boolean result = tableService.deleteTable(1);

        assertTrue(result);
        verify(tableDao).delete(1);
    }

    @Test
    void deleteTable_returnsFalseWhenNotFound() {
        when(tableDao.delete(99)).thenReturn(false);

        boolean result = tableService.deleteTable(99);

        assertFalse(result);
        verify(tableDao).delete(99);
    }

    @Test
    void updateTableAvailability_returnsTrueOnSuccess() {
        when(tableDao.updateAvailability(1, false)).thenReturn(true);

        boolean result = tableService.updateTableAvailability(1, false);

        assertTrue(result);
        verify(tableDao).updateAvailability(1, false);
    }

    @Test
    void updateTableAvailability_returnsFalseWhenNotFound() {
        when(tableDao.updateAvailability(99, true)).thenReturn(false);

        boolean result = tableService.updateTableAvailability(99, true);

        assertFalse(result);
        verify(tableDao).updateAvailability(99, true);
    }
}
