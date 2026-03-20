package dev.vavateam1.service;

import java.util.List;
import dev.vavateam1.dto.PaymentDto;

public interface HistoryService {
    List<PaymentDto> getPayments();
}