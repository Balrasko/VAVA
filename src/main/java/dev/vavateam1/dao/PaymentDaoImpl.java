package dev.vavateam1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.data.connection.ConnectionFactory;
import dev.vavateam1.model.Payment;
import dev.vavateam1.util.SqlUtils;

public class PaymentDaoImpl implements PaymentDao {

    private static final String SELECT_COLUMNS = "p.id, p.waiter_id, p.method_id, pm.name AS payment_method_name, " +
            "p.amount, p.refunded, p.tip, p.created_at, p.updated_at";

    private static final String FROM_JOIN = "FROM payments p JOIN payment_methods pm ON p.method_id = pm.id";

    private final ConnectionFactory connectionFactory;

    @Inject
    public PaymentDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " " + FROM_JOIN + " ORDER BY p.created_at DESC";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapRow(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch payments", e);
        }
    }

    @Override
    public Payment findById(int id) {
        String sql = "SELECT " + SELECT_COLUMNS + " " + FROM_JOIN + " WHERE p.id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch payment with id " + id, e);
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setWaiterId(rs.getInt("waiter_id"));
        p.setMethodId(rs.getInt("method_id"));
        p.setPaymentMethodName(rs.getString("payment_method_name"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setRefunded((Boolean) rs.getObject("refunded"));
        p.setTip(rs.getBigDecimal("tip"));
        p.setCreatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        p.setUpdatedAt(SqlUtils.toLocalDateTime(rs.getTimestamp("updated_at")));
        return p;
    }

    @Override
    public Payment setRefunded(Payment payment) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setRefunded'");
    }
}
