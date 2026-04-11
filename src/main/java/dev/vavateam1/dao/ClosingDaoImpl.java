package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.report.ClosingSummary;

public class ClosingDaoImpl implements ClosingDao {

    private static final String BUSINESS_DATE_SQL = """
            SELECT COALESCE(
                GREATEST(
                    COALESCE((SELECT MAX(created_at::date) FROM payments WHERE COALESCE(refunded, FALSE) = FALSE), CURRENT_DATE),
                    COALESCE((SELECT MAX(business_date) FROM cash_movements), CURRENT_DATE)
                ),
                CURRENT_DATE
            )
            """;

    private static final String PAYMENT_TOTALS_SQL = """
            SELECT
                COALESCE(SUM(p.amount), 0) AS sales_total,
                COALESCE(SUM(CASE WHEN LOWER(pm.name) = 'cash' THEN p.amount ELSE 0 END), 0) AS cash_total,
                COALESCE(SUM(CASE WHEN LOWER(pm.name) <> 'cash' THEN p.amount ELSE 0 END), 0) AS card_total,
                COALESCE(SUM(p.amount * COALESCE(p.tip, 0) / 100.0), 0) AS tips_total
            FROM payments p
            JOIN payment_methods pm ON pm.id = p.method_id
            WHERE COALESCE(p.refunded, FALSE) = FALSE
              AND p.created_at::date = ?
            """;

    private static final String CASH_MOVEMENTS_SQL = """
            SELECT
                COALESCE(SUM(CASE WHEN operation_type = 'CASH_FLOAT' THEN amount ELSE 0 END), 0) AS cash_float_total,
                COALESCE(SUM(CASE WHEN operation_type = 'WITHDRAWAL' THEN amount ELSE 0 END), 0) AS withdrawal_total
            FROM cash_movements
            WHERE business_date = ?
            """;

    private static final String INSERT_CASH_MOVEMENT_SQL = """
            INSERT INTO cash_movements (user_id, operation_type, amount, note, business_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, NOW(), NOW())
            """;

    private static final String INSERT_DAILY_CLOSING_SQL = """
            INSERT INTO daily_closings (
                closed_by_user_id,
                business_date,
                total_paid,
                total_tips,
                grand_total,
                cash_float,
                cash,
                card,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (business_date) DO NOTHING
            """;

    private final ConnectionFactory connectionFactory;

    @Inject
    public ClosingDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ClosingSummary getClosingSummary() {
        try (Connection conn = connectionFactory.getConnection()) {
            LocalDate businessDate = findBusinessDate(conn);
            return loadClosingSummary(conn, businessDate);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load closing summary", e);
        }
    }

    @Override
    public ClosingSummary recordCashOperation(int userId, CashOperationType operationType, BigDecimal amount, String note) {
        try (Connection conn = connectionFactory.getConnection()) {
            LocalDate businessDate = findBusinessDate(conn);

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_CASH_MOVEMENT_SQL)) {
                stmt.setInt(1, userId);
                stmt.setString(2, operationType.name());
                stmt.setBigDecimal(3, normalize(amount));
                stmt.setString(4, note);
                stmt.setDate(5, Date.valueOf(businessDate));
                stmt.executeUpdate();
            }

            return loadClosingSummary(conn, businessDate);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record cash operation", e);
        }
    }

    @Override
    public boolean createDailyClosing(int userId, ClosingSummary summary) {
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(INSERT_DAILY_CLOSING_SQL)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(summary.businessDate()));
            stmt.setBigDecimal(3, normalize(summary.totalPaid()));
            stmt.setBigDecimal(4, normalize(summary.totalTips()));
            stmt.setBigDecimal(5, normalize(summary.grandTotal()));
            stmt.setBigDecimal(6, normalize(summary.cashFloat()));
            stmt.setBigDecimal(7, normalize(summary.cash()));
            stmt.setBigDecimal(8, normalize(summary.card()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create daily closing", e);
        }
    }

    private LocalDate findBusinessDate(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(BUSINESS_DATE_SQL);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Date businessDate = rs.getDate(1);
                if (businessDate != null) {
                    return businessDate.toLocalDate();
                }
            }
            return LocalDate.now();
        }
    }

    private ClosingSummary loadClosingSummary(Connection conn, LocalDate businessDate) throws SQLException {
        BigDecimal salesTotal = BigDecimal.ZERO;
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal cardTotal = BigDecimal.ZERO;
        BigDecimal tipsTotal = BigDecimal.ZERO;

        try (PreparedStatement stmt = conn.prepareStatement(PAYMENT_TOTALS_SQL)) {
            stmt.setDate(1, Date.valueOf(businessDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    salesTotal = normalize(rs.getBigDecimal("sales_total"));
                    cashTotal = normalize(rs.getBigDecimal("cash_total"));
                    cardTotal = normalize(rs.getBigDecimal("card_total"));
                    tipsTotal = normalize(rs.getBigDecimal("tips_total"));
                }
            }
        }

        BigDecimal cashFloat = BigDecimal.ZERO;
        BigDecimal withdrawals = BigDecimal.ZERO;

        try (PreparedStatement stmt = conn.prepareStatement(CASH_MOVEMENTS_SQL)) {
            stmt.setDate(1, Date.valueOf(businessDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cashFloat = normalize(rs.getBigDecimal("cash_float_total"));
                    withdrawals = normalize(rs.getBigDecimal("withdrawal_total"));
                }
            }
        }

        BigDecimal netCashFloat = normalize(cashFloat.subtract(withdrawals));
        BigDecimal totalPaid = normalize(salesTotal.add(tipsTotal));
        BigDecimal grandTotal = normalize(netCashFloat.add(cashTotal).add(cardTotal));

        return new ClosingSummary(
                businessDate,
                totalPaid,
                tipsTotal,
                grandTotal,
                netCashFloat,
                cashTotal,
                cardTotal);
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
