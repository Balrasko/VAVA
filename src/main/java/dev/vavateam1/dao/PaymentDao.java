package dev.vavateam1.dao;

import java.util.List;
import dev.vavateam1.dto.PaymentDto;

public interface PaymentDao {
    List<PaymentDto> findAll();

    PaymentDto findById(int id);

    PaymentDto setRefunded(PaymentDto payment);
}
