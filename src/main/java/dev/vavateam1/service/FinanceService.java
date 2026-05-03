package dev.vavateam1.service;

import dev.vavateam1.dto.FinanceSummary;
import dev.vavateam1.model.Category;
import dev.vavateam1.report.FinanceItemReport;
import dev.vavateam1.report.FinanceReport;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public interface FinanceService {
    List<FinanceItemReport> getFinanceItems(LocalDate from, LocalDate to, Integer categoryId);
    LocalDate getLatestReportDate();
    List<Category> getCategories();
    LocalDate resolveReportDate(LocalDate from, LocalDate to, LocalDate latestDate);
    FinanceSummary computeSummary(List<FinanceItemReport> items);
    void exportReport(FinanceReport report, Path path) throws IOException;
}
