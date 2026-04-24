package dev.vavateam1.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Location;

//import dev.vavateam1.util.SqlUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

public class LocationDaoImpl implements LocationDao {
    private static final Logger log = LoggerFactory.getLogger(LocationDaoImpl.class);

    private final ConnectionFactory connectionFactory;

    @Inject
    public LocationDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Location> findAll() {
        log.info("Fetching all locations");
        String sql = "SELECT id, name, created_at, updated_at, deleted_at FROM locations WHERE deleted_at IS NULL ORDER BY id ASC";
        List<Location> locations = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Location loc = new Location();
                loc.setId(rs.getInt("id"));
                loc.setName(rs.getString("name"));
                loc.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                loc.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                loc.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
                locations.add(loc);
            }

            log.info("Fetched {} locations", locations.size());
            return locations;
        } catch (SQLException e) {
            log.error("Failed to fetch locations", e);
            throw new RuntimeException("Failed to fetch locations", e);
        }
    }

    @Override
    public Location createLocation(String name) {
        log.info("Creating location: {}", name);
        String sql = "INSERT INTO locations (name) VALUES (?) RETURNING id, name, created_at, updated_at, deleted_at";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Location loc = new Location();
                loc.setId(rs.getInt("id"));
                loc.setName(rs.getString("name"));
                loc.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                loc.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                loc.setDeletedAt(rs.getObject("deleted_at", OffsetDateTime.class));
                log.info("Location created with id: {}", loc.getId());
                return loc;
            }
            throw new RuntimeException("Failed to insert location");
        } catch (SQLException e) {
            log.error("Failed to create location: {}", name, e);
            throw new RuntimeException("Failed to create location", e);
        }
    }

    @Override
    public void updateLocationName(int locationId, String name) {
        log.info("Updating location id: {} to name: {}", locationId, name);
        String sql = "UPDATE locations SET name = ?, updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, locationId);
            stmt.executeUpdate();
            log.info("Location name updated id: {}", locationId);
        } catch (SQLException e) {
            log.error("Failed to update location name for id: {}", locationId, e);
            throw new RuntimeException("Failed to update location name", e);
        }
    }

    @Override
    public void softDeleteLocation(int locationId) {
        log.info("Soft deleting location id: {}", locationId);
        String sql = "UPDATE locations SET deleted_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted_at IS NULL";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            stmt.executeUpdate();
            log.info("Location soft deleted id: {}", locationId);
        } catch (SQLException e) {
            log.error("Failed to soft delete location id: {}", locationId, e);
            throw new RuntimeException("Failed to soft delete location", e);
        }
    }
}
