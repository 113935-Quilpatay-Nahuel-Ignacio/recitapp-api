package com.recitapp.recitapp_api.modules.payment.service.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferencePaymentMethodsRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPApiException;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentRequestDTO;
import com.recitapp.recitapp_api.modules.payment.dto.PaymentResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.MercadoPagoService;
import com.recitapp.recitapp_api.modules.transaction.dto.TransactionDTO;
import com.recitapp.recitapp_api.modules.transaction.dto.TransactionDetailDTO;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketPdfService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketEmailService;
import com.recitapp.recitapp_api.modules.transaction.dto.PaymentMethodDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoServiceImpl implements MercadoPagoService {

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
    public PaymentResponseDTO createPaymentPreference(PaymentRequestDTO paymentRequest) {
        try {
            log.info("🚀 [MERCADOPAGO] Creating payment preference - Event: {}, User: {}, Amount: ${}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId(), paymentRequest.getTotalAmount());
            
            // Log payment request details
            log.debug("📋 [MERCADOPAGO] Payment request details - Tickets count: {}, Payer: {}", 
                    paymentRequest.getTickets() != null ? paymentRequest.getTickets().size() : 0,
                    paymentRequest.getPayer() != null ? paymentRequest.getPayer().getEmail() : "No payer");

            // IMPORTANTE: Para incluir saldo de MercadoPago junto con tarjetas, NO usar purpose: "wallet_purchase"
            // El modo estándar automáticamente incluye: tarjetas + saldo MP + otros métodos
            log.info("💳 [MERCADOPAGO] UNIFIED mode - All payment methods including MercadoPago Wallet available");

            // Validar parámetros
            if (paymentRequest.getEventId() == null || paymentRequest.getUserId() == null) {
                throw new IllegalArgumentException("Event ID and User ID are required");
            }

            if (paymentRequest.getTickets() == null || paymentRequest.getTickets().isEmpty()) {
                throw new IllegalArgumentException("At least one ticket is required");
            }

            if (paymentRequest.getTotalAmount() == null || paymentRequest.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Total amount must be greater than 0");
            }

            // Configurar cliente de MercadoPago
            MercadoPagoConfig.setAccessToken(accessToken);
            PreferenceClient client = new PreferenceClient();

            // Separar tickets de pago y gratuitos
            List<PaymentRequestDTO.TicketItemDTO> paidTickets = paymentRequest.getTickets().stream()
                .filter(ticket -> ticket.getPrice() != null && ticket.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
                
            List<PaymentRequestDTO.TicketItemDTO> giftTickets = paymentRequest.getTickets().stream()
                .filter(ticket -> ticket.getPrice() == null || ticket.getPrice().compareTo(BigDecimal.ZERO) <= 0)
                .collect(Collectors.toList());
                
            log.info("Processing {} paid tickets and {} gift tickets", paidTickets.size(), giftTickets.size());
            
            // Log detailed ticket information
            for (PaymentRequestDTO.TicketItemDTO ticket : paidTickets) {
                log.debug("💰 [MERCADOPAGO] Paid ticket - Type: {}, Price: ${}, Quantity: {}, Total: ${}", 
                        ticket.getTicketType(), ticket.getPrice(), ticket.getQuantity(), 
                        ticket.getPrice().multiply(BigDecimal.valueOf(ticket.getQuantity())));
            }
            
            for (PaymentRequestDTO.TicketItemDTO ticket : giftTickets) {
                log.debug("🎁 [MERCADOPAGO] Gift ticket - Type: {}, Quantity: {}", 
                        ticket.getTicketType(), ticket.getQuantity());
            }
            
            // Si solo hay entradas gratuitas, no crear preferencia de MercadoPago
            if (paidTickets.isEmpty()) {
                log.info("Only gift tickets found, processing without MercadoPago");
                return processGiftTicketsOnly(paymentRequest);
            }
            
            // Calcular descuento proporcional si aplica
            BigDecimal originalPaidTotal = paidTickets.stream()
                .map(ticket -> ticket.getPrice().multiply(BigDecimal.valueOf(ticket.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            BigDecimal requestedAmount = paymentRequest.getTotalAmount();
            BigDecimal discountRatio = BigDecimal.ONE;
            
            if (originalPaidTotal.compareTo(requestedAmount) > 0 && requestedAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Hay un descuento aplicado (probablemente billetera virtual)
                discountRatio = requestedAmount.divide(originalPaidTotal, 4, RoundingMode.HALF_UP);
                log.info("💸 [MERCADOPAGO] Wallet discount detected - Original: ${}, Requested: ${}, Discount: {}%", 
                    originalPaidTotal, requestedAmount, 
                    BigDecimal.ONE.subtract(discountRatio).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
                log.debug("🧮 [MERCADOPAGO] Discount ratio calculated: {}", discountRatio);
            } else {
                log.debug("💯 [MERCADOPAGO] No discount applied - Full amount: ${}", originalPaidTotal);
            }
            
            final BigDecimal finalDiscountRatio = discountRatio;
            
            // Crear items de la preferencia solo para entradas de pago
            List<PreferenceItemRequest> items = paidTickets.stream()
                .map((PaymentRequestDTO.TicketItemDTO ticket) -> {
                    log.debug("Creating item for ticket: type={}, price={}, quantity={}", 
                            ticket.getTicketType(), ticket.getPrice(), ticket.getQuantity());
                    
                    // Las validaciones ya no son necesarias porque filtramos arriba
                    // pero las mantenemos por seguridad
                    if (ticket.getPrice() == null || ticket.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Ticket price must be greater than 0");
                    }
                    
                    if (ticket.getQuantity() == null || ticket.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Ticket quantity must be greater than 0");
                    }
                    
                    String itemTitle = String.format("Entrada %s - %s", 
                                                    ticket.getTicketType() != null ? ticket.getTicketType() : "General",
                                                    getEventName(paymentRequest.getEventId()));
                    
                    String itemDescription = String.format("Entrada tipo %s para el evento", 
                                                          ticket.getTicketType() != null ? ticket.getTicketType() : "General");
                    
                    // Validar que el título no sea muy largo (límite de MercadoPago)
                    if (itemTitle.length() > 256) {
                        itemTitle = itemTitle.substring(0, 253) + "...";
                    }
                    
                    if (itemDescription.length() > 600) {
                        itemDescription = itemDescription.substring(0, 597) + "...";
                    }
                    
                    // Aplicar descuento proporcional al precio del item si es necesario
                    BigDecimal adjustedPrice = ticket.getPrice().multiply(finalDiscountRatio);
                    // Redondear a 2 decimales para evitar problemas con centavos
                    adjustedPrice = adjustedPrice.setScale(2, RoundingMode.HALF_UP);
                    
                    log.debug("Item price adjustment: original={}, adjusted={}, ratio={}", 
                        ticket.getPrice(), adjustedPrice, finalDiscountRatio);
                    
                    return PreferenceItemRequest.builder()
                        .id(String.valueOf(ticket.getTicketPriceId() != null ? ticket.getTicketPriceId() : "1"))
                        .title(itemTitle)
                        .description(itemDescription)
                        .pictureUrl(null) // Podrías agregar imagen del evento aquí
                        .categoryId("tickets")
                        .quantity(ticket.getQuantity())
                        .currencyId("ARS")
                        .unitPrice(adjustedPrice) // Usar precio ajustado con descuento proporcional
                        .build();
                })
                .collect(Collectors.toList());

            log.info("📦 [MERCADOPAGO] Created {} preference items for processing", items.size());
            
            // Log each item created
            for (int i = 0; i < items.size(); i++) {
                PreferenceItemRequest item = items.get(i);
                log.debug("📋 [MERCADOPAGO] Item {}: {} x{} @ ${} each", 
                        i + 1, item.getTitle(), item.getQuantity(), item.getUnitPrice());
            }

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

            log.info("🔗 [MERCADOPAGO] Configured redirect URLs - Success: {}, Failure: {}, Pending: {}", 
                    successUrl, failureUrl, pendingUrl);

            // Configurar pagador para permitir selección libre de cuenta MercadoPago
            PreferencePayerRequest payer = null;
            
            // ✅ SOLUCIÓN: NO configurar email específico para permitir selección libre
            // Esto permite que el usuario elija cualquier cuenta de MercadoPago al pagar
            if (paymentRequest.getPayer() != null) {
                log.info("🆔 [MERCADOPAGO] Configuring payer for FREE account selection (no email restriction)");
                
                try {
                    // Configuración básica del pagador SIN email para máxima flexibilidad
                    payer = PreferencePayerRequest.builder()
                        .name(paymentRequest.getPayer().getFirstName() != null ? 
                              paymentRequest.getPayer().getFirstName() : "")
                        .surname(paymentRequest.getPayer().getLastName() != null ? 
                                paymentRequest.getPayer().getLastName() : "")
                        // ✅ NO configurar .email() - esto permite selección libre de cuenta
                        .build();
                    
                    log.info("✅ [MERCADOPAGO] Payer configured for free account selection - User can choose any MercadoPago account");
                    
                } catch (Exception e) {
                    log.warn("⚠️ [MERCADOPAGO] Error configuring payer, using minimal config: {}", e.getMessage());
                    // Configuración mínima sin restricciones
                    payer = PreferencePayerRequest.builder().build();
                }
            } else {
                log.info("🔓 [MERCADOPAGO] No payer restrictions - Full account selection freedom");
                // Sin configuración de payer = máxima libertad de selección
                payer = null;
            }

            String externalReference = "EVENTO_" + paymentRequest.getEventId() + "_USER_" + paymentRequest.getUserId() + "_" + UUID.randomUUID().toString();
            log.info("🆔 [MERCADOPAGO] Generated external reference: {}", externalReference);

            // Configurar métodos de pago explícitamente para asegurar que dinero en cuenta esté disponible
            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                // NO excluir account_money/available_money para permitir saldo de cuenta MercadoPago
                .excludedPaymentMethods(Collections.emptyList())
                .excludedPaymentTypes(Collections.emptyList())
                .installments(24) // Máximo de cuotas
                .build();
                
            log.info("💳 [MERCADOPAGO] Payment methods configured to include ALL options: cards, account money, and more");

            // Crear preferencia UNIFICADA que incluye TODOS los métodos incluyendo saldo MercadoPago
            // IMPORTANTE: NO usar "purpose": "wallet_purchase" para permitir TODAS las opciones
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .payer(payer)
                .externalReference(externalReference)
                .notificationUrl(webhookUrl)
                .paymentMethods(paymentMethods) // Configuración explícita de métodos de pago
                .expires(false)
                // SIN purpose = permite tarjetas + saldo MP + otros métodos automáticamente
                .build();
                
            log.info("✅ [MERCADOPAGO] Unified preference configured with ALL payment methods including MercadoPago Wallet");

            log.debug("Sending preference request to MercadoPago API...");
            Preference preference = client.create(preferenceRequest);

            log.info("Payment preference created successfully: {}", preference.getId());

            // Configuración para Checkout Bricks
            PaymentResponseDTO.BricksConfiguration bricksConfig = PaymentResponseDTO.BricksConfiguration.builder()
                .locale("es-AR")
                .theme("default")
                .paymentMethods(PaymentResponseDTO.PaymentMethods.builder()
                    .creditCard(true)
                    .debitCard(true)
                    .mercadoPagoWallet(true)
                    .cash(true)
                    .bankTransfer(true)
                    .build())
                .build();

            // Calcular el total final de la preferencia
            BigDecimal calculatedTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Verificar si hay diferencia de redondeo y ajustar el último item si es necesario
            BigDecimal difference = requestedAmount.subtract(calculatedTotal);
            BigDecimal finalPreferenceAmount = calculatedTotal;
            
            if (difference.abs().compareTo(new BigDecimal("0.10")) <= 0 && !items.isEmpty()) {
                // Si hay una pequeña diferencia (≤ 10 centavos), ajustar el último item
                PreferenceItemRequest lastItem = items.get(items.size() - 1);
                BigDecimal adjustedPrice = lastItem.getUnitPrice().add(difference);
                
                // Recrear el último item con precio ajustado
                items.set(items.size() - 1, PreferenceItemRequest.builder()
                    .id(lastItem.getId())
                    .title(lastItem.getTitle())
                    .description(lastItem.getDescription())
                    .pictureUrl(lastItem.getPictureUrl())
                    .categoryId(lastItem.getCategoryId())
                    .quantity(lastItem.getQuantity())
                    .currencyId(lastItem.getCurrencyId())
                    .unitPrice(adjustedPrice.setScale(2, RoundingMode.HALF_UP))
                    .build());
                    
                finalPreferenceAmount = requestedAmount;
                log.debug("Adjusted last item price by {} to match requested amount exactly", difference);
            }
                
            log.info("MercadoPago preference created. Original paid total: {}, Requested amount: {}, Final preference amount: {}, Gift tickets count: {}", 
                    originalPaidTotal, requestedAmount, finalPreferenceAmount, giftTickets.size());
                    
            // Si hay entradas gratuitas junto con las de pago, se procesarán después del pago
            if (!giftTickets.isEmpty()) {
                log.info("Mixed payment detected: {} paid tickets and {} gift tickets will be processed after payment", 
                        paidTickets.size(), giftTickets.size());
            }

            return PaymentResponseDTO.builder()
                // Campos para Checkout Pro (compatibilidad)
                .preferenceId(preference.getId())
                .initPoint(preference.getInitPoint())
                .sandboxInitPoint(preference.getSandboxInitPoint())
                // Campos para Checkout Bricks
                .publicKey(publicKey)
                .totalAmount(finalPreferenceAmount) // Usar el total de la preferencia (con descuentos aplicados proporcionalmente)
                .status("CREATED")
                .qrCodeData(null)
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodId("mercadopago")
                    .paymentTypeId("digital_wallet")
                    .paymentMethodName("MercadoPago")
                    .issuerName(null)
                    .build())
                .bricksConfig(bricksConfig)
                .build();

        } catch (MPException e) {
            log.error("MercadoPago SDK Exception:");
            log.error("  - Message: {}", e.getMessage());
            log.error("  - Type: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("  - Cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Error creating payment preference: " + e.getMessage(), e);
        } catch (MPApiException e) {
            log.error("MercadoPago API Exception:");
            log.error("  - Message: {}", e.getMessage());
            log.error("  - Status Code: {}", e.getStatusCode());
            
            // Mejorar el logging de la respuesta
            if (e.getApiResponse() != null) {
                try {
                    String responseContent = e.getApiResponse().getContent();
                    log.error("  - Response Content: {}", responseContent);
                    
                    Integer statusCode = e.getApiResponse().getStatusCode();
                    log.error("  - HTTP Status Code: {}", statusCode);
                    
                    // También mostrar headers si están disponibles
                    if (e.getApiResponse().getHeaders() != null) {
                        log.error("  - Response Headers: {}", e.getApiResponse().getHeaders());
                    }
                } catch (Exception responseException) {
                    log.error("  - Error reading response details: {}", responseException.getMessage());
                    log.error("  - Raw Response Object: {}", e.getApiResponse());
                }
            } else {
                log.error("  - No API Response available");
            }
            
            if (e.getCause() != null) {
                log.error("  - Cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("MercadoPago API Error (Status: " + e.getStatusCode() + "): " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating MercadoPago preference: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating payment preference", e);
        }
    }

    @Override
    public void processWebhookPayment(Map<String, String> params, String payload) {
        try {
            log.info("🔔 [MERCADOPAGO-WEBHOOK] Processing webhook notification");
            log.info("📥 [MERCADOPAGO-WEBHOOK] Params received: {}", params);
            log.debug("📄 [MERCADOPAGO-WEBHOOK] Full payload: {}", payload);
            
            String type = params.get("type");
            String dataId = params.get("data.id");
            
            log.info("🎯 [MERCADOPAGO-WEBHOOK] Notification type: {}, Data ID: {}", type, dataId);
            
            if ("payment".equals(type) && dataId != null) {
                log.info("💳 [MERCADOPAGO-WEBHOOK] Processing payment notification for ID: {}", dataId);
                
                // Obtener información detallada del pago
                try {
                    PaymentClient paymentClient = new PaymentClient();
                    Payment payment = paymentClient.get(Long.parseLong(dataId));
                    
                    // Extraer información del método de pago
                    String paymentMethodId = payment.getPaymentMethodId();
                    String paymentTypeId = payment.getPaymentTypeId();
                    String paymentMethodName = getPaymentMethodName(paymentMethodId);
                    String issuerName = payment.getIssuerId() != null ? payment.getIssuerId() : null;
                    String externalReference = payment.getExternalReference();
                    String paymentStatus = payment.getStatus();
                    
                    log.info("📊 [MERCADOPAGO-WEBHOOK] Payment details - Method: {}, Type: {}, Name: {}, Issuer: {}, Status: {}", 
                            paymentMethodId, paymentTypeId, paymentMethodName, issuerName, paymentStatus);
                    log.info("🆔 [MERCADOPAGO-WEBHOOK] External Reference: {}", externalReference);
                    log.info("💰 [MERCADOPAGO-WEBHOOK] Payment Amount: ${}", payment.getTransactionAmount());
                    
                    // Actualizar la transacción con el payment ID real de MercadoPago
                    if (externalReference != null && "approved".equals(paymentStatus)) {
                        log.info("✅ [MERCADOPAGO-WEBHOOK] Payment approved - Updating transaction with payment ID: {}", dataId);
                        boolean updated = updateTransactionWithPaymentId(externalReference, dataId, paymentMethodName);
                        
                        if (updated) {
                            log.info("🎉 [MERCADOPAGO-WEBHOOK] Transaction successfully updated with payment ID: {}", dataId);
                        } else {
                            log.warn("⚠️ [MERCADOPAGO-WEBHOOK] Could not find transaction with external reference: {} to update", externalReference);
                        }
                    } else if (externalReference != null && !"approved".equals(paymentStatus)) {
                        log.warn("❌ [MERCADOPAGO-WEBHOOK] Payment not approved - Status: {}, External Reference: {}", paymentStatus, externalReference);
                    }
                    
                } catch (MPException | MPApiException e) {
                    log.error("Error getting payment details: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing webhook notification", e);
        }
    }

    /**
     * Updates the transaction with the real MercadoPago payment ID received from webhook
     */
    private boolean updateTransactionWithPaymentId(String externalReference, String paymentId, String paymentMethodName) {
        try {
            // Find transaction by external reference
            TransactionDTO transaction = transactionService.findByExternalReference(externalReference);
            
            if (transaction != null) {
                // Update transaction with MercadoPago payment ID
                // We'll store it in a way that extractMercadoPagoPaymentId can find it
                // Option 1: Store in external reference as "ORIGINAL_REF|MP_PAYMENT_ID"
                String updatedExternalRef = externalReference + "|" + paymentId;
                transactionService.updateExternalReference(transaction.getId(), updatedExternalRef);
                
                log.info("Updated transaction {} with MercadoPago payment ID: {}", transaction.getId(), paymentId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error updating transaction with payment ID: {}", e.getMessage(), e);
            return false;
        }
    }

    private String getPaymentMethodName(String paymentMethodId) {
        // Mapear IDs de métodos de pago a nombres legibles
        switch (paymentMethodId) {
            case "visa": return "Visa";
            case "master": return "Mastercard";
            case "amex": return "American Express";
            case "account_money": return "Dinero en cuenta de MercadoPago";
            case "rapipago": return "Rapipago";
            case "pagofacil": return "Pago Fácil";
            case "redlink": return "RedLink";
            default: return "MercadoPago";
        }
    }

    @Override
    public String getPaymentStatus(String paymentId) {
        try {
            // En una implementación real, consultarías el estado del pago via API
            log.info("Getting payment status for payment ID: {}", paymentId);
            
            // Simulación temporal - en producción usar PaymentClient
            if (paymentId.startsWith("MP_")) {
                return "approved"; // Simulación para pagos de prueba
            }
            
            // Implementar consulta real aquí:
            // PaymentClient client = new PaymentClient();
            // Payment payment = client.get(Long.parseLong(paymentId));
            // return payment.getStatus();
            
            return "pending";
            
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting payment status", e);
        }
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    private String getEventName(Long eventId) {
        // Implementar lógica para obtener el nombre del evento
        // Por ahora retornar un placeholder
        return "Evento ID: " + eventId;
    }

    @Override
    public PaymentResponseDTO processConfirmedPayment(PaymentRequestDTO paymentRequest) {
        try {
            log.info("Processing confirmed payment for Event ID: {}, User ID: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId());
            
            // 1. Use the TicketService to purchase tickets (this creates both tickets and transaction)
            TicketPurchaseRequestDTO ticketPurchaseRequest = buildTicketPurchaseRequest(paymentRequest);
            TicketPurchaseResponseDTO purchaseResponse = ticketService.purchaseTickets(ticketPurchaseRequest);
            
            // 2. Generar PDFs y enviar emails para cada ticket
            for (TicketDTO ticket : purchaseResponse.getTickets()) {
                generateTicketPDF(ticket);
                sendTicketByEmail(ticket, paymentRequest.getPayer().getEmail());
            }
            
            // 3. Construir respuesta
            PaymentResponseDTO.BricksConfiguration bricksConfig = PaymentResponseDTO.BricksConfiguration.builder()
                .locale("es-AR")
                .theme("default")
                .paymentMethods(PaymentResponseDTO.PaymentMethods.builder()
                    .creditCard(true)
                    .debitCard(true)
                    .mercadoPagoWallet(true)
                    .cash(true)
                    .bankTransfer(true)
                    .build())
                .build();
            
            return PaymentResponseDTO.builder()
                .preferenceId("COMPLETED_" + purchaseResponse.getTransactionId())
                .initPoint(null)
                .sandboxInitPoint(null)
                .publicKey(publicKey)
                .totalAmount(paymentRequest.getTotalAmount())
                .status("COMPLETED")
                .qrCodeData(purchaseResponse.getTickets().get(0).getQrCode()) // QR del primer ticket
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodId("mercadopago")
                    .paymentTypeId("digital_wallet")
                    .paymentMethodName("MercadoPago")
                    .issuerName(null)
                    .build())
                .bricksConfig(bricksConfig)
                .build();
            
        } catch (Exception e) {
            log.error("Error processing confirmed payment: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing payment", e);
        }
    }
    
    private TicketPurchaseRequestDTO buildTicketPurchaseRequest(PaymentRequestDTO paymentRequest) {
        log.info("Building ticket purchase request from payment request");
        
        // Get MercadoPago payment method ID dynamically
        Long mercadoPagoPaymentMethodId = getMercadoPagoPaymentMethodId();
        
        List<TicketPurchaseRequestDTO.TicketRequestDTO> ticketRequests = new ArrayList<>();
        
        for (PaymentRequestDTO.TicketItemDTO ticketItem : paymentRequest.getTickets()) {
            // Crear una entrada por cada cantidad solicitada
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
            .paymentMethodId(mercadoPagoPaymentMethodId)
            .userId(paymentRequest.getUserId())
            .tickets(ticketRequests)
            .build();
    }
    
    private Long getMercadoPagoPaymentMethodId() {
        try {
            // Get all active payment methods and find MercadoPago
            var paymentMethods = transactionService.getActivePaymentMethods();
            return paymentMethods.stream()
                .filter(pm -> "MERCADOPAGO".equalsIgnoreCase(pm.getName()))
                .findFirst()
                .map(pm -> pm.getId())
                .orElseThrow(() -> new RuntimeException("MercadoPago payment method not found"));
        } catch (Exception e) {
            log.error("Error getting MercadoPago payment method ID: {}", e.getMessage());
            // Fallback to a reasonable default (this should be updated based on your actual DB)
            log.warn("Using fallback payment method ID. Please check your payment_methods table.");
            return 1L; // Fallback - adjust this based on your actual database
        }
    }
    
    private void generateTicketPDF(TicketDTO ticket) {
        log.info("Generating PDF for ticket: {}", ticket.getQrCode());
        try {
            // Generar PDF usando el servicio
            byte[] pdfBytes = ticketPdfService.generateTicketPdf(ticket);
            
            // Guardar PDF en el sistema de archivos
            String filePath = ticketPdfService.saveTicketPdf(ticket);
            
            log.info("PDF generated and saved successfully for ticket: {} at path: {}", ticket.getQrCode(), filePath);
        } catch (Exception e) {
            log.error("Error generating PDF for ticket: {}", e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo de pago
        }
    }
    
    private void sendTicketByEmail(TicketDTO ticket, String email) {
        log.info("Sending ticket by email to: {}", email);
        try {
            // Generar PDF para adjuntar al email
            byte[] pdfBytes = ticketPdfService.generateTicketPdf(ticket);
            
            // Enviar email con PDF adjunto
            ticketEmailService.sendTicketWithAttachment(ticket, email, pdfBytes);
            
            log.info("Email sent successfully for ticket: {} to {}", ticket.getQrCode(), email);
        } catch (Exception e) {
            log.error("Error sending email for ticket: {}", e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo de pago
        }
    }
    
    /**
     * Procesa únicamente entradas gratuitas (GIFT) sin usar MercadoPago
     */
    private PaymentResponseDTO processGiftTicketsOnly(PaymentRequestDTO paymentRequest) {
        log.info("Processing gift tickets only (no payment required)");
        
        try {
            // Obtener el método de pago para entradas gratuitas
            Long giftPaymentMethodId = getGiftPaymentMethodId();
            
            // Construir request de compra para entradas gratuitas
            TicketPurchaseRequestDTO ticketPurchaseRequest = buildTicketPurchaseRequestForGifts(paymentRequest, giftPaymentMethodId);
            
            // Procesar la compra directamente
            TicketPurchaseResponseDTO purchaseResponse = ticketService.purchaseTickets(ticketPurchaseRequest);
            
            // Generar PDFs y enviar emails para cada ticket
            for (TicketDTO ticket : purchaseResponse.getTickets()) {
                generateTicketPDF(ticket);
                sendTicketByEmail(ticket, paymentRequest.getPayer().getEmail());
            }
            
            // Configuración para respuesta
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
            
            return PaymentResponseDTO.builder()
                .preferenceId("GIFT_" + purchaseResponse.getTransactionId())
                .initPoint(null)
                .sandboxInitPoint(null)
                .publicKey(publicKey)
                .totalAmount(BigDecimal.ZERO) // Las entradas de regalo no tienen costo
                .status("COMPLETED")
                .qrCodeData(purchaseResponse.getTickets().get(0).getQrCode()) // QR del primer ticket
                .paymentMethodInfo(PaymentResponseDTO.PaymentMethodInfo.builder()
                    .paymentMethodId("gift")
                    .paymentTypeId("gift")
                    .paymentMethodName("Entrada de Regalo")
                    .issuerName(null)
                    .build())
                .bricksConfig(bricksConfig)
                .build();
                
        } catch (Exception e) {
            log.error("Error processing gift tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing gift tickets", e);
        }
    }
    
    /**
     * Construye request de compra específicamente para entradas gratuitas
     */
    private TicketPurchaseRequestDTO buildTicketPurchaseRequestForGifts(PaymentRequestDTO paymentRequest, Long giftPaymentMethodId) {
        log.info("Building ticket purchase request for gift tickets");
        
        List<TicketPurchaseRequestDTO.TicketRequestDTO> ticketRequests = new ArrayList<>();
        
        for (PaymentRequestDTO.TicketItemDTO ticketItem : paymentRequest.getTickets()) {
            // Solo procesar entradas gratuitas
            if (ticketItem.getPrice() == null || ticketItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                // Crear una entrada por cada cantidad solicitada
                for (int i = 0; i < ticketItem.getQuantity(); i++) {
                    TicketPurchaseRequestDTO.TicketRequestDTO ticketRequest = new TicketPurchaseRequestDTO.TicketRequestDTO();
                    ticketRequest.setSectionId(ticketItem.getSectionId());
                    ticketRequest.setAttendeeFirstName(ticketItem.getAttendeeFirstName());
                    ticketRequest.setAttendeeLastName(ticketItem.getAttendeeLastName());
                    ticketRequest.setAttendeeDni(ticketItem.getAttendeeDni());
                    ticketRequest.setPrice(BigDecimal.ZERO); // Forzar precio 0 para entradas de regalo
                    
                    // IMPORTANTE: Pasar información del tipo de ticket
                    ticketRequest.setTicketPriceId(ticketItem.getTicketPriceId());
                    ticketRequest.setTicketType(ticketItem.getTicketType());
                    
                    ticketRequests.add(ticketRequest);
                }
            }
        }
        
        return TicketPurchaseRequestDTO.builder()
            .eventId(paymentRequest.getEventId())
            .paymentMethodId(giftPaymentMethodId)
            .userId(paymentRequest.getUserId())
            .tickets(ticketRequests)
            .build();
    }
    
    /**
     * Obtiene el ID del método de pago para entradas gratuitas
     */
    private Long getGiftPaymentMethodId() {
        try {
            // Buscar el método de pago para entradas gratuitas
            var paymentMethods = transactionService.getActivePaymentMethods();
            return paymentMethods.stream()
                .filter(pm -> "GIFT".equalsIgnoreCase(pm.getName()) || "ENTRADA_REGALO".equalsIgnoreCase(pm.getName()))
                .findFirst()
                .map(pm -> pm.getId())
                .orElseThrow(() -> new RuntimeException("Gift payment method not found"));
        } catch (Exception e) {
            log.error("Error getting gift payment method ID: {}", e.getMessage());
            // Fallback - necesitarás ajustar esto según tu base de datos
            log.warn("Using fallback gift payment method ID. Please check your payment_methods table.");
            return 6L; // Ajustar según tu DB real
        }
    }
} 