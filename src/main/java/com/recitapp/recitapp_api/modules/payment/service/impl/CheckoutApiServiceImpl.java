package com.recitapp.recitapp_api.modules.payment.service.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.payment.PaymentAdditionalInfoRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPApiException;

import com.recitapp.recitapp_api.modules.payment.dto.CheckoutApiPaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.CheckoutApiService;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutApiServiceImpl implements CheckoutApiService {
    
    @Value("${mercadopago.access.token}")
    private String accessToken;
    
    @Value("${mercadopago.public.key}")
    private String publicKey;
    
    @Value("${mercadopago.webhook.url:}")
    private String webhookUrl;
    
    private final TransactionService transactionService;
    private final TicketService ticketService;
    
    @Override
    public PaymentResponseDTO processCardPayment(CheckoutApiPaymentRequestDTO paymentRequest) {
        log.info("🚀 [CHECKOUT_API] Iniciando procesamiento de pago con tarjeta - Event: {}, Amount: {}", 
                paymentRequest.getEventId(), paymentRequest.getTotalAmount());
        
        try {
            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            
            // Validar token de tarjeta
            if (!paymentRequest.hasValidCardToken()) {
                log.error("❌ [CHECKOUT_API] Token de tarjeta inválido");
                return createErrorResponse("Token de tarjeta inválido");
            }
            
            // Crear referencia externa única
            String externalReference = "API_CARD_" + paymentRequest.getEventId() + 
                                     "_USER_" + paymentRequest.getUserId() + 
                                     "_" + UUID.randomUUID().toString();
            
            // Construir información del pagador
            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                .email(paymentRequest.getPayer().getEmail())
                .firstName(paymentRequest.getPayer().getFirstName())
                .lastName(paymentRequest.getPayer().getLastName())
                .build();
            
            // Información adicional para mejorar aprobación
            PaymentAdditionalInfoRequest additionalInfo = PaymentAdditionalInfoRequest.builder()
                .build();
            
            // Crear petición de pago
            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(paymentRequest.getTotalAmount())
                .token(paymentRequest.getCardToken())
                .description(paymentRequest.getDescription())
                .externalReference(externalReference)
                .paymentMethodId(paymentRequest.getPaymentMethodId())
                .installments(paymentRequest.getInstallments())
                .payer(payer)
                .additionalInfo(additionalInfo)
                .notificationUrl(webhookUrl)
                .build();
            
            log.info("💳 [CHECKOUT_API] Enviando pago con tarjeta a MercadoPago - Ref: {}", externalReference);
            
            // Procesar pago
            Payment payment = client.create(paymentCreateRequest);
            
            log.info("✅ [CHECKOUT_API] Pago procesado - ID: {}, Status: {}, Status Detail: {}", 
                    payment.getId(), payment.getStatus(), payment.getStatusDetail());
            
            // Procesar según estado del pago
            if ("approved".equals(payment.getStatus())) {
                return handleApprovedPayment(payment, paymentRequest);
            } else if ("pending".equals(payment.getStatus())) {
                return handlePendingPayment(payment, paymentRequest);
            } else {
                return handleRejectedPayment(payment, paymentRequest);
            }
            
        } catch (MPApiException e) {
            log.error("❌ [CHECKOUT_API] Error de API de MercadoPago: {}", e.getMessage(), e);
            return createErrorResponse("Error de procesamiento: " + e.getMessage());
        } catch (MPException e) {
            log.error("❌ [CHECKOUT_API] Error de MercadoPago: {}", e.getMessage(), e);
            return createErrorResponse("Error de conexión con MercadoPago");
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error inesperado: {}", e.getMessage(), e);
            return createErrorResponse("Error interno del servidor");
        }
    }
    
    @Override
    public PaymentResponseDTO processWalletPayment(PaymentRequestDTO paymentRequest) {
        log.info("🚀 [CHECKOUT_API] Iniciando pago con dinero en cuenta - Event: {}, Amount: {}", 
                paymentRequest.getEventId(), paymentRequest.getTotalAmount());
        
        try {
            // Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            
            // Crear referencia externa única
            String externalReference = "API_WALLET_" + paymentRequest.getEventId() + 
                                     "_USER_" + paymentRequest.getUserId() + 
                                     "_" + UUID.randomUUID().toString();
            
            // Construir información del pagador
            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                .email(paymentRequest.getPayer().getEmail())
                .firstName(paymentRequest.getPayer().getFirstName())
                .lastName(paymentRequest.getPayer().getLastName())
                .build();
            
            // Crear petición de pago con saldo en cuenta
            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(paymentRequest.getTotalAmount())
                .description("Compra de entradas - Evento ID: " + paymentRequest.getEventId())
                .externalReference(externalReference)
                .paymentMethodId("account_money")
                .payer(payer)
                .notificationUrl(webhookUrl)
                .build();
            
            log.info("💰 [CHECKOUT_API] Enviando pago con dinero en cuenta - Ref: {}", externalReference);
            
            // Procesar pago
            Payment payment = client.create(paymentCreateRequest);
            
            log.info("✅ [CHECKOUT_API] Pago con wallet procesado - ID: {}, Status: {}", 
                    payment.getId(), payment.getStatus());
            
            // Procesar según estado del pago
            if ("approved".equals(payment.getStatus())) {
                return handleApprovedWalletPayment(payment, paymentRequest);
            } else {
                return handleRejectedWalletPayment(payment, paymentRequest);
            }
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error en pago con wallet: {}", e.getMessage(), e);
            return createErrorResponse("Error procesando pago con dinero en cuenta");
        }
    }
    
    @Override
    public String getPaymentStatus(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            
            Payment payment = client.get(Long.parseLong(paymentId));
            return payment.getStatus();
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error obteniendo estado del pago {}: {}", paymentId, e.getMessage());
            return "error";
        }
    }
    
    @Override
    public String getPublicKey() {
        return publicKey;
    }
    
    // Métodos auxiliares
    
    private PaymentResponseDTO handleApprovedPayment(Payment payment, CheckoutApiPaymentRequestDTO paymentRequest) {
        try {
            log.info("✅ [CHECKOUT_API] Pago aprobado - procesando tickets");
            
            // Crear petición de compra de tickets
            TicketPurchaseRequestDTO ticketPurchaseRequest = TicketPurchaseRequestDTO.builder()
                .userId(paymentRequest.getUserId())
                .eventId(paymentRequest.getEventId())
                .paymentMethodId(1L) // ID para MercadoPago API
                .tickets(Collections.emptyList()) // Se completará según la lógica de negocio
                .build();
            
            // Procesar compra de tickets
            TicketPurchaseResponseDTO purchaseResponse = ticketService.purchaseTickets(ticketPurchaseRequest);
            
            return PaymentResponseDTO.builder()
                .preferenceId(payment.getId().toString())
                .publicKey(publicKey)
                .totalAmount(paymentRequest.getTotalAmount())
                .status("APPROVED")
                .transactionId(purchaseResponse.getTransactionId())
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodId(paymentRequest.getPaymentMethodId())
                    .paymentTypeId(paymentRequest.getPaymentTypeId())
                    .paymentMethodName(getPaymentMethodName(paymentRequest.getPaymentMethodId()))
                    .build())
                .apiConfig(createApiConfiguration())
                .build();
                
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error procesando tickets para pago aprobado: {}", e.getMessage());
            return createErrorResponse("Pago aprobado pero error procesando tickets");
        }
    }
    
    private PaymentResponseDTO handlePendingPayment(Payment payment, CheckoutApiPaymentRequestDTO paymentRequest) {
        log.info("⏳ [CHECKOUT_API] Pago pendiente - ID: {}, Detail: {}", 
                payment.getId(), payment.getStatusDetail());
        
        return PaymentResponseDTO.builder()
            .preferenceId(payment.getId().toString())
            .publicKey(publicKey)
            .totalAmount(paymentRequest.getTotalAmount())
            .status("PENDING")
            .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                .paymentMethodId(paymentRequest.getPaymentMethodId())
                .paymentTypeId(paymentRequest.getPaymentTypeId())
                .paymentMethodName(getPaymentMethodName(paymentRequest.getPaymentMethodId()))
                .build())
            .apiConfig(createApiConfiguration())
            .build();
    }
    
    private PaymentResponseDTO handleRejectedPayment(Payment payment, CheckoutApiPaymentRequestDTO paymentRequest) {
        log.warn("❌ [CHECKOUT_API] Pago rechazado - ID: {}, Detail: {}", 
                payment.getId(), payment.getStatusDetail());
        
        return PaymentResponseDTO.builder()
            .preferenceId(payment.getId().toString())
            .publicKey(publicKey)
            .totalAmount(paymentRequest.getTotalAmount())
            .status("REJECTED")
            .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                .paymentMethodId(paymentRequest.getPaymentMethodId())
                .paymentTypeId(paymentRequest.getPaymentTypeId())
                .paymentMethodName(getPaymentMethodName(paymentRequest.getPaymentMethodId()))
                .build())
            .apiConfig(createApiConfiguration())
            .build();
    }
    
    private PaymentResponseDTO handleApprovedWalletPayment(Payment payment, PaymentRequestDTO paymentRequest) {
        try {
            log.info("✅ [CHECKOUT_API] Pago con wallet aprobado - procesando tickets");
            
            // Procesar tickets similar al método anterior
            TicketPurchaseRequestDTO ticketPurchaseRequest = TicketPurchaseRequestDTO.builder()
                .userId(paymentRequest.getUserId())
                .eventId(paymentRequest.getEventId())
                .paymentMethodId(1L) // ID para MercadoPago Wallet
                .tickets(Collections.emptyList()) // Se completará según la lógica de negocio
                .build();
            
            TicketPurchaseResponseDTO purchaseResponse = ticketService.purchaseTickets(ticketPurchaseRequest);
            
            return PaymentResponseDTO.builder()
                .preferenceId(payment.getId().toString())
                .publicKey(publicKey)
                .totalAmount(paymentRequest.getTotalAmount())
                .status("APPROVED")
                .transactionId(purchaseResponse.getTransactionId())
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodId("account_money")
                    .paymentTypeId("account_money")
                    .paymentMethodName("Dinero en cuenta de Mercado Pago")
                    .build())
                .apiConfig(createApiConfiguration())
                .build();
                
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error procesando tickets para pago con wallet: {}", e.getMessage());
            return createErrorResponse("Pago aprobado pero error procesando tickets");
        }
    }
    
    private PaymentResponseDTO handleRejectedWalletPayment(Payment payment, PaymentRequestDTO paymentRequest) {
        log.warn("❌ [CHECKOUT_API] Pago con wallet rechazado - ID: {}, Detail: {}", 
                payment.getId(), payment.getStatusDetail());
        
        return PaymentResponseDTO.builder()
            .preferenceId(payment.getId().toString())
            .publicKey(publicKey)
            .totalAmount(paymentRequest.getTotalAmount())
            .status("REJECTED")
            .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                .paymentMethodId("account_money")
                .paymentTypeId("account_money")
                .paymentMethodName("Dinero en cuenta de Mercado Pago")
                .build())
            .apiConfig(createApiConfiguration())
            .build();
    }
    
    private PaymentResponseDTO createErrorResponse(String errorMessage) {
        return PaymentResponseDTO.builder()
            .status("ERROR")
            .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                .paymentMethodName(errorMessage)
                .build())
            .apiConfig(createApiConfiguration())
            .build();
    }
    
    private PaymentResponseDTO.ApiConfiguration createApiConfiguration() {
        return PaymentResponseDTO.ApiConfiguration.builder()
            .locale("es-AR")
            .theme("default")
            .enabledPaymentMethods(PaymentResponseDTO.EnabledPaymentMethods.builder()
                .creditCard(true)
                .debitCard(true)
                .mercadoPagoWallet(true)
                .build())
            .build();
    }
    
    private String getPaymentMethodName(String paymentMethodId) {
        switch (paymentMethodId.toLowerCase()) {
            case "visa": return "Visa";
            case "master": return "Mastercard";
            case "amex": return "American Express";
            case "cabal": return "Cabal";
            case "naranja": return "Naranja";
            case "account_money": return "Dinero en cuenta de Mercado Pago";
            default: return "Tarjeta de " + paymentMethodId;
        }
    }
} 