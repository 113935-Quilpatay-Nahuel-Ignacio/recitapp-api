package com.recitapp.recitapp_api.modules.payment.service;

import com.recitapp.recitapp_api.modules.payment.dto.RefundResponseDTO;

import java.math.BigDecimal;

public interface MercadoPagoRefundService {
    
    /**
     * Attempts to process a refund through MercadoPago
     * 
     * @param paymentId The MercadoPago payment ID
     * @param amount The amount to refund
     * @param reason The reason for the refund
     * @return RefundResponseDTO with the result
     */
    RefundResponseDTO processRefund(String paymentId, BigDecimal amount, String reason);
    
    /**
     * Checks if a payment can be refunded
     * 
     * @param paymentId The MercadoPago payment ID
     * @return true if the payment can be refunded
     */
    boolean canRefund(String paymentId);
    
    /**
     * Gets the refund status from MercadoPago
     * 
     * @param refundId The MercadoPago refund ID
     * @return The refund status
     */
    String getRefundStatus(String refundId);
} 