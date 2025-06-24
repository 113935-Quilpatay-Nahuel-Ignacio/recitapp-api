package com.recitapp.recitapp_api.modules.payment.controller;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickPreferenceResponseDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickProcessRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.PaymentBrickService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/brick")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configurar según necesidades de CORS
public class PaymentBrickController {

    private final PaymentBrickService paymentBrickService;

    /**
     * Crear preferencia para Payment Brick
     * POST /api/payments/brick/preference
     */
    @PostMapping("/preference")
    public ResponseEntity<PaymentBrickPreferenceResponseDTO> createPaymentBrickPreference(
            @Valid @RequestBody PaymentBrickRequestDTO request) {
        
        log.info("🧱 [PAYMENT-BRICK-API] Creating preference request - Event: {}, User: {}", 
                request.getEventId(), request.getUserId());
        
        try {
            PaymentBrickPreferenceResponseDTO response = paymentBrickService.createPaymentBrickPreference(request);
            
            if ("success".equals(response.getStatus())) {
                log.info("✅ [PAYMENT-BRICK-API] Preference created successfully - ID: {}", response.getPreferenceId());
                return ResponseEntity.ok(response);
            } else {
                log.error("❌ [PAYMENT-BRICK-API] Error creating preference: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK-API] Unexpected error creating preference: {}", e.getMessage(), e);
            
            PaymentBrickPreferenceResponseDTO errorResponse = PaymentBrickPreferenceResponseDTO.builder()
                .status("error")
                .message("Internal server error")
                .build();
                
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Procesar pago del Payment Brick
     * POST /api/payments/brick/process
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPaymentBrickPayment(
            @Valid @RequestBody PaymentBrickProcessRequestDTO request) {
        
        log.info("🧱 [PAYMENT-BRICK-API] Processing payment - Method: {}, Amount: ${}", 
                request.getSelectedPaymentMethod(), request.getTransactionAmount());
        
        try {
            PaymentResponseDTO response = paymentBrickService.processPaymentBrickPayment(request);
            
            if ("approved".equals(response.getStatus())) {
                log.info("✅ [PAYMENT-BRICK-API] Payment processed successfully - Transaction ID: {}", response.getTransactionId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ [PAYMENT-BRICK-API] Payment failed: {}", response.getStatus());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK-API] Unexpected error processing payment: {}", e.getMessage(), e);
            
            PaymentResponseDTO errorResponse = PaymentResponseDTO.builder()
                .status("error")
                .build();
                
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtener configuración del Payment Brick
     * GET /api/payments/brick/config
     */
    @GetMapping("/config")
    public ResponseEntity<PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO> getPaymentBrickConfig() {
        
        log.info("🧱 [PAYMENT-BRICK-API] Getting configuration");
        
        try {
            PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO config = paymentBrickService.getPaymentBrickConfig();
            
            log.info("✅ [PAYMENT-BRICK-API] Configuration retrieved successfully");
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK-API] Error getting configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validar método de pago
     * GET /api/payments/brick/validate/{paymentMethodId}
     */
    @GetMapping("/validate/{paymentMethodId}")
    public ResponseEntity<Boolean> validatePaymentMethod(@PathVariable String paymentMethodId) {
        
        log.info("🧱 [PAYMENT-BRICK-API] Validating payment method: {}", paymentMethodId);
        
        try {
            boolean isEnabled = paymentBrickService.isPaymentMethodEnabled(paymentMethodId);
            
            log.info("✅ [PAYMENT-BRICK-API] Payment method {} validation: {}", paymentMethodId, isEnabled);
            return ResponseEntity.ok(isEnabled);
            
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK-API] Error validating payment method: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(false);
        }
    }

    /**
     * Obtener public key de MercadoPago para el frontend
     * GET /api/payments/brick/public-key
     */
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        
        log.info("🧱 [PAYMENT-BRICK-API] Getting public key");
        
        try {
            PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO config = paymentBrickService.getPaymentBrickConfig();
            
            // En una implementación real, deberías tener el public key en el servicio
            // Por ahora, usar una respuesta dummy
            String publicKey = "APP_USR-df680b3d-32c3-4b5e-b692-5009ccc21bd3"; // Tu public key de producción
            
            log.info("✅ [PAYMENT-BRICK-API] Public key retrieved successfully");
            return ResponseEntity.ok(publicKey);
            
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK-API] Error getting public key: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 