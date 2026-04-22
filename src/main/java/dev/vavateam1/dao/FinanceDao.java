package dev.vavateam1.dao;

import java.time.LocalDate;
import java.util.List;

import dev.vavateam1.report.FinanceItemReport;
import dev.vavateam1.report.FinanceReport;

public interface FinanceDao {
    FinanceReport getFinanceReport();

    List<FinanceItemReport> getFinanceItems(LocalDate fromDate, LocalDate toDate, Integer categoryId);
}
