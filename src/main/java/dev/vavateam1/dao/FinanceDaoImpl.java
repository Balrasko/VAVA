package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.report.FinanceItemReport;
import dev.vavateam1.report.FinanceReport;

public class FinanceDaoImpl implements FinanceDao {

    private static final String REPORT_DATE_SQL = """
            SELECT COALESCE(MAX(created_at::date), CURRENT_DATE)
            FROM payments
            WHERE COALESCE(refunded, FALSE) = FALSE
            """;

    private static final String DAILY_SALES_SQL = """
            SELECT COALESCE(SUM(amount), 0)
            FROM payments
            WHERE COALESCE(refunded, FALSE) = FALSE
              AND created_at::date = ?
            """;

    private static final String SOLD_ITEMS_TOTAL_SQL = """
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM order_items oi
            JOIN payments p ON p.id = oi.payment_id
            WHERE COALESCE(p.refunded, FALSE) = FALSE
              AND p.created_at::date = ?
            """;

    private static final String ITEMS_SQL = """
            SELECT mi.item_code AS item_id,
                   mi.name,
                   COALESCE(SUM(oi.quantity), 0) AS sold_pieces,
                   mi.price AS price_per_piece
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            JOIN payments p ON p.id = oi.payment_id
            WHERE COALESCE(p.refunded, FALSE) = FALSE
              AND p.created_at::date = ?
            GROUP BY mi.id, mi.item_code, mi.name, mi.price
            ORDER BY sold_pieces DESC, mi.name ASC
            """;

    private static final String FILTERED_ITEMS_SQL_BASE = """
            SELECT mi.item_code AS item_id,
                   mi.name,
                   COALESCE(SUM(oi.quantity), 0) AS sold_pieces,
                   mi.price AS price_per_piece
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            JOIN payments p ON p.id = oi.payment_id
            WHERE COALESCE(p.refunded, FALSE) = FALSE
            """;

    private final ConnectionFactory connectionFactory;

    @Inject
    public FinanceDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public FinanceReport getFinanceReport() {
        try (Connection conn = connectionFactory.getConnection()) {
            LocalDate reportDate = findReportDate(conn);
            BigDecimal dailySales = findDailySales(conn, reportDate);
            int soldItemsTotal = findSoldItemsTotal(conn, reportDate);
            List<FinanceItemReport> items = findItems(conn, reportDate);
            return new FinanceReport(reportDate, dailySales, soldItemsTotal, items);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load finance report", e);
        }
    }

    @Override
    public List<FinanceItemReport> getFinanceItems(LocalDate fromDate, LocalDate toDate, Integer categoryId) {
        try (Connection conn = connectionFactory.getConnection()) {
            return findItems(conn, fromDate, toDate, categoryId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load filtered finance items", e);
        }
    }

    private LocalDate findReportDate(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(REPORT_DATE_SQL);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Date reportDate = rs.getDate(1);
                if (reportDate != null) {
                    return reportDate.toLocalDate();
                }
            }
            return LocalDate.now();
        }
    }

    private BigDecimal findDailySales(Connection conn, LocalDate reportDate) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DAILY_SALES_SQL)) {
            stmt.setDate(1, Date.valueOf(reportDate));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private int findSoldItemsTotal(Connection conn, LocalDate reportDate) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SOLD_ITEMS_TOTAL_SQL)) {
            stmt.setDate(1, Date.valueOf(reportDate));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<FinanceItemReport> findItems(Connection conn, LocalDate reportDate) throws SQLException {
        List<FinanceItemReport> items = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(ITEMS_SQL)) {
            stmt.setDate(1, Date.valueOf(reportDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new FinanceItemReport(
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getInt("sold_pieces"),
                            rs.getBigDecimal("price_per_piece")));
                }
            }
        }

        return items;
    }

    private List<FinanceItemReport> findItems(Connection conn, LocalDate fromDate, LocalDate toDate, Integer categoryId)
            throws SQLException {
        List<FinanceItemReport> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder(FILTERED_ITEMS_SQL_BASE);
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append(" AND p.created_at::date >= ?");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND p.created_at::date <= ?");
            params.add(Date.valueOf(toDate));
        }

        if (categoryId != null) {
            sql.append(" AND mi.category_id = ?");
            params.add(categoryId);
        }

        sql.append("""
                
                GROUP BY mi.id, mi.item_code, mi.name, mi.price
                ORDER BY sold_pieces DESC, mi.name ASC
                """);

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new FinanceItemReport(
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getInt("sold_pieces"),
                            rs.getBigDecimal("price_per_piece")));
                }
            }
        }

        return items;
    }
}
