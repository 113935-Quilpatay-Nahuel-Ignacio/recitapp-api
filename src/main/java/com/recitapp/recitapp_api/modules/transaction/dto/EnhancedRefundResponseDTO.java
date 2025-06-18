package com.recitapp.recitapp_api.modules.transaction.dto;

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
public class EnhancedRefundResponseDTO {
    
    // Basic refund information
    private Long refundTransactionId;
    private Long originalTransactionId;
    private BigDecimal refundAmount;
    private String status;
    private LocalDateTime processedAt;
    
    // MercadoPago refund information
    private boolean mercadoPagoRefundAttempted;
    private boolean mercadoPagoRefundSuccessful;
    private String mercadoPagoRefundId;
    private String mercadoPagoErrorMessage;
    
    // Wallet fallback information
    private boolean walletFallbackUsed;
    private BigDecimal walletCreditAmount;
    private BigDecimal newWalletBalance;
    
    // Processing details
    private String processingMethod; // "MERCADOPAGO", "WALLET", "MIXED"
    private String message;
    
    // Factory methods
    public static EnhancedRefundResponseDTO mercadoPagoSuccess(
            Long refundTransactionId, Long originalTransactionId, BigDecimal amount,
            String mercadoPagoRefundId) {
        return EnhancedRefundResponseDTO.builder()
                .refundTransactionId(refundTransactionId)
                .originalTransactionId(originalTransactionId)
                .refundAmount(amount)
                .status("COMPLETED")
                .processedAt(LocalDateTime.now())
                .mercadoPagoRefundAttempted(true)
                .mercadoPagoRefundSuccessful(true)
                .mercadoPagoRefundId(mercadoPagoRefundId)
                .walletFallbackUsed(false)
                .processingMethod("MERCADOPAGO")
                .message("Reembolso procesado exitosamente a través de MercadoPago")
                .build();
    }
    
    public static EnhancedRefundResponseDTO walletFallback(
            Long refundTransactionId, Long originalTransactionId, BigDecimal amount,
            BigDecimal newWalletBalance, String mercadoPagoError) {
        return EnhancedRefundResponseDTO.builder()
                .refundTransactionId(refundTransactionId)
                .originalTransactionId(originalTransactionId)
                .refundAmount(amount)
                .status("COMPLETED")
                .processedAt(LocalDateTime.now())
                .mercadoPagoRefundAttempted(true)
                .mercadoPagoRefundSuccessful(false)
                .mercadoPagoErrorMessage(mercadoPagoError)
                .walletFallbackUsed(true)
                .walletCreditAmount(amount)
                .newWalletBalance(newWalletBalance)
                .processingMethod("WALLET")
                .message("Reembolso procesado como crédito en billetera virtual debido a fallo en MercadoPago")
                .build();
    }
    
    public static EnhancedRefundResponseDTO failure(
            Long originalTransactionId, BigDecimal amount, String errorMessage) {
        return EnhancedRefundResponseDTO.builder()
                .originalTransactionId(originalTransactionId)
                .refundAmount(amount)
                .status("FAILED")
                .processedAt(LocalDateTime.now())
                .mercadoPagoRefundAttempted(true)
                .mercadoPagoRefundSuccessful(false)
                .walletFallbackUsed(false)
                .processingMethod("NONE")
                .message("Error al procesar reembolso: " + errorMessage)
                .build();
    }
} 