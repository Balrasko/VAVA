package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyClosing {
    private int id;
    private int closedByUserId;
    private LocalDate businessDate;
    private BigDecimal totalPaid;
    private BigDecimal totalTips;
    private BigDecimal grandTotal;
    private BigDecimal cashFloat;
    private BigDecimal cash;
    private BigDecimal card;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}