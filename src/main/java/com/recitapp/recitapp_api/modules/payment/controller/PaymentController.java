package com.recitapp.recitapp_api.modules.payment.controller;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final MercadoPagoService mercadoPagoService;

    @PostConstruct
    public void init() {
        System.out.println("🚀 PaymentController initialized successfully!");
        System.out.println("📍 Controller path: /api/payments");
        System.out.println("🔗 Available endpoints:");
        System.out.println("  - POST /api/payments/create-preference");
        System.out.println("  - POST /api/payments/webhook");
        System.out.println("  - GET /api/payments/status/{paymentId}");
        System.out.println("  - GET /api/payments/public-key");
        log.info("PaymentController ready to handle requests");
    }

    @PostMapping("/create-preference")
    public ResponseEntity<PaymentResponseDTO> createPaymentPreference(
            @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            // BYPASS DE SEGURIDAD: Establecer contexto anónimo para forzar que sea público
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
                    "anonymous", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
                SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
                System.out.println("🔓 SECURITY BYPASS: Set anonymous authentication for payment endpoint");
            }
            
            System.out.println("=== PAYMENT CONTROLLER ===");
            System.out.println("✅ Payment endpoint reached successfully!");
            System.out.println("Event ID: " + paymentRequest.getEventId());
            System.out.println("User ID: " + paymentRequest.getUserId());
            System.out.println("Total Amount: " + paymentRequest.getTotalAmount());
            
            log.info("Creating payment preference for event: {} and user: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId());
            
            PaymentResponseDTO response = mercadoPagoService.createPaymentPreference(paymentRequest);
            
            System.out.println("✅ Payment preference created successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error in payment controller: " + e.getMessage());
            log.error("Error creating payment preference: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestParam Map<String, String> params,
            @RequestBody String payload) {
        
        try {
            mercadoPagoService.processWebhookPayment(params, payload);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String paymentId) {
        try {
            String status = mercadoPagoService.getPaymentStatus(paymentId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        try {
            String publicKey = mercadoPagoService.getPublicKey();
            return ResponseEntity.ok(publicKey);
        } catch (Exception e) {
            log.error("Error getting public key: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }
} 