package dev.vavateam1.dao;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Location;
import dev.vavateam1.util.SqlUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.google.inject.Inject;

public class LocationDaoImpl implements LocationDao {
    private final ConnectionFactory connectionFactory;

    @Inject
    public LocationDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Location> findAll() {
        String sql = "SELECT id, name, created_at, updated_at FROM locations ORDER BY id ASC";
        List<Location> locations = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Location loc = new Location();
                loc.setId(rs.getInt("id"));
                loc.setName(rs.getString("name"));
                loc.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                loc.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));

                locations.add(loc);
            }

            return locations;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch locations", e);
        }
    }

    @Override
    public Location createLocation(String name) {
        String sql = "INSERT INTO locations (name) VALUES (?) RETURNING id, name, created_at, updated_at";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Location loc = new Location();
                loc.setId(rs.getInt("id"));
                loc.setName(rs.getString("name"));
                loc.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
                loc.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
                return loc;
            }
            throw new RuntimeException("Failed to insert location");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create location", e);
        }
    }
}
