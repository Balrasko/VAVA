package dev.vavateam1.service;

import java.math.BigDecimal;

import com.google.inject.Inject;

import dev.vavateam1.dao.ClosingDao;
import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.model.ClosingSummary;

public class ClosingServiceImpl implements ClosingService {

    private final ClosingDao closingDao;

    @Inject
    public ClosingServiceImpl(ClosingDao closingDao) {
        this.closingDao = closingDao;
    }

    @Override
    public ClosingSummary getClosingSummary() {
        return closingDao.getClosingSummary();
    }

    @Override
    public ClosingSummary addCashFloat(int userId, BigDecimal amount) {
        validateAmount(amount);
        return closingDao.recordCashOperation(userId, CashOperationType.CASH_FLOAT, amount, "Cash float insert");
    }

    @Override
    public ClosingSummary withdrawCash(int userId, BigDecimal amount) {
        validateAmount(amount);
        return closingDao.recordCashOperation(userId, CashOperationType.WITHDRAWAL, amount, "Cash withdrawal");
    }

    @Override
    public boolean closeDay(int userId) {
        ClosingSummary summary = closingDao.getClosingSummary();
        return closingDao.createDailyClosing(userId, summary);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
