package com.recitapp.recitapp_api.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long eventId;
    private Long userId;
    private List<TicketItemDTO> tickets;
    private BigDecimal totalAmount;
    private PayerDTO payer;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketItemDTO {
        private Long sectionId;
        private Long ticketPriceId;
        private String ticketType;
        private String attendeeFirstName;
        private String attendeeLastName;
        private String attendeeDni;
        private BigDecimal price;
        private Integer quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayerDTO {
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String documentType;
        private String documentNumber;
    }
} 