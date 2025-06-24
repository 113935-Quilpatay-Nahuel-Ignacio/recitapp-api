package com.recitapp.recitapp_api.modules.payment.service;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickPreferenceResponseDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickProcessRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;

public interface PaymentBrickService {
    
    /**
     * Crea una preferencia de MercadoPago para el Payment Brick
     * 
     * @param request Datos de la compra y configuración
     * @return Respuesta con preference_id y configuración
     */
    PaymentBrickPreferenceResponseDTO createPaymentBrickPreference(PaymentBrickRequestDTO request);
    
    /**
     * Procesa el pago enviado desde el Payment Brick
     * 
     * @param request Datos del formulario de pago del Payment Brick
     * @return Respuesta del procesamiento del pago
     */
    PaymentResponseDTO processPaymentBrickPayment(PaymentBrickProcessRequestDTO request);
    
    /**
     * Obtiene la configuración del Payment Brick para el frontend
     * 
     * @return Configuración con public key y opciones disponibles
     */
    PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO getPaymentBrickConfig();
    
    /**
     * Valida si un método de pago está habilitado para Payment Brick
     * 
     * @param paymentMethodId ID del método de pago
     * @return true si está habilitado
     */
    boolean isPaymentMethodEnabled(String paymentMethodId);
} 