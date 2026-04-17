package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.InventoryIngredient;
//import dev.vavateam1.util.SqlUtils;

public class InventoryIngredientDaoImpl implements InventoryIngredientDao {

    private static final String FIND_ALL_SQL = """
            SELECT id, name, quantity, minimal_quantity, unit, cost_per_unit, created_at, updated_at, deleted_at
            FROM inventory_ingredients
            WHERE deleted_at IS NULL
            ORDER BY id ASC
            """;

    private static final String DELETE_SQL = """
            UPDATE inventory_ingredients
            SET deleted_at = NOW(), updated_at = NOW()
            WHERE id = ? AND deleted_at IS NULL
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id
            FROM inventory_ingredients
            WHERE id = ?
            """;

    private static final String FIND_BY_NAME_SQL = """
            SELECT id
            FROM inventory_ingredients
            WHERE LOWER(name) = LOWER(?)
            """;

    private static final String INSERT_SQL = """
            INSERT INTO inventory_ingredients (name, quantity, minimal_quantity, unit, cost_per_unit, updated_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            """;

    private static final String UPDATE_BY_ID_SQL = """
            UPDATE inventory_ingredients
            SET name = ?, quantity = ?, minimal_quantity = ?, unit = ?, cost_per_unit = ?, updated_at = NOW()
            WHERE id = ?
            """;

    private static final String UPDATE_BY_NAME_SQL = """
            UPDATE inventory_ingredients
            SET quantity = ?, minimal_quantity = ?, unit = ?, cost_per_unit = ?, updated_at = NOW()
            WHERE LOWER(name) = LOWER(?)
            """;

    private final ConnectionFactory connectionFactory;

    @Inject
    public InventoryIngredientDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private InventoryIngredient mapRow(ResultSet rs) throws SQLException {
        return new InventoryIngredient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getBigDecimal("quantity"),
                rs.getBigDecimal("minimal_quantity"),
                rs.getString("unit"),
                rs.getBigDecimal("cost_per_unit"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getObject("deleted_at", OffsetDateTime.class));
    }

    @Override
    public List<InventoryIngredient> findAll() {
        List<InventoryIngredient> ingredients = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ingredients.add(mapRow(rs));
            }

            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch inventory ingredients", e);
        }
    }

    @Override
    public void saveAll(List<InventoryIngredient> ingredients) {
        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (InventoryIngredient ingredient : ingredients) {
                    saveIngredient(conn, ingredient);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save inventory ingredients", e);
        }
    }

    private void saveIngredient(Connection conn, InventoryIngredient ingredient) throws SQLException {
        Integer existingId = ingredient.getId() > 0
                ? findExistingIdById(conn, ingredient.getId())
                : findExistingIdByName(conn, ingredient.getName());

        if (existingId != null) {
            updateIngredient(conn, existingId, ingredient);
        } else {
            insertIngredient(conn, ingredient);
        }
    }

    private Integer findExistingIdById(Connection conn, int id) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    private Integer findExistingIdByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(FIND_BY_NAME_SQL)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    private void insertIngredient(Connection conn, InventoryIngredient ingredient) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindCommonFields(stmt, ingredient);
            stmt.executeUpdate();
        }
    }

    private void updateIngredient(Connection conn, int existingId, InventoryIngredient ingredient) throws SQLException {
        if (ingredient.getId() > 0) {
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_BY_ID_SQL)) {
                bindCommonFields(stmt, ingredient);
                stmt.setInt(6, existingId);
                stmt.executeUpdate();
            }
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_BY_NAME_SQL)) {
            stmt.setBigDecimal(1, normalizeDecimal(ingredient.getQuantity()));
            stmt.setBigDecimal(2, normalizeDecimal(ingredient.getMinimalQuantity()));
            stmt.setString(3, ingredient.getUnit());
            stmt.setBigDecimal(4, normalizeDecimal(ingredient.getCostPerUnit()));
            stmt.setString(5, ingredient.getName());
            stmt.executeUpdate();
        }
    }

    private void bindCommonFields(PreparedStatement stmt, InventoryIngredient ingredient) throws SQLException {
        stmt.setString(1, ingredient.getName());
        stmt.setBigDecimal(2, normalizeDecimal(ingredient.getQuantity()));
        stmt.setBigDecimal(3, normalizeDecimal(ingredient.getMinimalQuantity()));
        stmt.setString(4, ingredient.getUnit());
        stmt.setBigDecimal(5, normalizeDecimal(ingredient.getCostPerUnit()));
    }

    @Override
    public void delete(int id) {
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete inventory ingredient", e);
        }
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
