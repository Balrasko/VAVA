package dev.vavateam1.service;

import java.util.List;
import dev.vavateam1.model.Payment;

public interface HistoryService {
    List<Payment> getPayments();
}
