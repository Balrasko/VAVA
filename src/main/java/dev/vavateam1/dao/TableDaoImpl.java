package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Table;
//import dev.vavateam1.util.SqlUtils;

public class TableDaoImpl implements TableDao {
    private final ConnectionFactory connectionFactory;

    @Inject
    public TableDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private Table mapResultSetToTable(ResultSet rs) throws SQLException {
        Table t = new Table();
        t.setId(rs.getInt("id"));
        t.setLocationId(rs.getInt("location_id"));
        t.setTableNumber(rs.getInt("table_number"));
        t.setPosX(rs.getBigDecimal("pos_x"));
        t.setPosY(rs.getBigDecimal("pos_y"));
        t.setAvailability((Boolean) rs.getObject("availability"));
        t.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        t.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        t.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
        return t;
    }

    public List<Table> findAll() {
        String sql = "SELECT id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at, deleted_at FROM tables WHERE deleted_at IS NULL";
        List<Table> tables = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tables.add(mapResultSetToTable(rs));
            }

            return tables;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tables", e);
        }
    }

    @Override
    public void updatePosition(int tableId, BigDecimal posX, BigDecimal posY) {
        String sql = "UPDATE tables SET pos_x = ?, pos_y = ?, updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, posX);
            stmt.setBigDecimal(2, posY);
            stmt.setInt(3, tableId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table position for id=" + tableId, e);
        }
    }

    @Override
    public void updateTableDetails(int tableId, int locationId, int tableNumber, boolean availability) {
        String sql = "UPDATE tables SET location_id = ?, table_number = ?, availability = ?, updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            stmt.setInt(2, tableNumber);
            stmt.setBoolean(3, availability);
            stmt.setInt(4, tableId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table details for id=" + tableId, e);
        }
    }

    @Override
    public void softDeleteTable(int tableId) {
        String sql = "UPDATE tables SET deleted_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft delete table for id=" + tableId, e);
        }
    }

    @Override
    public Table createTable(int locationId) {
        String sql = "INSERT INTO tables (location_id, table_number, pos_x, pos_y, availability) "
                + "VALUES (?, (SELECT COALESCE(MAX(table_number), 0) + 1 FROM tables), 0, 0, true) "
                + "RETURNING id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at, deleted_at";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTable(rs);
            }
            throw new RuntimeException("Failed to insert table");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }
}
