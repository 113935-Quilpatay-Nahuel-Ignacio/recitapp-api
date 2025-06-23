package com.recitapp.recitapp_api.modules.payment.service.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
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
        return createPaymentPreference(paymentRequest, false);
    }

    public PaymentResponseDTO createPaymentPreferenceWalletOnly(PaymentRequestDTO paymentRequest) {
        return createPaymentPreference(paymentRequest, true);
    }

    private PaymentResponseDTO createPaymentPreference(PaymentRequestDTO paymentRequest, boolean walletOnly) {
        try {
            log.info("Creating MercadoPago preference for event: {}, user: {}, amount: {}, walletOnly: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId(), paymentRequest.getTotalAmount(), walletOnly);
            
            PreferenceClient client = new PreferenceClient();
            
            // Validar datos de entrada
            if (paymentRequest.getTickets() == null || paymentRequest.getTickets().isEmpty()) {
                throw new IllegalArgumentException("No tickets provided in payment request");
            }
            
            // Separar entradas de pago de entradas gratuitas
            List<PaymentRequestDTO.TicketItemDTO> paidTickets = paymentRequest.getTickets().stream()
                .filter(ticket -> ticket.getPrice() != null && ticket.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
                
            List<PaymentRequestDTO.TicketItemDTO> giftTickets = paymentRequest.getTickets().stream()
                .filter(ticket -> ticket.getPrice() == null || ticket.getPrice().compareTo(BigDecimal.ZERO) <= 0)
                .collect(Collectors.toList());
                
            log.info("Processing {} paid tickets and {} gift tickets", paidTickets.size(), giftTickets.size());
            
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
                log.info("Wallet discount detected. Original total: {}, Requested amount: {}, Discount ratio: {}", 
                    originalPaidTotal, requestedAmount, discountRatio);
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

            log.debug("Created {} items for preference", items.size());

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();

            log.debug("Configured back URLs - Success: {}, Failure: {}, Pending: {}", 
                    successUrl, failureUrl, pendingUrl);

            // Configurar pagador (simplificado para evitar errores de SDK)
            PreferencePayerRequest payer = null;
            if (paymentRequest.getPayer() != null && paymentRequest.getPayer().getEmail() != null) {
                log.debug("Configuring payer: email={}, name={} {}", 
                        paymentRequest.getPayer().getEmail(), 
                        paymentRequest.getPayer().getFirstName(), 
                        paymentRequest.getPayer().getLastName());
                
                try {
                    // Configuración básica del pagador
                    payer = PreferencePayerRequest.builder()
                        .email(paymentRequest.getPayer().getEmail())
                        .name(paymentRequest.getPayer().getFirstName() != null ? 
                              paymentRequest.getPayer().getFirstName() : "")
                        .surname(paymentRequest.getPayer().getLastName() != null ? 
                                paymentRequest.getPayer().getLastName() : "")
                        .build();
                    
                    log.debug("Payer configured successfully with email: {}", paymentRequest.getPayer().getEmail());
                    
                } catch (Exception e) {
                    log.warn("Error configuring payer, proceeding with minimal payer info: {}", e.getMessage());
                    // Configurar pagador mínimo solo con email
                    payer = PreferencePayerRequest.builder()
                        .email(paymentRequest.getPayer().getEmail())
                        .build();
                }
            } else {
                log.warn("No payer information provided or missing email");
            }

            String externalReference = "EVENTO_" + paymentRequest.getEventId() + "_USER_" + paymentRequest.getUserId() + "_" + UUID.randomUUID().toString();
            log.debug("Generated external reference: {}", externalReference);

            // Crear preferencia con soporte para Wallet Purchase (cuentas de MercadoPago)
            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .payer(payer)
                .externalReference(externalReference)
                .notificationUrl(webhookUrl)
                .expires(false);
                
            // Solo agregar purpose si es wallet-only
            if (walletOnly) {
                requestBuilder.purpose("wallet_purchase"); // Habilita pagos solo con cuentas de MercadoPago
                log.info("Preference configured for wallet-only payments");
            } else {
                log.info("Preference configured for all payment methods");
            }
                
            PreferenceRequest preferenceRequest = requestBuilder.build();

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
            log.info("Processing MercadoPago webhook with params: {}", params);
            log.info("Webhook payload: {}", payload);
            
            String type = params.get("type");
            String dataId = params.get("data.id");
            
            if ("payment".equals(type) && dataId != null) {
                log.info("Processing payment notification for payment ID: {}", dataId);
                
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
                    
                    log.info("Payment method details - Method: {}, Type: {}, Name: {}, Issuer: {}, Status: {}, External Reference: {}", 
                            paymentMethodId, paymentTypeId, paymentMethodName, issuerName, paymentStatus, externalReference);
                    
                    // Actualizar la transacción con el payment ID real de MercadoPago
                    if (externalReference != null && "approved".equals(paymentStatus)) {
                        log.info("Updating transaction with MercadoPago payment ID: {} for external reference: {}", dataId, externalReference);
                        boolean updated = updateTransactionWithPaymentId(externalReference, dataId, paymentMethodName);
                        
                        if (updated) {
                            log.info("Transaction successfully updated with MercadoPago payment ID: {}", dataId);
                        } else {
                            log.warn("Could not find transaction with external reference: {} to update with payment ID: {}", externalReference, dataId);
                        }
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