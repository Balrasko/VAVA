package dev.vavateam1.dao;

import java.util.List;
import dev.vavateam1.dto.PaymentDto;

public interface PaymentDao {
    List<PaymentDto> findAll();

    PaymentDto findById(int id);

    PaymentDto setRefunded(PaymentDto payment);

    int createPayment(int waiterId, int methodId, java.math.BigDecimal amount, boolean refunded,
            java.math.BigDecimal tip);
}
