package dev.vavateam1.service;

import dev.vavateam1.dao.ClosingDao;
import dev.vavateam1.model.CashOperationType;
import dev.vavateam1.report.ClosingSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosingServiceImplTest {

    @Mock
    ClosingDao closingDao;

    @InjectMocks
    ClosingServiceImpl closingService;

    @Test
    void addCashFloat_nullAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> closingService.addCashFloat(1, null));
    }

    @Test
    void addCashFloat_zeroAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> closingService.addCashFloat(1, BigDecimal.ZERO));
    }

    @Test
    void addCashFloat_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> closingService.addCashFloat(1, new BigDecimal("-10.00")));
    }

    @Test
    void addCashFloat_validAmount_callsDao() {
        BigDecimal amount = new BigDecimal("50.00");
        ClosingSummary fakeSummary = mock(ClosingSummary.class);
        when(closingDao.recordCashOperation(eq(1), eq(CashOperationType.CASH_FLOAT), eq(amount), anyString()))
            .thenReturn(fakeSummary);

        ClosingSummary result = closingService.addCashFloat(1, amount);

        assertNotNull(result);
        verify(closingDao).recordCashOperation(eq(1), eq(CashOperationType.CASH_FLOAT), eq(amount), anyString());
    }

    @Test
    void withdrawCash_nullAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> closingService.withdrawCash(1, null));
    }

    @Test
    void withdrawCash_validAmount_callsDao() {
        BigDecimal amount = new BigDecimal("20.00");
        ClosingSummary fakeSummary = mock(ClosingSummary.class);
        when(closingDao.recordCashOperation(eq(1), eq(CashOperationType.WITHDRAWAL), eq(amount), anyString()))
            .thenReturn(fakeSummary);

        closingService.withdrawCash(1, amount);

        verify(closingDao).recordCashOperation(eq(1), eq(CashOperationType.WITHDRAWAL), eq(amount), anyString());
    }
}