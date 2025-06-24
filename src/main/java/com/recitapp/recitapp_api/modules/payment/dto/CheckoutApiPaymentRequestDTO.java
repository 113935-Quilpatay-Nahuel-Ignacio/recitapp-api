package com.recitapp.recitapp_api.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutApiPaymentRequestDTO {
    
    // Información básica del pago
    @NotNull(message = "El monto total es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal totalAmount;
    
    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;
    
    // Información del evento/compra
    @NotNull(message = "El ID del evento es requerido")
    private Long eventId;
    
    @NotNull(message = "El ID del usuario es requerido")
    private Long userId;
    
    // Token de la tarjeta (para pagos con tarjeta)
    private String cardToken;
    
    // Método de pago
    @NotBlank(message = "El método de pago es requerido")
    private String paymentMethodId; // "visa", "master", "account_money", etc.
    
    // Tipo de pago
    @NotBlank(message = "El tipo de pago es requerido")
    private String paymentTypeId; // "credit_card", "debit_card", "account_money"
    
    // Cuotas (solo para tarjetas)
    @Min(value = 1, message = "Las cuotas deben ser al menos 1")
    private Integer installments = 1;
    
    // Información del pagador
    @NotNull(message = "La información del pagador es requerida")
    private PayerInfo payer;
    
    // Información adicional para tarjetas
    private CardInfo cardInfo;
    
    // Referencia externa
    private String externalReference;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerInfo {
        @NotBlank(message = "El email es requerido")
        @Email(message = "El email debe tener un formato válido")
        private String email;
        
        private String firstName;
        private String lastName;
        
        // Información de identificación
        private String identificationType; // "DNI", "CI", "CUIL", etc.
        private String identificationNumber;
        
        // Información de contacto
        private String phone;
        private AddressInfo address;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardInfo {
        private String cardholderName;
        private String issuerId;
        private String firstSixDigits;
        private String lastFourDigits;
        private String expirationMonth;
        private String expirationYear;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String streetName;
        private String streetNumber;
        private String zipCode;
        private String city;
        private String state;
        private String country;
    }
    
    // Métodos de utilidad
    public boolean isCardPayment() {
        return "credit_card".equals(paymentTypeId) || "debit_card".equals(paymentTypeId);
    }
    
    public boolean isWalletPayment() {
        return "account_money".equals(paymentTypeId);
    }
    
    public boolean hasValidCardToken() {
        return cardToken != null && !cardToken.trim().isEmpty();
    }
} 