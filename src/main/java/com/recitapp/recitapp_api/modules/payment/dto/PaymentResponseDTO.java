package com.recitapp.recitapp_api.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    // Campos para Checkout Pro (mantener compatibilidad)
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    
    // Campos para Checkout Bricks y API
    private String publicKey;
    private BigDecimal totalAmount;
    private String status;
    private Long transactionId;
    private String qrCodeData;
    
    // Nuevos campos para manejo mejorado de estados
    private String statusCode; // APRO, OTHE, CONT, etc.
    private String statusDetail; // status_detail de MercadoPago
    private String displayName; // Nombre legible del estado
    private String userMessage; // Mensaje específico para el usuario
    private boolean shouldDeliverTickets; // Si debe entregar entradas
    private boolean canRetry; // Si el usuario puede reintentar
    private String paymentId; // ID del pago en MercadoPago
    
    // Wallet information
    private BigDecimal walletDiscountApplied;
    private BigDecimal amountAfterWallet;
    private String walletMessage;
    
    // Información del método de pago
    private PaymentMethodInfo paymentMethodInfo;
    
    // Configuración para Bricks (mantener compatibilidad)
    private BricksConfiguration bricksConfig;
    
    // Nueva configuración para Checkout API
    private ApiConfiguration apiConfig;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BricksConfiguration {
        private String locale; // 'es-AR', 'pt-BR', etc.
        private String theme; // 'default', 'dark', 'bootstrap', 'flat'
        private PaymentMethods paymentMethods;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiConfiguration {
        private String locale; // 'es-AR', 'pt-BR', etc.
        private String theme; // 'default', 'dark', 'bootstrap', 'flat'
        private EnabledPaymentMethods enabledPaymentMethods;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethods {
        private boolean creditCard;
        private boolean debitCard;
        private boolean mercadoPagoWallet;
        private boolean cash;
        private boolean bankTransfer;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnabledPaymentMethods {
        private boolean creditCard;
        private boolean debitCard;
        private boolean mercadoPagoWallet;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodInfo {
        private String paymentMethodId; // "visa", "master", "account_money", etc.
        private String paymentTypeId; // "credit_card", "debit_card", "account_money", etc.
        private String paymentMethodName; // "Visa", "Mastercard", "Dinero en cuenta", etc.
        private String issuerName; // Banco emisor
    }
} 