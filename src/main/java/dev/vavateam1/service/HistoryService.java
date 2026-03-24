package dev.vavateam1.service;

import java.util.List;

import dev.vavateam1.dto.PaymentDto;
import dev.vavateam1.model.PaymentSummary;

public interface HistoryService {
    List<PaymentDto> getPayments();

    PaymentSummary getPaymentSummary(int paymentId);

    void refund(int paymentId);
}
