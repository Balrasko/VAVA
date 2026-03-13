package dev.vavateam1.dao;

import java.util.List;
import dev.vavateam1.model.Payment;

public interface PaymentDao {
    List<Payment> findAll();

    Payment findById(int id);
}
