package com.recitapp.recitapp_api.modules.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MercadoPagoPaymentStatus {
    
    // Estados de aprobación
    APRO("APRO", "approved", "Pago aprobado", "Tu pago ha sido aprobado exitosamente", true, false),
    APPROVED("approved", "approved", "Pago aprobado", "Tu pago ha sido aprobado exitosamente", true, false),
    
    // Estados pendientes
    CONT("CONT", "pending", "Pendiente de pago", "Tu pago está siendo procesado. Te daremos tus entradas cuando se complete el proceso.", false, false),
    PENDING("pending", "pending", "Pendiente de pago", "Tu pago está siendo procesado. Te daremos tus entradas cuando se complete el proceso.", false, false),
    IN_PROCESS("in_process", "in_process", "En proceso", "Tu pago está siendo procesado. Te daremos tus entradas cuando se complete el proceso.", false, false),
    
    // Estados de rechazo - Errores de tarjeta
    CALL("CALL", "rejected", "Rechazado - Validación requerida", 
         "Tu pago fue rechazado. Debes contactar a tu banco para autorizar el pago. Puedes intentar con otra tarjeta.", false, true),
    
    FUND("FUND", "rejected", "Rechazado - Fondos insuficientes", 
         "Tu pago fue rechazado por fondos insuficientes. Verifica tu saldo o intenta con otra tarjeta.", false, true),
    
    SECU("SECU", "rejected", "Rechazado - Código de seguridad inválido", 
         "Tu pago fue rechazado por código de seguridad inválido. Verifica el CVV de tu tarjeta e intenta nuevamente.", false, true),
    
    EXPI("EXPI", "rejected", "Rechazado - Fecha de vencimiento inválida", 
         "Tu pago fue rechazado por fecha de vencimiento inválida. Verifica la fecha de tu tarjeta e intenta nuevamente.", false, true),
    
    FORM("FORM", "rejected", "Rechazado - Error en formulario", 
         "Tu pago fue rechazado por datos incorrectos. Verifica todos los campos del formulario e intenta nuevamente.", false, true),
    
    OTHE("OTHE", "rejected", "Rechazado - Error general", 
         "Tu pago fue rechazado. Verifica los datos de tu tarjeta o intenta con otra tarjeta.", false, true),
    
    // Estados genéricos de rechazo
    REJECTED("rejected", "rejected", "Pago rechazado", 
             "Tu pago fue rechazado. Verifica los datos de tu tarjeta o intenta con otra tarjeta.", false, true),
    
    // Estados de cancelación
    CANCELLED("cancelled", "cancelled", "Pago cancelado", "El pago ha sido cancelado.", false, false),
    
    // Estados de autorización
    AUTHORIZED("authorized", "authorized", "Pago autorizado", "El pago ha sido autorizado pero aún no capturado.", false, false),
    
    // Estados de reembolso
    REFUNDED("refunded", "refunded", "Pago reembolsado", "El pago ha sido reembolsado.", false, false),
    CHARGED_BACK("charged_back", "charged_back", "Contracargo", "Se ha realizado un contracargo.", false, false),
    
    // Estado desconocido
    UNKNOWN("unknown", "unknown", "Estado desconocido", "No se pudo determinar el estado del pago.", false, false);
    
    private final String code;
    private final String status;
    private final String displayName;
    private final String userMessage;
    private final boolean shouldDeliverTickets;
    private final boolean canRetry;
    
    /**
     * Obtiene el enum correspondiente al código de estado
     */
    public static MercadoPagoPaymentStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String normalizedCode = code.trim().toUpperCase();
        
        for (MercadoPagoPaymentStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(normalizedCode)) {
                return status;
            }
        }
        
        // Si no encuentra el código exacto, intenta con el status
        for (MercadoPagoPaymentStatus status : values()) {
            if (status.getStatus().equalsIgnoreCase(code.trim().toLowerCase())) {
                return status;
            }
        }
        
        return UNKNOWN;
    }
    
    /**
     * Obtiene el enum correspondiente al status detail de MercadoPago
     */
    public static MercadoPagoPaymentStatus fromStatusDetail(String statusDetail) {
        if (statusDetail == null || statusDetail.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String detail = statusDetail.trim().toLowerCase();
        
        // Mapear status_detail específicos a nuestros enums
        if (detail.contains("call_for_authorize") || detail.contains("call")) {
            return CALL;
        } else if (detail.contains("insufficient_amount") || detail.contains("fund")) {
            return FUND;
        } else if (detail.contains("security_code") || detail.contains("cvv") || detail.contains("secu")) {
            return SECU;
        } else if (detail.contains("expiration") || detail.contains("date") || detail.contains("expi")) {
            return EXPI;
        } else if (detail.contains("bad_filled") || detail.contains("form")) {
            return FORM;
        } else if (detail.contains("other") || detail.contains("general")) {
            return OTHE;
        }
        
        return UNKNOWN;
    }
    
    /**
     * Determina el estado final basado en status y status_detail
     */
    public static MercadoPagoPaymentStatus determineStatus(String status, String statusDetail) {
        if (status == null) {
            return UNKNOWN;
        }
        
        String normalizedStatus = status.trim().toLowerCase();
        
        // Si es aprobado, siempre es APRO/APPROVED
        if ("approved".equals(normalizedStatus)) {
            return APRO;
        }
        
        // Si es pendiente, usar CONT
        if ("pending".equals(normalizedStatus) || "in_process".equals(normalizedStatus)) {
            return CONT;
        }
        
        // Si es rechazado, usar el status_detail para determinar el motivo específico
        if ("rejected".equals(normalizedStatus)) {
            MercadoPagoPaymentStatus detailStatus = fromStatusDetail(statusDetail);
            if (detailStatus != UNKNOWN) {
                return detailStatus;
            }
            return OTHE; // Error general si no se puede determinar
        }
        
        // Para otros estados, usar el status directamente
        return fromCode(status);
    }
} 