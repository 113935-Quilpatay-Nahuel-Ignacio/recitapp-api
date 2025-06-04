package com.recitapp.recitapp_api.modules.payment.service;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;

import java.util.Map;

public interface MercadoPagoService {
    PaymentResponseDTO createPaymentPreference(PaymentRequestDTO paymentRequest);
    PaymentResponseDTO processConfirmedPayment(PaymentRequestDTO paymentRequest);
    void processWebhookPayment(Map<String, String> params, String payload);
    String getPaymentStatus(String paymentId);
    String getPublicKey();
} 