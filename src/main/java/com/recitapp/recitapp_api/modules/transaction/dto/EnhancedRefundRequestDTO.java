package com.recitapp.recitapp_api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedRefundRequestDTO {
    private Long transactionId;
    private String reason;
    private Boolean fullRefund;
    private List<Long> ticketIds; // Only for partial refund
    
    // New fields for enhanced refund processing
    private Boolean forceMercadoPagoRefund; // Force attempt MercadoPago refund even if risky
    private Boolean allowWalletFallback; // Allow fallback to wallet if MercadoPago fails
    private String mercadoPagoPaymentId; // Optional: specific MercadoPago payment ID
} 