package dev.vavateam1.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FinanceReport(
        LocalDate reportDate,
        BigDecimal dailySales,
        int soldItemsTotal,
        List<FinanceItemReport> items) {
}
