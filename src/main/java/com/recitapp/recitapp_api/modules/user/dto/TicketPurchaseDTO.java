package com.recitapp.recitapp_api.modules.user.dto;

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
public class TicketPurchaseDTO {
    private Long ticketId;
    private String eventName;
    private String artistName;
    private String venueName;
    private String section;
    private LocalDateTime eventDate;
    private BigDecimal price;
    private String ticketStatus;
    private String qrCode;
}