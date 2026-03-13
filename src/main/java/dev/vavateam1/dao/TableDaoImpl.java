package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
