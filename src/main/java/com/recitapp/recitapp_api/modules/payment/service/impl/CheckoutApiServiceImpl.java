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
import com.recitapp.recitapp_api.modules.payment.enums.MercadoPagoPaymentStatus;
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
                return createErrorResponse("Token de tarjeta inválido", null);
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
            
            // Determinar estado usando el nuevo enum
            MercadoPagoPaymentStatus paymentStatus = MercadoPagoPaymentStatus.determineStatus(
                payment.getStatus(), payment.getStatusDetail());
            
            log.info("📊 [CHECKOUT_API] Estado determinado: {} - ShouldDeliverTickets: {}, CanRetry: {}", 
                    paymentStatus.getCode(), paymentStatus.isShouldDeliverTickets(), paymentStatus.isCanRetry());
            
            // Procesar según estado del pago
            return handlePaymentByStatus(payment, paymentRequest, paymentStatus);
            
        } catch (MPApiException e) {
            log.error("❌ [CHECKOUT_API] Error de API de MercadoPago: {}", e.getMessage(), e);
            return createErrorResponse("Error de procesamiento: " + e.getMessage(), null);
        } catch (MPException e) {
            log.error("❌ [CHECKOUT_API] Error de MercadoPago: {}", e.getMessage(), e);
            return createErrorResponse("Error de conexión con MercadoPago", null);
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error inesperado: {}", e.getMessage(), e);
            return createErrorResponse("Error interno del servidor", null);
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
            
            // Determinar estado usando el nuevo enum
            MercadoPagoPaymentStatus paymentStatus = MercadoPagoPaymentStatus.determineStatus(
                payment.getStatus(), payment.getStatusDetail());
            
            // Procesar según estado del pago
            return handleWalletPaymentByStatus(payment, paymentRequest, paymentStatus);
            
        } catch (Exception e) {
            log.error("❌ [CHECKOUT_API] Error en pago con wallet: {}", e.getMessage(), e);
            return createErrorResponse("Error procesando pago con dinero en cuenta", null);
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
    
    // Métodos auxiliares actualizados
    
    private PaymentResponseDTO handlePaymentByStatus(Payment payment, CheckoutApiPaymentRequestDTO paymentRequest, 
                                                   MercadoPagoPaymentStatus paymentStatus) {
        
        PaymentResponseDTO.PaymentResponseDTOBuilder responseBuilder = PaymentResponseDTO.builder()
            .preferenceId(payment.getId().toString())
            .paymentId(payment.getId().toString())
            .publicKey(publicKey)
            .totalAmount(paymentRequest.getTotalAmount())
            .status(paymentStatus.getStatus())
            .statusCode(paymentStatus.getCode())
            .statusDetail(payment.getStatusDetail())
            .displayName(paymentStatus.getDisplayName())
            .userMessage(paymentStatus.getUserMessage())
            .shouldDeliverTickets(paymentStatus.isShouldDeliverTickets())
            .canRetry(paymentStatus.isCanRetry())
            .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                .paymentMethodId(paymentRequest.getPaymentMethodId())
                .paymentTypeId(paymentRequest.getPaymentTypeId())
                .paymentMethodName(getPaymentMethodName(paymentRequest.getPaymentMethodId()))
                .build())
            .apiConfig(createApiConfiguration());
        
        // Solo procesar tickets si el pago fue aprobado
        if (paymentStatus.isShouldDeliverTickets()) {
            try {
                log.info("✅ [CHECKOUT_API] Pago aprobado - procesando tickets");
                
                // TODO: Por ahora solo loggeamos que el pago fue aprobado
                // La integración completa con tickets requiere información adicional
                // que no está disponible en el DTO actual de CheckoutApiPaymentRequestDTO
                log.info("🎫 [CHECKOUT_API] Pago aprobado correctamente - Payment ID: {}, Amount: {}", 
                        payment.getId(), paymentRequest.getTotalAmount());
                
                // Cuando se implemente completamente, aquí se creará la transacción y tickets
                // usando el servicio de transacciones que ya maneja la lógica completa
                
            } catch (Exception e) {
                log.error("❌ [CHECKOUT_API] Error inesperado procesando tickets: {}", e.getMessage(), e);
            }
        } else {
            log.info("⏳ [CHECKOUT_API] Pago no aprobado - no se procesan tickets. Estado: {}", paymentStatus.getCode());
        }
        
        return responseBuilder.build();
    }
    
    private PaymentResponseDTO handleWalletPaymentByStatus(Payment payment, PaymentRequestDTO paymentRequest, 
                                                         MercadoPagoPaymentStatus paymentStatus) {
        
        PaymentResponseDTO.PaymentResponseDTOBuilder responseBuilder = PaymentResponseDTO.builder()
            .preferenceId(payment.getId().toString())
            .paymentId(payment.getId().toString())
            .publicKey(publicKey)
            .totalAmount(paymentRequest.getTotalAmount())
            .status(paymentStatus.getStatus())
            .statusCode(paymentStatus.getCode())
            .statusDetail(payment.getStatusDetail())
            .displayName(paymentStatus.getDisplayName())
            .userMessage(paymentStatus.getUserMessage())
            .shouldDeliverTickets(paymentStatus.isShouldDeliverTickets())
            .canRetry(paymentStatus.isCanRetry())
            .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                .paymentMethodId("account_money")
                .paymentTypeId("account_money")
                .paymentMethodName("Dinero en cuenta de Mercado Pago")
                .build())
            .apiConfig(createApiConfiguration());
        
        // Solo procesar tickets si el pago fue aprobado
        if (paymentStatus.isShouldDeliverTickets()) {
            try {
                log.info("✅ [CHECKOUT_API] Pago con wallet aprobado - procesando tickets");
                
                // TODO: Similar al método anterior, por ahora solo loggeamos
                log.info("🎫 [CHECKOUT_API] Pago con wallet aprobado correctamente - Payment ID: {}, Amount: {}", 
                        payment.getId(), paymentRequest.getTotalAmount());
                
            } catch (Exception e) {
                log.error("❌ [CHECKOUT_API] Error procesando tickets con wallet: {}", e.getMessage(), e);
            }
        }
        
        return responseBuilder.build();
    }
    
    private PaymentResponseDTO createErrorResponse(String errorMessage, String paymentId) {
        return PaymentResponseDTO.builder()
            .status("ERROR")
            .statusCode("ERROR")
            .displayName("Error de procesamiento")
            .userMessage(errorMessage)
            .shouldDeliverTickets(false)
            .canRetry(true)
            .paymentId(paymentId)
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
        return switch (paymentMethodId != null ? paymentMethodId.toLowerCase() : "") {
            case "visa" -> "Visa";
            case "master" -> "Mastercard";
            case "amex" -> "American Express";
            case "naranja" -> "Naranja";
            case "maestro" -> "Maestro";
            case "cabal" -> "Cabal";
            case "account_money" -> "Dinero en cuenta de Mercado Pago";
            default -> "Tarjeta";
        };
    }
} 