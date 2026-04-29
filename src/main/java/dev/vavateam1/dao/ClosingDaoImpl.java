package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.report.ClosingSummary;

public class ClosingDaoImpl implements ClosingDao {
    private static final Logger log = LoggerFactory.getLogger(ClosingDaoImpl.class);

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
                COALESCE(SUM(
                    CASE
                        WHEN COALESCE(p.tip, 0) > 0 THEN p.amount - (p.amount / (1 + (p.tip / 100.0)))
                        ELSE 0
                    END
                ), 0) AS tips_total
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
        log.info("Loading closing summary");
        try (Connection conn = connectionFactory.getConnection()) {
            LocalDate businessDate = findBusinessDate(conn);
            log.info("Loading closing summary for business date: {}", businessDate);
            return loadClosingSummary(conn, businessDate);
        } catch (SQLException e) {
            log.error("Failed to load closing summary", e);
            throw new RuntimeException("Failed to load closing summary", e);
        }
    }

    @Override
    public ClosingSummary recordCashOperation(int userId, CashOperationType operationType, BigDecimal amount, String note) {
        log.info("Recording cash operation {} of {} for user id: {}", operationType, amount, userId);
        try (Connection conn = connectionFactory.getConnection()) {
            LocalDate businessDate = findBusinessDate(conn);

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_CASH_MOVEMENT_SQL)) {
                stmt.setInt(1, userId);
                stmt.setString(2, operationType.name());
                stmt.setBigDecimal(3, normalize(amount));
                stmt.setString(4, note);
                stmt.setDate(5, Date.valueOf(businessDate));
                stmt.executeUpdate();
                log.info("Cash operation {} recorded for business date: {}", operationType, businessDate);
            }

            return loadClosingSummary(conn, businessDate);
        } catch (SQLException e) {
            log.error("Failed to record cash operation {} for user id: {}", operationType, userId, e);
            throw new RuntimeException("Failed to record cash operation", e);
        }
    }

    @Override
    public boolean createDailyClosing(int userId, ClosingSummary summary) {
        log.info("Creating daily closing for user id: {}, business date: {}", userId, summary.businessDate());
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
            boolean created = stmt.executeUpdate() > 0;
            if (created) {
                log.info("Daily closing created for business date: {}", summary.businessDate());
            } else {
                log.info("Daily closing already exists for business date: {}", summary.businessDate());
            }
            return created;
        } catch (SQLException e) {
            log.error("Failed to create daily closing for business date: {}", summary.businessDate(), e);
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
        BigDecimal totalPaid = normalize(salesTotal);
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
