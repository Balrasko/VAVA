package dev.vavateam1.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.dto.PaymentDto;
//import dev.vavateam1.util.SqlUtils;

public class PaymentDaoImpl implements PaymentDao {
    private static final Logger log = LoggerFactory.getLogger(PaymentDaoImpl.class);

    private static final String SELECT_COLUMNS = "p.id, p.waiter_id, p.method_id, pm.name AS payment_method_name, " +
            "p.amount, p.refunded, p.tip, p.created_at, p.updated_at";

    private static final String FROM_JOIN = "FROM payments p JOIN payment_methods pm ON p.method_id = pm.id";

    private final ConnectionFactory connectionFactory;

    @Inject
    public PaymentDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private PaymentDto mapRow(ResultSet rs) throws SQLException {
        PaymentDto p = new PaymentDto();
        p.setId(rs.getInt("id"));
        p.setWaiterId(rs.getInt("waiter_id"));
        p.setMethodId(rs.getInt("method_id"));
        p.setPaymentMethodName(rs.getString("payment_method_name"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setRefunded((Boolean) rs.getObject("refunded"));
        p.setTip(rs.getBigDecimal("tip"));
        p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        p.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return p;
    }

    @Override
    public List<PaymentDto> findAll() {
        log.info("Fetching all payments");
        String sql = "SELECT " + SELECT_COLUMNS + " " + FROM_JOIN + " ORDER BY p.created_at DESC";
        List<PaymentDto> payments = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapRow(rs));
            }
            log.info("Fetched {} payments", payments.size());
            return payments;
        } catch (SQLException e) {
            log.error("Failed to fetch payments", e);
            throw new RuntimeException("Failed to fetch payments", e);
        }
    }

    @Override
    public PaymentDto findById(int id) {
        log.info("Fetching payment id: {}", id);
        String sql = "SELECT " + SELECT_COLUMNS + " " + FROM_JOIN + " WHERE p.id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                log.info("Payment found id: {}", id);
                return mapRow(rs);
            }
            log.info("Payment not found id: {}", id);
            return null;
        } catch (SQLException e) {
            log.error("Failed to fetch payment id: {}", id, e);
            throw new RuntimeException("Failed to fetch payment with id " + id, e);
        }
    }

    @Override
    public PaymentDto setRefunded(PaymentDto payment) {
        log.info("Marking payment as refunded, id: {}", payment.getId());
        String sql = "UPDATE payments SET refunded = TRUE, updated_at = NOW() " +
                "WHERE id = ? AND COALESCE(refunded, FALSE) = FALSE";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getId());
            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                PaymentDto currentPayment = findById(payment.getId());
                if (currentPayment != null && Boolean.TRUE.equals(currentPayment.getRefunded())) {
                    throw new IllegalStateException("Payment " + payment.getId() + " is already refunded");
                }
                return null;
            }

            log.info("Payment marked as refunded id: {}", payment.getId());
            return findById(payment.getId());
        } catch (SQLException e) {
            log.error("Failed to mark payment as refunded id: {}", payment.getId(), e);
            throw new RuntimeException("Failed to mark payment " + payment.getId() + " as refunded", e);
        }
    }

    @Override
    public int createPayment(int waiterId, int methodId, BigDecimal amount, boolean refunded, BigDecimal tip) {
        log.info("Creating payment for waiter id: {}, amount: {}", waiterId, amount);
        String sql = "INSERT INTO payments (waiter_id, method_id, amount, refunded, tip) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, waiterId);
            stmt.setInt(2, methodId);
            stmt.setBigDecimal(3, amount);
            stmt.setBoolean(4, refunded);
            stmt.setBigDecimal(5, tip);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    log.info("Payment created with id: {}", id);
                    return id;
                }
            }
            throw new RuntimeException("Failed to get generated key for new payment");
        } catch (SQLException e) {
            log.error("Failed to create payment for waiter id: {}", waiterId, e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }
}
