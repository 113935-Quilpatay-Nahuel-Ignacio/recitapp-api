package com.recitapp.recitapp_api.modules.payment.service.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.payment.PaymentRefundCreateRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentRefund;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPApiException;
import com.recitapp.recitapp_api.modules.payment.dto.RefundResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.MercadoPagoRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoRefundServiceImpl implements MercadoPagoRefundService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Override
    public RefundResponseDTO processRefund(String paymentId, BigDecimal amount, String reason) {
        log.info("Processing MercadoPago refund for payment ID: {}, amount: {}, reason: {}", paymentId, amount, reason);
        
        try {
            // Configure MercadoPago with access token
            MercadoPagoConfig.setAccessToken(accessToken);
            
            // First, verify the payment exists and is refundable
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.parseLong(paymentId));
            
            if (payment == null) {
                log.error("Payment not found: {}", paymentId);
                return RefundResponseDTO.failure(
                        paymentId,
                        amount,
                        "Payment not found in MercadoPago",
                        "PAYMENT_NOT_FOUND"
                );
            }
            
            // Check if payment can be refunded
            if (!canRefundPayment(payment)) {
                log.error("Payment {} cannot be refunded. Status: {}", paymentId, payment.getStatus());
                return RefundResponseDTO.failure(
                        paymentId,
                        amount,
                        "Payment cannot be refunded. Status: " + payment.getStatus(),
                        "PAYMENT_NOT_REFUNDABLE"
                );
            }
            
            // Create refund client
            PaymentRefundClient refundClient = new PaymentRefundClient();
            
            // Log reason if provided
            if (reason != null && !reason.trim().isEmpty()) {
                log.info("Refund reason provided: {}", reason);
            }
            
            // Process the refund using the correct MercadoPago API method
            // According to the official documentation, we use refund(Long paymentId, BigDecimal amount)
            PaymentRefund refund = refundClient.refund(Long.parseLong(paymentId), amount);
            
            log.info("MercadoPago refund created successfully. Refund ID: {}, Status: {}, Amount: {}", 
                    refund.getId(), refund.getStatus(), refund.getAmount());
            
            // Handle different refund statuses and create appropriate response
            String statusMessage = getRefundStatusMessage(refund.getStatus());
            
            return RefundResponseDTO.success(
                    refund.getId().toString(),
                    paymentId,
                    refund.getAmount(),
                    refund.getStatus(),
                    statusMessage
            );
            
        } catch (MPApiException e) {
            log.error("MercadoPago API error during refund for payment {}: Status: {}, Message: {}", 
                     paymentId, e.getStatusCode(), e.getMessage());
            
            String errorMessage = getMercadoPagoApiErrorMessage(e.getStatusCode(), e.getMessage());
            
            return RefundResponseDTO.failure(
                    paymentId,
                    amount,
                    errorMessage,
                    "MP_API_ERROR_" + e.getStatusCode()
            );
            
        } catch (MPException e) {
            log.error("MercadoPago SDK error during refund for payment {}: {}", paymentId, e.getMessage());
            
            return RefundResponseDTO.failure(
                    paymentId,
                    amount,
                    "Error de conexión con MercadoPago: " + e.getMessage(),
                    "MP_SDK_ERROR"
            );
            
        } catch (NumberFormatException e) {
            log.error("Invalid payment ID format: {}", paymentId);
            
            return RefundResponseDTO.failure(
                    paymentId,
                    amount,
                    "Formato de ID de pago inválido",
                    "INVALID_PAYMENT_ID"
            );
            
        } catch (Exception e) {
            log.error("Unexpected error during refund processing for payment {}: {}", paymentId, e.getMessage(), e);
            
            return RefundResponseDTO.failure(
                    paymentId,
                    amount,
                    "Error inesperado al procesar el reembolso: " + e.getMessage(),
                    "UNEXPECTED_ERROR"
            );
        }
    }
    
    private boolean canRefundPayment(Payment payment) {
        String status = payment.getStatus();
        return "approved".equals(status) || "authorized".equals(status);
    }
    
    private String getRefundStatusMessage(String status) {
        return switch (status) {
            case "approved" -> "Reembolso aprobado y procesado exitosamente";
            case "pending" -> "Reembolso en proceso. Se completará en los próximos días hábiles";
            case "rejected" -> "Reembolso rechazado por MercadoPago";
            case "cancelled" -> "Reembolso cancelado";
            default -> "Estado del reembolso: " + status;
        };
    }
    
    private String getMercadoPagoApiErrorMessage(int statusCode, String originalMessage) {
        return switch (statusCode) {
            case 400 -> "Solicitud de reembolso inválida. Verifique los datos proporcionados";
            case 401 -> "Credenciales de MercadoPago inválidas o expiradas";
            case 403 -> "No tiene permisos para realizar esta operación";
            case 404 -> "Pago no encontrado en MercadoPago";
            case 422 -> "El pago no puede ser reembolsado en este momento";
            case 429 -> "Demasiadas solicitudes. Intente nuevamente en unos minutos";
            case 500 -> "Error interno de MercadoPago. Intente nuevamente más tarde";
            default -> "Error de MercadoPago: " + originalMessage;
        };
    }

    @Override
    public boolean canRefund(String paymentId) {
        log.info("Checking if payment {} can be refunded", paymentId);
        
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient paymentClient = new PaymentClient();
            
            Payment payment = paymentClient.get(Long.parseLong(paymentId));
            
            // Check if payment status allows refund
            String status = payment.getStatus();
            boolean canRefund = "approved".equals(status) || "authorized".equals(status);
            
            log.info("Payment {} status: {}, can refund: {}", paymentId, status, canRefund);
            return canRefund;
            
        } catch (Exception e) {
            log.error("Error checking refund eligibility for payment {}: {}", paymentId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getRefundStatus(String refundId) {
        log.info("Getting refund status for refund ID: {}", refundId);
        
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentRefundClient refundClient = new PaymentRefundClient();
            
            // Note: MercadoPago doesn't have a direct method to get refund by ID
            // This would need to be implemented based on the specific MercadoPago API version
            // For now, we'll return a placeholder
            log.warn("getRefundStatus not fully implemented - returning 'unknown'");
            return "unknown";
            
        } catch (Exception e) {
            log.error("Error getting refund status for refund {}: {}", refundId, e.getMessage());
            return "error";
        }
    }
} 