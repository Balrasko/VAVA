package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Table> findAll() {
        String sql = "SELECT id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at FROM tables";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Table> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(mapRow(rs));
            }
            return tables;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all tables", e);
        }
    }

    @Override
    public Optional<Table> findById(int id) {
        String sql = "SELECT id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at FROM tables WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find table by id: " + id, e);
        }
    }

    @Override
    public Table create(int locationId, int tableNumber, BigDecimal posX, BigDecimal posY, boolean availability) {
        String sql = "INSERT INTO tables (location_id, table_number, pos_x, pos_y, availability, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, NOW(), NOW()) "
                + "RETURNING id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            stmt.setInt(2, tableNumber);
            stmt.setBigDecimal(3, posX);
            stmt.setBigDecimal(4, posY);
            stmt.setBoolean(5, availability);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Failed to create table, no row returned");
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table with table_number: " + tableNumber, e);
        }
    }

    @Override
    public Table update(Table table) {
        String sql = "UPDATE tables SET location_id = ?, table_number = ?, pos_x = ?, pos_y = ?, availability = ?, updated_at = NOW() "
                + "WHERE id = ? "
                + "RETURNING id, location_id, table_number, pos_x, pos_y, availability, created_at, updated_at";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, table.getLocationId());
            stmt.setInt(2, table.getTableNumber());
            stmt.setBigDecimal(3, table.getPosX());
            stmt.setBigDecimal(4, table.getPosY());
            stmt.setObject(5, table.getAvailability());
            stmt.setInt(6, table.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Failed to update table with id: " + table.getId());
                }
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table with id: " + table.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM tables WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete table with id: " + id, e);
        }
    }

    @Override
    public boolean updateAvailability(int id, boolean availability) {
        String sql = "UPDATE tables SET availability = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, availability);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update availability for table with id: " + id, e);
        }
    }

    private Table mapRow(ResultSet rs) throws SQLException {
        Table table = new Table();
        table.setId(rs.getInt("id"));
        table.setLocationId(rs.getInt("location_id"));
        table.setTableNumber(rs.getInt("table_number"));
        table.setPosX(rs.getBigDecimal("pos_x"));
        table.setPosY(rs.getBigDecimal("pos_y"));
        table.setAvailability((Boolean) rs.getObject("availability"));
        table.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        table.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
        return table;
    }
}
