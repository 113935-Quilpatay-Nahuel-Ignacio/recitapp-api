package com.recitapp.recitapp_api.modules.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDate;
    private Long sectionId;
    private String sectionName;
    private String venueName;
    private BigDecimal price;
    private String status;
    private String attendeeFirstName;
    private String attendeeLastName;
    private String attendeeDni;
    private String qrCode;
    private LocalDateTime purchaseDate;
    
    // User information (purchaser)
    private Long userId;
    private String userName;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    
    // Promotional information
    private Boolean isGift;
    private String promotionName;
    private String promotionDescription;
    private String ticketType; // "PROMOTIONAL_2X1", "GENERAL", "GIFT"
}