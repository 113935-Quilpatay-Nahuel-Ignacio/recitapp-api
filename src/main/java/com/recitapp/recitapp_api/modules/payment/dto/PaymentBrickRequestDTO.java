package com.recitapp.recitapp_api.modules.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBrickRequestDTO {
    
    @NotNull(message = "Event ID is required")
    @JsonProperty("event_id")
    private Long eventId;
    
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    private Long userId;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    @NotNull(message = "Tickets are required")
    private List<TicketItemDTO> tickets;
    
    // Información del pagador (puede ser diferente al usuario logueado)
    private PayerDTO payer;
    
    // Configuración específica del Payment Brick
    @JsonProperty("payment_config")
    private PaymentConfigDTO paymentConfig;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketItemDTO {
        @NotNull(message = "Ticket price ID is required")
        @JsonProperty("ticket_price_id")
        private Long ticketPriceId;
        
        @NotNull(message = "Ticket type is required")
        @JsonProperty("ticket_type")
        private String ticketType;
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must be non-negative")
        private BigDecimal price;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerDTO {
        @Email(message = "Valid email is required")
        private String email;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        // Identificación
        @JsonProperty("identification_type")
        private String identificationType;
        
        @JsonProperty("identification_number")
        private String identificationNumber;
        
        // Teléfono
        @JsonProperty("area_code")
        private String areaCode;
        
        private String phone;
        
        // Dirección
        @JsonProperty("street_name")
        private String streetName;
        
        @JsonProperty("street_number")
        private String streetNumber;
        
        @JsonProperty("zip_code")
        private String zipCode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentConfigDTO {
        // Métodos de pago habilitados para Payment Brick
        @JsonProperty("credit_card")
        private Boolean creditCard = true;
        
        @JsonProperty("debit_card")
        private Boolean debitCard = true;
        
        @JsonProperty("mercado_pago_wallet")
        private Boolean mercadoPagoWallet = true;
        
        // Deshabilitar métodos no deseados
        @JsonProperty("ticket")
        private Boolean ticket = false;
        
        @JsonProperty("bank_transfer")
        private Boolean bankTransfer = false;
        
        // Configuración de cuotas
        @JsonProperty("max_installments")
        private Integer maxInstallments = 12;
        
        // Propósito de la preferencia
        @JsonProperty("purpose")
        private String purpose = "wallet_purchase"; // wallet_purchase | onboarding_credits
    }
} 