package dev.vavateam1.service;

import java.math.BigDecimal;

import dev.vavateam1.report.ClosingSummary;

public interface ClosingService {
    ClosingSummary getClosingSummary();

    ClosingSummary addCashFloat(int userId, BigDecimal amount);

    ClosingSummary withdrawCash(int userId, BigDecimal amount);

    boolean closeDay(int userId);
}
