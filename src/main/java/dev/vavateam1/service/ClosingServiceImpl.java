package dev.vavateam1.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.dao.ClosingDao;
import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.report.ClosingSummary;

public class ClosingServiceImpl implements ClosingService {
    private static final Logger log = LoggerFactory.getLogger(ClosingServiceImpl.class);

    private final ClosingDao closingDao;

    @Inject
    public ClosingServiceImpl(ClosingDao closingDao) {
        this.closingDao = closingDao;
    }

    @Override
    public ClosingSummary getClosingSummary() {
        log.info("Fetching closing summary");
        return closingDao.getClosingSummary();
    }

    @Override
    public ClosingSummary addCashFloat(int userId, BigDecimal amount) {
        validateAmount(amount);
        log.info("Adding cash float of {} for user id: {}", amount, userId);
        ClosingSummary summary = closingDao.recordCashOperation(userId, CashOperationType.CASH_FLOAT, amount, "Cash float insert");
        log.info("Cash float recorded for user id: {}", userId);
        return summary;
    }

    @Override
    public ClosingSummary withdrawCash(int userId, BigDecimal amount) {
        validateAmount(amount);
        log.info("Withdrawing cash of {} for user id: {}", amount, userId);
        ClosingSummary summary = closingDao.recordCashOperation(userId, CashOperationType.WITHDRAWAL, amount, "Cash withdrawal");
        log.info("Cash withdrawal recorded for user id: {}", userId);
        return summary;
    }

    @Override
    public boolean closeDay(int userId) {
        log.info("Closing day for user id: {}", userId);
        ClosingSummary summary = closingDao.getClosingSummary();
        boolean closed = closingDao.createDailyClosing(userId, summary);
        if (closed) {
            log.info("Day closed successfully for user id: {}, business date: {}", userId, summary.businessDate());
        } else {
            log.info("Day close skipped (already closed) for business date: {}", summary.businessDate());
        }
        return closed;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
