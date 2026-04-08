package dev.vavateam1.dao;

import java.math.BigDecimal;

import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.model.ClosingSummary;

public interface ClosingDao {
    ClosingSummary getClosingSummary();

    ClosingSummary recordCashOperation(int userId, CashOperationType operationType, BigDecimal amount, String note);

    boolean createDailyClosing(int userId, ClosingSummary summary);
}
