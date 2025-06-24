package com.recitapp.recitapp_api.modules.payment.service.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferencePaymentMethodsRequest;
import com.mercadopago.client.preference.PreferencePaymentTypeRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.payment.PaymentAdditionalInfoRequest;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPApiException;

import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickPreferenceResponseDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentBrickProcessRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.PaymentBrickService;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketPdfService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketEmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentBrickServiceImpl implements PaymentBrickService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.public.key}")
    private String publicKey;

    @Value("${mercadopago.success.url}")
    private String successUrl;

    @Value("${mercadopago.failure.url}")
    private String failureUrl;

    @Value("${mercadopago.pending.url}")
    private String pendingUrl;

    @Value("${mercadopago.webhook.url}")
    private String webhookUrl;

    private final TransactionService transactionService;
    private final TicketService ticketService;
    private final TicketPdfService ticketPdfService;
    private final TicketEmailService ticketEmailService;

    @Override
    public PaymentBrickPreferenceResponseDTO createPaymentBrickPreference(PaymentBrickRequestDTO request) {
        try {
            log.info("🧱 [PAYMENT-BRICK] Creating preference - Event: {}, User: {}, Amount: ${}", 
                    request.getEventId(), request.getUserId(), request.getTotalAmount());
            
            // Validar request
            validatePaymentBrickRequest(request);
            
            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PreferenceClient client = new PreferenceClient();
            
            // Generar referencia externa única
            String externalReference = generateExternalReference(request.getEventId(), request.getUserId());
            
            // Crear items de la preferencia
            List<PreferenceItemRequest> items = createPreferenceItems(request);
            
            // Configurar pagador
            PreferencePayerRequest payer = createPreferencePayer(request);
            
            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();
            
            // Configurar métodos de pago permitidos
            PreferencePaymentMethodsRequest paymentMethods = createPaymentMethodsConfig(request);
            
            // Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .paymentMethods(paymentMethods)
                .notificationUrl(webhookUrl)
                .externalReference(externalReference)
                .purpose("wallet_purchase") // Permite login en MercadoPago con cualquier cuenta
                .autoReturn("approved")
                .statementDescriptor("RECITAPP")
                .expires(true)
                .build();
            
            Preference preference = client.create(preferenceRequest);
            
            log.info("✅ [PAYMENT-BRICK] Preference created successfully - ID: {}", preference.getId());
            
            return PaymentBrickPreferenceResponseDTO.builder()
                .preferenceId(preference.getId())
                .publicKey(publicKey)
                .amount(request.getTotalAmount())
                .currencyId("ARS")
                .externalReference(externalReference)
                .successUrl(successUrl)
                .failureUrl(failureUrl)
                .pendingUrl(pendingUrl)
                .webhookUrl(webhookUrl)
                .initPoint(preference.getInitPoint())
                .sandboxInitPoint(preference.getSandboxInitPoint())
                .status("success")
                .message("Preference created successfully")
                .paymentMethodsConfig(buildPaymentMethodsConfig(request))
                .build();
                
        } catch (MPException | MPApiException e) {
            log.error("❌ [PAYMENT-BRICK] Error creating preference: {}", e.getMessage(), e);
            return PaymentBrickPreferenceResponseDTO.builder()
                .status("error")
                .message("Error creating payment preference: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK] Unexpected error: {}", e.getMessage(), e);
            return PaymentBrickPreferenceResponseDTO.builder()
                .status("error")
                .message("Unexpected error occurred")
                .build();
        }
    }

    @Override
    public PaymentResponseDTO processPaymentBrickPayment(PaymentBrickProcessRequestDTO request) {
        try {
            log.info("🧱 [PAYMENT-BRICK] Processing payment - Method: {}, Amount: ${}", 
                    request.getSelectedPaymentMethod(), request.getTransactionAmount());
            
            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            
            // Crear request de pago
            PaymentCreateRequest paymentRequest = buildPaymentRequest(request);
            
            // Procesar pago
            Payment payment = client.create(paymentRequest);
            
            log.info("💳 [PAYMENT-BRICK] Payment processed - ID: {}, Status: {}", 
                    payment.getId(), payment.getStatus());
            
            // Procesar el resultado del pago
            return processPaymentResult(payment, request);
            
        } catch (MPException | MPApiException e) {
            log.error("❌ [PAYMENT-BRICK] Error processing payment: {}", e.getMessage(), e);
            return PaymentResponseDTO.builder()
                .status("error")
                .build();
        } catch (Exception e) {
            log.error("💥 [PAYMENT-BRICK] Unexpected error: {}", e.getMessage(), e);
            return PaymentResponseDTO.builder()
                .status("error")
                .build();
        }
    }

    @Override
    public PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO getPaymentBrickConfig() {
        return PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO.builder()
            .creditCard(true)
            .debitCard(true)
            .mercadoPagoWallet(true)
            .excludedPaymentTypes(new String[]{"ticket", "bank_transfer", "atm"})
            .maxInstallments(12)
            .build();
    }

    @Override
    public boolean isPaymentMethodEnabled(String paymentMethodId) {
        // Métodos permitidos para Payment Brick
        List<String> enabledMethods = Arrays.asList(
            "credit_card", "debit_card", "account_money", 
            "visa", "master", "amex", "naranja", "cabal", "maestro"
        );
        return enabledMethods.contains(paymentMethodId);
    }

    // Métodos privados de apoyo
    
    private void validatePaymentBrickRequest(PaymentBrickRequestDTO request) {
        if (request.getEventId() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("Event ID and User ID are required");
        }
        
        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            throw new IllegalArgumentException("At least one ticket is required");
        }
        
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than 0");
        }
    }
    
    private String generateExternalReference(Long eventId, Long userId) {
        return String.format("BRICK-%d-%d-%s", eventId, userId, UUID.randomUUID().toString().substring(0, 8));
    }
    
    private List<PreferenceItemRequest> createPreferenceItems(PaymentBrickRequestDTO request) {
        return request.getTickets().stream()
            .filter(ticket -> ticket.getPrice() != null && ticket.getPrice().compareTo(BigDecimal.ZERO) > 0)
            .map(ticket -> {
                String itemTitle = String.format("Entrada %s - Evento %d", 
                    ticket.getTicketType(), request.getEventId());
                
                return PreferenceItemRequest.builder()
                    .id(String.valueOf(ticket.getTicketPriceId()))
                    .title(itemTitle)
                    .description("Entrada para evento")
                    .categoryId("tickets")
                    .quantity(ticket.getQuantity())
                    .currencyId("ARS")
                    .unitPrice(ticket.getPrice())
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private PreferencePayerRequest createPreferencePayer(PaymentBrickRequestDTO request) {
        if (request.getPayer() != null && request.getPayer().getEmail() != null) {
            return PreferencePayerRequest.builder()
                .email(request.getPayer().getEmail())
                .name(request.getPayer().getFirstName())
                .surname(request.getPayer().getLastName())
                .build();
        }
        return null;
    }
    
    private PreferencePaymentMethodsRequest createPaymentMethodsConfig(PaymentBrickRequestDTO request) {
        List<PreferencePaymentTypeRequest> excludedPaymentTypes = new ArrayList<>();
        
        // Excluir métodos según configuración
        PaymentBrickRequestDTO.PaymentConfigDTO config = request.getPaymentConfig();
        if (config != null) {
            if (!config.getTicket()) {
                excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("ticket").build());
            }
            if (!config.getBankTransfer()) {
                excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("bank_transfer").build());
                excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("atm").build());
            }
        } else {
            // Configuración por defecto: solo tarjetas y wallet
            excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("ticket").build());
            excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("bank_transfer").build());
            excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("atm").build());
        }
        
        return PreferencePaymentMethodsRequest.builder()
            .excludedPaymentTypes(excludedPaymentTypes)
            .installments(config != null ? config.getMaxInstallments() : 12)
            .build();
    }
    
    private PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO buildPaymentMethodsConfig(PaymentBrickRequestDTO request) {
        PaymentBrickRequestDTO.PaymentConfigDTO config = request.getPaymentConfig();
        
        return PaymentBrickPreferenceResponseDTO.PaymentMethodsConfigDTO.builder()
            .creditCard(config != null ? config.getCreditCard() : true)
            .debitCard(config != null ? config.getDebitCard() : true)
            .mercadoPagoWallet(config != null ? config.getMercadoPagoWallet() : true)
            .excludedPaymentTypes(new String[]{"ticket", "bank_transfer", "atm"})
            .maxInstallments(config != null ? config.getMaxInstallments() : 12)
            .build();
    }
    
    private PaymentCreateRequest buildPaymentRequest(PaymentBrickProcessRequestDTO request) {
        return PaymentCreateRequest.builder()
            .transactionAmount(request.getTransactionAmount())
            .token(request.getToken())
            .description("Compra de entradas en RecitApp")
            .installments(request.getInstallments() != null ? request.getInstallments() : 1)
            .paymentMethodId(request.getPaymentMethodId())
            .payer(PaymentPayerRequest.builder()
                .email(request.getPayer().getEmail())
                .firstName(request.getPayer().getFirstName())
                .lastName(request.getPayer().getLastName())
                .build())
            .externalReference(request.getExternalReference())
            .build();
    }
    
    private PaymentResponseDTO processPaymentResult(Payment payment, PaymentBrickProcessRequestDTO request) {
        if ("approved".equals(payment.getStatus())) {
            log.info("✅ [PAYMENT-BRICK] Payment approved - Processing tickets");
            
            // Aquí procesar la compra de tickets
            // Similar a como se hace en MercadoPagoServiceImpl
            
            return PaymentResponseDTO.builder()
                .status("approved")
                .transactionId(payment.getId())
                .publicKey(publicKey)
                .build();
        } else {
            log.warn("⚠️ [PAYMENT-BRICK] Payment not approved - Status: {}", payment.getStatus());
            
            return PaymentResponseDTO.builder()
                .status(payment.getStatus())
                .transactionId(payment.getId())
                .build();
        }
    }
} 