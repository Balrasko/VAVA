package dev.vavateam1.model;

import java.math.BigDecimal;

public record FinanceItemReport(
        int itemId,
        String name,
        int soldPieces,
        BigDecimal pricePerPiece) {
}
