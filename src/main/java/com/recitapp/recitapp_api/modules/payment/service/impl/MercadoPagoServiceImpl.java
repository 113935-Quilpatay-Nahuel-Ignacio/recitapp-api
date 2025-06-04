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
        try {
            log.info("Creating MercadoPago preference for event: {}, user: {}, amount: {}", 
                    paymentRequest.getEventId(), paymentRequest.getUserId(), paymentRequest.getTotalAmount());
            
            PreferenceClient client = new PreferenceClient();
            
            // Validar datos de entrada
            if (paymentRequest.getTickets() == null || paymentRequest.getTickets().isEmpty()) {
                throw new IllegalArgumentException("No tickets provided in payment request");
            }
            
            // Crear items de la preferencia
            List<PreferenceItemRequest> items = paymentRequest.getTickets().stream()
                .map((PaymentRequestDTO.TicketItemDTO ticket) -> {
                    log.debug("Creating item for ticket: type={}, price={}, quantity={}", 
                            ticket.getTicketType(), ticket.getPrice(), ticket.getQuantity());
                    
                    // Validaciones de los items
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
                    
                    return PreferenceItemRequest.builder()
                        .id(String.valueOf(ticket.getTicketPriceId() != null ? ticket.getTicketPriceId() : "1"))
                        .title(itemTitle)
                        .description(itemDescription)
                        .pictureUrl(null) // Podrías agregar imagen del evento aquí
                        .categoryId("tickets")
                        .quantity(ticket.getQuantity())
                        .currencyId("ARS")
                        .unitPrice(new BigDecimal(ticket.getPrice().toString()))
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

            // Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .payer(payer)
                .externalReference(externalReference)
                .notificationUrl(webhookUrl)
                .expires(false)
                .build();

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

            return PaymentResponseDTO.builder()
                // Campos para Checkout Pro (compatibilidad)
                .preferenceId(preference.getId())
                .initPoint(preference.getInitPoint())
                .sandboxInitPoint(preference.getSandboxInitPoint())
                // Campos para Checkout Bricks
                .publicKey(publicKey)
                .totalAmount(paymentRequest.getTotalAmount())
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
                    
                    log.info("Payment method details - Method: {}, Type: {}, Name: {}, Issuer: {}", 
                            paymentMethodId, paymentTypeId, paymentMethodName, issuerName);
                            
                    // Aquí podrías actualizar tu base de datos con esta información
                    // updatePaymentMethodInDatabase(dataId, paymentMethodId, paymentTypeId, paymentMethodName, issuerName);
                    
                } catch (MPException | MPApiException e) {
                    log.error("Error getting payment details: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing webhook notification", e);
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
                // ticketRequest.setPromotionId(null); // No promotions for now
                
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
} 