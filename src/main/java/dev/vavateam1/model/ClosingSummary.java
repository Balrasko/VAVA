package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClosingSummary(
        LocalDate businessDate,
        BigDecimal totalPaid,
        BigDecimal totalTips,
        BigDecimal grandTotal,
        BigDecimal cashFloat,
        BigDecimal cash,
        BigDecimal card) {
}
