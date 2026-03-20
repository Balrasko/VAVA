package dev.vavateam1.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private int id;
    private int waiterId;
    private int methodId;
    private String paymentMethodName;
    private BigDecimal amount;
    private boolean refunded;
    private BigDecimal tip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}