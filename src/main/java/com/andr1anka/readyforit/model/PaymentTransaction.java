package com.andr1anka.readyforit.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction", indexes = {
        @Index(name = "idx_payment_order_id", columnList = "order_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Унікальний order_id, що ми надсилаємо в LiqPay (UUID)
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // UAH / USD / EUR

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    // Транзакційний ID від LiqPay (приходить у callback'у)
    @Column(name = "liqpay_payment_id")
    private String liqpayPaymentId;

    // last4 цифр картки — лише ці зберігаємо, повний номер ніколи не торкається нашого сервера
    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    // Тип картки (Visa/Mastercard) — теж від LiqPay
    @Column(name = "card_type", length = 32)
    private String cardType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}