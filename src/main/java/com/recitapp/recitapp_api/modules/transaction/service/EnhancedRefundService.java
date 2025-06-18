package com.recitapp.recitapp_api.modules.transaction.service;

import com.recitapp.recitapp_api.modules.transaction.dto.EnhancedRefundRequestDTO;
import com.recitapp.recitapp_api.modules.transaction.dto.EnhancedRefundResponseDTO;

public interface EnhancedRefundService {
    
    /**
     * Processes an enhanced refund with MercadoPago integration and wallet fallback
     * 
     * @param refundRequest The enhanced refund request
     * @return EnhancedRefundResponseDTO with detailed processing information
     */
    EnhancedRefundResponseDTO processEnhancedRefund(EnhancedRefundRequestDTO refundRequest);
    
    /**
     * Checks if a transaction can be refunded through MercadoPago
     * 
     * @param transactionId The transaction ID
     * @return true if the transaction can be refunded through MercadoPago
     */
    boolean canRefundThroughMercadoPago(Long transactionId);
} 