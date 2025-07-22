package com.recitapp.recitapp_api.modules.payment.controller;

import com.recitapp.recitapp_api.modules.payment.dto.CheckoutApiPaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.CheckoutApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/checkout-api")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CheckoutApiController {
    
    private final CheckoutApiService checkoutApiService;
    
    /**
     * Procesar pago con tarjeta usando Checkout API
     * POST /api/checkout-api/card-payment
     */
    @PostMapping("/card-payment")
    public ResponseEntity<PaymentResponseDTO> processCardPayment(
            @Valid @RequestBody CheckoutApiPaymentRequestDTO paymentRequest) {
        
        // ========================================

        // ========================================
        System.out.println("\n" +
            "████████████████████████████████████████████████████████████████\n" +
            "██                                                            ██\n" +
            "██  🎯 ENDPOINT: /checkout-api/card-payment CALLED 🎯         ██\n" +
            "██                                                            ██\n" +
            "████████████████████████████████████████████████████████████████");
        
        System.out.println("🔍 [CHECKOUT_API_CONTROLLER] Event ID: " + paymentRequest.getEventId());
        System.out.println("🔍 [CHECKOUT_API_CONTROLLER] User ID: " + paymentRequest.getUserId());
        System.out.println("🔍 [CHECKOUT_API_CONTROLLER] Total Amount: " + paymentRequest.getTotalAmount());
        if (paymentRequest.getCardInfo() != null) {
            System.out.println("🔍 [CHECKOUT_API_CONTROLLER] Cardholder Name: '" + paymentRequest.getCardInfo().getCardholderName() + "'");
        }
        if (paymentRequest.getPayer() != null) {
            System.out.println("🔍 [CHECKOUT_API_CONTROLLER] Payer Email: " + paymentRequest.getPayer().getEmail());
        }
        System.out.println("████████████████████████████████████████████████████████████████\n");
        
        log.info("🚀 [CHECKOUT_API_CONTROLLER] Recibida petición de pago con tarjeta - Event: {}, Amount: {}", 
                paymentRequest.getEventId(), paymentRequest.getTotalAmount());
        
        try {
            PaymentResponseDTO response = checkoutApiService.processCardPayment(paymentRequest);
            
            log.info("✅ [CHECKOUT_API_CONTROLLER] Pago procesado - Status: {}", response.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API_CONTROLLER] Error procesando pago con tarjeta: {}", e.getMessage(), e);
            
            PaymentResponseDTO errorResponse = PaymentResponseDTO.builder()
                .status("ERROR")
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodName("Error: " + e.getMessage())
                    .build())
                .build();
                
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Procesar pago con dinero en cuenta de Mercado Pago
     * POST /api/checkout-api/wallet-payment
     */
    @PostMapping("/wallet-payment")
    public ResponseEntity<PaymentResponseDTO> processWalletPayment(
            @Valid @RequestBody PaymentRequestDTO paymentRequest) {
        
        log.info("🚀 [CHECKOUT_API_CONTROLLER] Recibida petición de pago con wallet - Event: {}, Amount: {}", 
                paymentRequest.getEventId(), paymentRequest.getTotalAmount());
        
        try {
            PaymentResponseDTO response = checkoutApiService.processWalletPayment(paymentRequest);
            
            log.info("✅ [CHECKOUT_API_CONTROLLER] Pago con wallet procesado - Status: {}", response.getStatus());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API_CONTROLLER] Error procesando pago con wallet: {}", e.getMessage(), e);
            
            PaymentResponseDTO errorResponse = PaymentResponseDTO.builder()
                .status("ERROR")
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodName("Error: " + e.getMessage())
                    .build())
                .build();
                
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Obtener estado de un pago
     * GET /api/checkout-api/payment-status/{paymentId}
     */
    @GetMapping("/payment-status/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        
        log.info("🔍 [CHECKOUT_API_CONTROLLER] Consultando estado del pago: {}", paymentId);
        
        try {
            String status = checkoutApiService.getPaymentStatus(paymentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", paymentId);
            response.put("status", status);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("✅ [CHECKOUT_API_CONTROLLER] Estado del pago {} obtenido: {}", paymentId, status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API_CONTROLLER] Error obteniendo estado del pago {}: {}", paymentId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("paymentId", paymentId);
            errorResponse.put("status", "error");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Obtener la clave pública de MercadoPago para inicializar el SDK
     * GET /api/checkout-api/public-key
     */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        
        log.info("🔑 [CHECKOUT_API_CONTROLLER] Solicitada clave pública");
        
        try {
            String publicKey = checkoutApiService.getPublicKey();
            
            Map<String, String> response = new HashMap<>();
            response.put("publicKey", publicKey);
            
            log.info("✅ [CHECKOUT_API_CONTROLLER] Clave pública proporcionada");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API_CONTROLLER] Error obteniendo clave pública: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error obteniendo clave pública");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Endpoint de salud para verificar que el servicio está funcionando
     * GET /api/checkout-api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Checkout API Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        
        log.debug("📊 [CHECKOUT_API_CONTROLLER] Health check solicitado");
        
        return ResponseEntity.ok(response);
    }
} 