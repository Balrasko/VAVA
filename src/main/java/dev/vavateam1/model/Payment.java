package dev.vavateam1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {

    private Integer id;
    private Integer waiterId;
    private Integer methodId;
    private BigDecimal amount;
    private Boolean refunded;
    private BigDecimal tip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {}

    public Payment(Integer id, Integer waiterId, Integer methodId, BigDecimal amount, Boolean refunded, BigDecimal tip, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.waiterId = waiterId;
        this.methodId = methodId;
        this.amount = amount;
        this.refunded = refunded;
        this.tip = tip;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getWaiterId() { return waiterId; }
    public void setWaiterId(Integer waiterId) { this.waiterId = waiterId; }

    public Integer getMethodId() { return methodId; }
    public void setMethodId(Integer methodId) { this.methodId = methodId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getRefunded() { return refunded; }
    public void setRefunded(Boolean refunded) { this.refunded = refunded; }

    public BigDecimal getTip() { return tip; }
    public void setTip(BigDecimal tip) { this.tip = tip; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}