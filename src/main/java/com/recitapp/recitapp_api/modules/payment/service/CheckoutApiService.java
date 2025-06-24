package com.recitapp.recitapp_api.modules.payment.service;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.dto.CheckoutApiPaymentRequestDTO;

public interface CheckoutApiService {
    
    /**
     * Process payment using Checkout API with card token
     * @param paymentRequest Payment request with card token
     * @return Payment response with status and details
     */
    PaymentResponseDTO processCardPayment(CheckoutApiPaymentRequestDTO paymentRequest);
    
    /**
     * Process payment using Mercado Pago wallet (account_money)
     * @param paymentRequest Payment request for wallet payment
     * @return Payment response with status and details
     */
    PaymentResponseDTO processWalletPayment(PaymentRequestDTO paymentRequest);
    
    /**
     * Check payment status by payment ID
     * @param paymentId MercadoPago payment ID
     * @return Payment status
     */
    String getPaymentStatus(String paymentId);
    
    /**
     * Get public key for frontend initialization
     * @return MercadoPago public key
     */
    String getPublicKey();
} 