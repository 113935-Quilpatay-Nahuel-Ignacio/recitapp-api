package com.recitapp.recitapp_api.modules.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBrickProcessRequestDTO {
    
    @NotNull(message = "Payment method ID is required")
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than 0")
    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;
    
    @JsonProperty("installments")
    private Integer installments;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("preference_id")
    private String preferenceId;
    
    @JsonProperty("external_reference")
    private String externalReference;
    
    @NotNull(message = "Payer information is required")
    private PayerDTO payer;
    
    @JsonProperty("additional_info")
    private AdditionalInfoDTO additionalInfo;
    
    // Para Payment Brick específico
    @JsonProperty("form_data")
    private Map<String, Object> formData;
    
    @JsonProperty("selected_payment_method")
    private String selectedPaymentMethod;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerDTO {
        @NotNull(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        private IdentificationDTO identification;
        
        private PhoneDTO phone;
        
        private AddressDTO address;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentificationDTO {
        private String type;
        private String number;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        private String number;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
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
    public static class AdditionalInfoDTO {
        @JsonProperty("ip_address")
        private String ipAddress;
        
        private ItemsDTO[] items;
        
        private PayerInfoDTO payer;
        
        @JsonProperty("shipments")
        private ShipmentsDTO shipments;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemsDTO {
        private String id;
        private String title;
        private String description;
        @JsonProperty("picture_url")
        private String pictureUrl;
        @JsonProperty("category_id")
        private String categoryId;
        private Integer quantity;
        @JsonProperty("unit_price")
        private BigDecimal unitPrice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerInfoDTO {
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        private PhoneDTO phone;
        
        private AddressDTO address;
        
        @JsonProperty("registration_date")
        private String registrationDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentsDTO {
        @JsonProperty("receiver_address")
        private AddressDTO receiverAddress;
    }
} 