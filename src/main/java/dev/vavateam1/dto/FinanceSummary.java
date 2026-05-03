package dev.vavateam1.dto;

import java.math.BigDecimal;

public record FinanceSummary(BigDecimal totalSales, int soldItemsTotal) {}
