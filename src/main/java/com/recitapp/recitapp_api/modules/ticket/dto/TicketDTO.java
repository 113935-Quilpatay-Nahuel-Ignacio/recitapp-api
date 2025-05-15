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
}