package com.recitapp.recitapp_api.modules.payment.controller;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.MercadoPagoService;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final MercadoPagoService mercadoPagoService;
    private final TicketService ticketService;
    private final TransactionService transactionService;

    @PostConstruct
    public void init() {
        System.out.println("🚀 PaymentController initialized successfully!");
        System.out.println("📍 Controller path: /api/payments");
        System.out.println("🔗 Available endpoints:");
        System.out.println("  - POST /api/payments/create-preference");
        System.out.println("  - POST /api/payments/process-payment");
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

    @PostMapping("/create-preference-wallet-only")
    public ResponseEntity<PaymentResponseDTO> createPaymentPreferenceWalletOnly(
            @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            // BYPASS DE SEGURIDAD: Establecer contexto anónimo para forzar que sea público
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
                    "anonymous", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
                SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
                System.out.println("🔓 SECURITY BYPASS: Set anonymous authentication for wallet-only payment endpoint");
            }
            
            System.out.println("=== WALLET-ONLY PAYMENT CONTROLLER ===");
            System.out.println("✅ Wallet-only payment endpoint reached successfully!");
            System.out.println("Event ID: " + paymentRequest.getEventId());
            System.out.println("User ID: " + paymentRequest.getUserId());
            System.out.println("Total Amount: " + paymentRequest.getTotalAmount());
            
            log.info("Creating wallet-only payment preference for event: {} and user: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId());
            
            PaymentResponseDTO response = mercadoPagoService.createPaymentPreferenceWalletOnly(paymentRequest);
            
            System.out.println("✅ Wallet-only payment preference created successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error in wallet-only payment controller: " + e.getMessage());
            log.error("Error creating wallet-only payment preference: {}", e.getMessage());
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

    @PostMapping("/process-payment")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Valid @RequestBody PaymentRequestDTO paymentRequest) 
    {
        try {
            log.info("Processing confirmed payment for event: {} and user: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId());
            
            PaymentResponseDTO response = mercadoPagoService.processConfirmedPayment(paymentRequest);
            
            log.info("Payment processed successfully and tickets created");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/wallet-purchase")
    public ResponseEntity<PaymentResponseDTO> processWalletPurchase(@RequestBody PaymentRequestDTO paymentRequest) {
        try {
            log.info("Processing wallet purchase for Event ID: {}, User ID: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId());
            
            // Build ticket purchase request with wallet payment method
            TicketPurchaseRequestDTO ticketPurchaseRequest = buildWalletTicketPurchaseRequest(paymentRequest);
            
            // Process purchase with automatic wallet discount
            TicketPurchaseResponseDTO purchaseResponse = ticketService.purchaseTickets(ticketPurchaseRequest);
            
            // Note: PDF generation and email sending would be handled by the service layer
            
            // Build response
            PaymentResponseDTO.BricksConfiguration bricksConfig = PaymentResponseDTO.BricksConfiguration.builder()
                .locale("es-AR")
                .theme("default")
                .paymentMethods(PaymentResponseDTO.PaymentMethods.builder()
                    .creditCard(false)
                    .debitCard(false)
                    .mercadoPagoWallet(false)
                    .cash(false)
                    .bankTransfer(false)
                    .build())
                .build();
            
            String status = purchaseResponse.getAmountAfterWallet().compareTo(BigDecimal.ZERO) == 0 ? 
                "COMPLETED" : "PARTIAL_WALLET_PAYMENT";
            
            return ResponseEntity.ok(PaymentResponseDTO.builder()
                .preferenceId("WALLET_" + purchaseResponse.getTransactionId())
                .initPoint(null)
                .sandboxInitPoint(null)
                .publicKey("WALLET_PAYMENT")
                .totalAmount(paymentRequest.getTotalAmount())
                .status(status)
                .qrCodeData(purchaseResponse.getTickets().get(0).getQrCode())
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodId("billetera_virtual")
                    .paymentTypeId("wallet")
                    .paymentMethodName("Billetera Virtual Recitapp")
                    .issuerName("RecitApp")
                    .build())
                .bricksConfig(bricksConfig)
                .walletDiscountApplied(purchaseResponse.getWalletDiscountApplied())
                .amountAfterWallet(purchaseResponse.getAmountAfterWallet())
                .walletMessage(purchaseResponse.getWalletMessage())
                .build());
            
        } catch (Exception e) {
            log.error("Error processing wallet purchase: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing wallet payment", e);
        }
    }
    
    private TicketPurchaseRequestDTO buildWalletTicketPurchaseRequest(PaymentRequestDTO paymentRequest) {
        log.info("Building wallet ticket purchase request from payment request");
        
        // Get wallet payment method ID dynamically
        Long walletPaymentMethodId = getWalletPaymentMethodId();
        
        List<TicketPurchaseRequestDTO.TicketRequestDTO> ticketRequests = new ArrayList<>();
        
        for (PaymentRequestDTO.TicketItemDTO ticketItem : paymentRequest.getTickets()) {
            // Create an entry for each requested quantity
            for (int i = 0; i < ticketItem.getQuantity(); i++) {
                TicketPurchaseRequestDTO.TicketRequestDTO ticketRequest = new TicketPurchaseRequestDTO.TicketRequestDTO();
                ticketRequest.setSectionId(ticketItem.getSectionId());
                ticketRequest.setAttendeeFirstName(ticketItem.getAttendeeFirstName());
                ticketRequest.setAttendeeLastName(ticketItem.getAttendeeLastName());
                ticketRequest.setAttendeeDni(ticketItem.getAttendeeDni());
                ticketRequest.setPrice(ticketItem.getPrice());
                
                // IMPORTANTE: Pasar información del tipo de ticket
                ticketRequest.setTicketPriceId(ticketItem.getTicketPriceId());
                ticketRequest.setTicketType(ticketItem.getTicketType());
                
                ticketRequests.add(ticketRequest);
            }
        }
        
        return TicketPurchaseRequestDTO.builder()
            .eventId(paymentRequest.getEventId())
            .paymentMethodId(walletPaymentMethodId)
            .userId(paymentRequest.getUserId())
            .tickets(ticketRequests)
            .build();
    }
    
    private Long getWalletPaymentMethodId() {
        try {
            // Get all active payment methods and find Wallet
            var paymentMethods = transactionService.getPaymentMethods(false);
            return paymentMethods.stream()
                .filter(pm -> "BILLETERA_VIRTUAL".equalsIgnoreCase(pm.getName()))
                .findFirst()
                .map(pm -> pm.getId())
                .orElseThrow(() -> new RuntimeException("Wallet payment method not found"));
        } catch (Exception e) {
            log.error("Error getting Wallet payment method ID: {}", e.getMessage());
            // Fallback to the actual wallet payment method ID from data.sql
            log.warn("Using fallback payment method ID for wallet. Please check your payment_methods table.");
            return 4L; // From data.sql: BILLETERA_VIRTUAL has ID 4
        }
    }
} 