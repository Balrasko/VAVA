package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Payment {
    private Integer id;
    private Integer waiterId;
    private Integer methodId;
    private String paymentMethodName;
    private BigDecimal amount;
    private Boolean refunded;
    private BigDecimal tip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}