package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Table;
import dev.vavateam1.util.SqlUtils;

public class TableDaoImpl implements TableDao {
    private final ConnectionFactory connectionFactory;

    @Inject
    public TableDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public List<Table> findAll() {
        String sql = "SELECT id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at FROM tables";
        List<Table> tables = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Table t = new Table();
                t.setId(rs.getInt("id"));
                t.setLocationId(rs.getInt("location_id"));
                t.setTableNumber(rs.getInt("table_number"));
                t.setPosX(rs.getBigDecimal("pos_x"));
                t.setPosY(rs.getBigDecimal("pos_y"));
                t.setAvailability((Boolean) rs.getObject("availability"));
                t.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                t.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));

                tables.add(t);
            }

            return tables;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tables", e);
        }
    }

    @Override
    public void updatePosition(int tableId, BigDecimal posX, BigDecimal posY) {
        String sql = "UPDATE tables SET pos_x = ?, pos_y = ?, updated_at = NOW() WHERE id = ?";

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
    public Table createTable(int locationId) {
        String sql = "INSERT INTO tables (location_id, table_number, pos_x, pos_y, availability) VALUES (?, (SELECT COALESCE(MAX(table_number), 0) + 1 FROM tables), 0, 0, true) RETURNING id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Table t = new Table();
                t.setId(rs.getInt("id"));
                t.setLocationId(rs.getInt("location_id"));
                t.setTableNumber(rs.getInt("table_number"));
                t.setPosX(rs.getBigDecimal("pos_x"));
                t.setPosY(rs.getBigDecimal("pos_y"));
                t.setAvailability((Boolean) rs.getObject("availability"));
                t.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                t.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
                return t;
            }
            throw new RuntimeException("Failed to insert table");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }
}
