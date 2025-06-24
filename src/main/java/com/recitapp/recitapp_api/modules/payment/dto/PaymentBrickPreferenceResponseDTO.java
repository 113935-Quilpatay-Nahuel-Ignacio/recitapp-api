package com.recitapp.recitapp_api.modules.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentBrickPreferenceResponseDTO {
    
    @JsonProperty("preference_id")
    private String preferenceId;
    
    @JsonProperty("public_key")
    private String publicKey;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency_id")
    private String currencyId;
    
    @JsonProperty("external_reference")
    private String externalReference;
    
    @JsonProperty("success_url")
    private String successUrl;
    
    @JsonProperty("failure_url")
    private String failureUrl;
    
    @JsonProperty("pending_url")
    private String pendingUrl;
    
    @JsonProperty("webhook_url")
    private String webhookUrl;
    
    @JsonProperty("init_point")
    private String initPoint;
    
    @JsonProperty("sandbox_init_point")
    private String sandboxInitPoint;
    
    // Información del estado
    private String status;
    private String message;
    
    // Para debugging
    @JsonProperty("payment_methods_config")
    private PaymentMethodsConfigDTO paymentMethodsConfig;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodsConfigDTO {
        @JsonProperty("credit_card")
        private Boolean creditCard;
        
        @JsonProperty("debit_card")
        private Boolean debitCard;
        
        @JsonProperty("mercado_pago_wallet")
        private Boolean mercadoPagoWallet;
        
        @JsonProperty("excluded_payment_types")
        private String[] excludedPaymentTypes;
        
        @JsonProperty("max_installments")
        private Integer maxInstallments;
    }
} 