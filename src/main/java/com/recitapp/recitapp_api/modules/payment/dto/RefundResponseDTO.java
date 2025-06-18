package com.recitapp.recitapp_api.modules.payment.dto;

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
public class RefundResponseDTO {
    
    private boolean success;
    private String refundId;
    private String paymentId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private LocalDateTime processedAt;
    private String errorMessage;
    private String errorCode;
    
    // Factory methods for common scenarios
    public static RefundResponseDTO success(String refundId, String paymentId, BigDecimal amount, String status) {
        return RefundResponseDTO.builder()
                .success(true)
                .refundId(refundId)
                .paymentId(paymentId)
                .amount(amount)
                .status(status)
                .processedAt(LocalDateTime.now())
                .build();
    }
    
    public static RefundResponseDTO success(String refundId, String paymentId, BigDecimal amount, String status, String reason) {
        return RefundResponseDTO.builder()
                .success(true)
                .refundId(refundId)
                .paymentId(paymentId)
                .amount(amount)
                .status(status)
                .reason(reason)
                .processedAt(LocalDateTime.now())
                .build();
    }
    
    public static RefundResponseDTO failure(String paymentId, BigDecimal amount, String errorMessage, String errorCode) {
        return RefundResponseDTO.builder()
                .success(false)
                .paymentId(paymentId)
                .amount(amount)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .processedAt(LocalDateTime.now())
                .build();
    }
} 