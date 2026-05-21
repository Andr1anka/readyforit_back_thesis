package com.andr1anka.readyforit.dto;

import com.andr1anka.readyforit.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryItemDTO {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String cardLast4;
    private String cardType;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}