package com.recitapp.recitapp_api.modules.transaction.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private TransactionStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_refund")
    private Boolean isRefund;

    @ManyToOne
    @JoinColumn(name = "original_transaction_id")
    private Transaction originalTransaction;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isRefund == null) {
            isRefund = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
